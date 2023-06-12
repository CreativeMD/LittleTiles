package team.creative.littletiles.client.render.cache.pipeline;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.lighting.QuadLighter;
import team.creative.creativecore.client.render.model.CreativeQuadLighter;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.mixin.BufferBuilderAccessor;
import team.creative.creativecore.mixin.ForgeModelBlockRendererAccessor;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.client.IFakeRenderingBlock;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.ByteBufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.level.little.LittleSubLevel;

@OnlyIn(Dist.CLIENT)
public class LittleRenderPipelineForge extends LittleRenderPipeline {
    
    public static final LittleRenderPipelineForge INSTANCE = new LittleRenderPipelineForge();
    private static final ThreadLocal<BufferBuilder> BUILDER_SUPPLIER = ThreadLocal.withInitial(() -> new BufferBuilder(131072));
    
    @Override
    public void startCompile(RenderChunkExtender chunk) {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBuffer vertexBuffer = chunk.getVertexBuffer(layer);
            ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
            if (manager != null) {
                synchronized (manager) {
                    manager.queued++;
                }
                manager.backToRAM();
            } else
                ((VertexBufferExtender) vertexBuffer).setManager(manager = new ChunkLayerUploadManager(chunk, layer));
        }
    }
    
    @Override
    public void endCompile(RenderChunkExtender chunk, RebuildTaskExtender task) {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBuffer vertexBuffer = chunk.getVertexBuffer(layer);
            ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
            synchronized (manager) {
                manager.queued--;
            }
        }
        
        ChunkLayerMap<ChunkLayerCache> caches = task.getLayeredCache();
        if (caches != null)
            for (Entry<RenderType, ChunkLayerCache> entry : caches.tuples()) {
                VertexBuffer vertexBuffer = chunk.getVertexBuffer(entry.getKey());
                ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
                manager.set(entry.getValue());
            }
        
        task.clear();
    }
    
    @Override
    public void add(RenderChunkExtender chunk, BETiles be, RebuildTaskExtender rebuildTask) {
        be.updateQuadCache(chunk);
        
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            synchronized (be.render.getBufferCache()) {
                if (!be.render.getBufferCache().has(layer))
                    continue;
                
                be.render.getBufferCache().add(layer, rebuildTask.builder(layer), rebuildTask.getOrCreate(layer));
            }
        }
    }
    
    @Override
    public void buildCache(PoseStack pose, ChunkLayerMap<BufferHolder> buffers, RenderingBlockContext data, VertexFormat format, SingletonList<BakedQuad> bakedQuadWrapper) {
        Level renderLevel = data.be.getLevel();
        while (renderLevel instanceof LittleSubLevel sub && !sub.shouldUseLightingForRenderig())
            renderLevel = sub.getParent();
        
        BlockPos pos = data.be.getBlockPos();
        
        ForgeModelBlockRendererAccessor renderer = (ForgeModelBlockRendererAccessor) MC.getBlockRenderer().getModelRenderer();
        boolean smooth = Minecraft.useAmbientOcclusion() && data.state.getLightEmission(data.be.getLevel(), pos) == 0;
        QuadLighter lighter = smooth ? renderer.getSmoothLighter().get() : renderer.getFlatLighter().get();
        
        lighter.setup(renderLevel, pos, data.state);
        
        int overlay = OverlayTexture.NO_OVERLAY;
        
        data.chunk.prepareBlockTranslation(pose, pos);
        
        // Render vertex buffer
        for (Tuple<RenderType, IndexedCollector<LittleRenderBox>> entry : data.be.render.boxCache.tuples()) {
            
            IndexedCollector<LittleRenderBox> cubes = entry.value;
            if (cubes == null || cubes.isEmpty())
                continue;
            
            BufferBuilder builder = BUILDER_SUPPLIER.get();
            
            builder.begin(VertexFormat.Mode.QUADS, format);
            
            IntArrayList indexes = new IntArrayList();
            for (Iterator<LittleRenderBox> iterator = cubes.sectionIterator(x -> {
                indexes.add(x);
                indexes.add(((BufferBuilderAccessor) builder).getVertices() * format.getVertexSize());
            });iterator.hasNext();) {
                LittleRenderBox cube = iterator.next();
                BlockState state = cube.state;
                
                ((CreativeQuadLighter) lighter).setState(state);
                ((CreativeQuadLighter) lighter).setCustomTint(cube.color);
                
                if (OptifineHelper.isShaders()) {
                    if (state.getBlock() instanceof IFakeRenderingBlock)
                        state = ((IFakeRenderingBlock) state.getBlock()).getFakeState(state);
                    OptifineHelper.pushBuffer(state, pos, data.be.getLevel(), builder);
                }
                
                for (int h = 0; h < Facing.VALUES.length; h++) {
                    Facing facing = Facing.VALUES[h];
                    Object quadObject = cube.getQuad(facing);
                    List<BakedQuad> quads = null;
                    if (quadObject instanceof List) {
                        quads = (List<BakedQuad>) quadObject;
                    } else if (quadObject instanceof BakedQuad quad) {
                        bakedQuadWrapper.setElement(quad);
                        quads = bakedQuadWrapper;
                    }
                    if (quads != null && !quads.isEmpty())
                        for (BakedQuad quad : quads)
                            lighter.process(builder, pose.last(), quad, overlay);
                }
                
                bakedQuadWrapper.setElement(null);
                
                if (OptifineHelper.isShaders())
                    OptifineHelper.popBuffer(builder);
                
                if (!LittleTiles.CONFIG.rendering.useQuadCache)
                    cube.deleteQuadCache();
            }
            
            if (OptifineHelper.isShaders())
                OptifineHelper.calcNormalChunkLayer(builder);
            
            buffers.put(entry.key, new ByteBufferHolder(builder.end(), indexes.toIntArray()));
        }
        
        ((CreativeQuadLighter) lighter).setCustomTint(-1);
        lighter.reset();
    }
    
}
