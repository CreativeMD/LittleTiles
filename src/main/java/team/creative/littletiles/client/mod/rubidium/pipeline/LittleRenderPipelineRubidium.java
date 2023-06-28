package team.creative.littletiles.client.mod.rubidium.pipeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.jellysquid.mods.sodium.client.model.IndexBufferBuilder;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorSampler;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.texture.SpriteExtended;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import me.jellysquid.mods.sodium.client.util.color.ColorARGB;
import me.jellysquid.mods.sodium.client.world.biome.BlockColorsExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.client.IFakeRenderingBlock;
import team.creative.littletiles.client.mod.oculus.OculusManager;
import team.creative.littletiles.client.mod.rubidium.buffer.RubidiumByteBufferHolder;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.mixin.rubidium.BlockRenderCacheAccessor;
import team.creative.littletiles.mixin.rubidium.BlockRenderContextAccessor;
import team.creative.littletiles.mixin.rubidium.BlockRendererAccessor;
import team.creative.littletiles.mixin.rubidium.ChunkBuilderAccessor;
import team.creative.littletiles.mixin.rubidium.ChunkVertexBufferBuilderAccessor;
import team.creative.littletiles.mixin.rubidium.RenderSectionManagerAccessor;

public class LittleRenderPipelineRubidium extends LittleRenderPipeline {
    
    public static RenderChunkExtender getChunk(BlockPos pos) {
        return (RenderChunkExtender) ((RenderSectionManagerAccessor) SodiumWorldRenderer.instance().getRenderSectionManager())
                .callGetRenderSection(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
    
    private static final int WINDING_LENGTH = 6;
    private ChunkBuildBuffers buildBuffers;
    private BlockRenderer renderer;
    private LittleLightDataAccess lightAccess;
    private LightPipelineProvider lighters;
    private QuadLightData cachedQuadLightData = new QuadLightData();
    public BlockRenderContext context = new BlockRenderContext(null);
    private Set<TextureAtlasSprite> sprites = new ObjectOpenHashSet<>();
    private MutableBlockPos modelOffset = new MutableBlockPos();
    private IntArrayList indexes = new IntArrayList();
    private int[] faceCounters = new int[ModelQuadFacing.COUNT];
    private int[] colors = new int[4];
    private Vec3d cubeCenter = new Vec3d();
    
    @Override
    public void buildCache(PoseStack pose, ChunkLayerMap<BufferHolder> buffers, RenderingBlockContext data, VertexFormat format, SingletonList<BakedQuad> bakedQuadWrapper) {
        Level renderLevel = data.be.getLevel();
        while (renderLevel instanceof LittleSubLevel sub && !sub.shouldUseLightingForRenderig())
            renderLevel = sub.getParent();
        
        ((BlockRenderContextAccessor) context).setWorld(renderLevel);
        
        BlockPos pos = data.be.getBlockPos();
        
        lightAccess.prepare(renderLevel);
        
        LightPipeline lighter = lighters.getLighter(((BlockRendererAccessor) renderer)
                .getUseAmbientOcclusion() && data.state.getLightEmission(data.be.getLevel(), pos) == 0 ? LightMode.SMOOTH : LightMode.FLAT);
        
        BlockColorsExtended blockColors = ((BlockRendererAccessor) renderer).getBlockColors();
        data.chunk.prepareModelOffset(modelOffset, pos);
        
        // Render vertex buffer
        for (Tuple<RenderType, IndexedCollector<LittleRenderBox>> entry : data.be.render.boxCache.tuples()) {
            
            ChunkModelBuilder builder = buildBuffers.get(entry.getKey());
            ChunkVertexBufferBuilder vertexBuffer = builder.getVertexBuffer();
            
            IndexedCollector<LittleRenderBox> cubes = entry.value;
            if (cubes == null || cubes.isEmpty())
                continue;
            
            vertexBuffer.start(data.chunk.chunkId());
            
            for (int i = 0; i < ModelQuadFacing.VALUES.length; i++)
                builder.getIndexBuffer(ModelQuadFacing.VALUES[i]).start();
            
            Arrays.fill(faceCounters, 0);
            ChunkVertexBufferBuilderAccessor v = (ChunkVertexBufferBuilderAccessor) vertexBuffer;
            
            for (Iterator<LittleRenderBox> iterator = cubes.sectionIterator(x -> {
                indexes.add(x);
                ChunkVertexBufferBuilderAccessor a = (ChunkVertexBufferBuilderAccessor) builder.getVertexBuffer();
                indexes.add(a.getCount() * a.getStride());
                for (int i = 0; i < faceCounters.length; i++)
                    indexes.add(faceCounters[i]);
            });iterator.hasNext();) {
                LittleRenderBox cube = iterator.next();
                BlockState state = cube.state;
                
                context.update(pos, modelOffset, state, null, 0, ModelData.EMPTY, entry.key);
                OculusManager.setLocalPos(buildBuffers, pos);
                cubeCenter.set((cube.maxX + cube.minX) * 0.5, (cube.maxY + cube.minY) * 0.5, (cube.maxZ + cube.minZ) * 0.5);
                OculusManager.setMid(vertexBuffer, cubeCenter);
                
                ColorSampler<BlockState> colorizer = null;
                
                if (OculusManager.isShaders()) {
                    if (state.getBlock() instanceof IFakeRenderingBlock fake)
                        state = fake.getFakeState(state);
                    OculusManager.setMaterialId(buildBuffers, state);
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
                    if (quads != null && !quads.isEmpty()) {
                        Direction direction = facing.toVanilla();
                        ModelQuadFacing modelFacing = ModelQuadFacing.fromDirection(direction);
                        IndexBufferBuilder indexBuffer = builder.getIndexBuffer(modelFacing);
                        
                        for (BakedQuad quad : quads) {
                            
                            lighter.calculate((ModelQuadView) quad, pos, cachedQuadLightData, direction, quad.getDirection(), quad.isShade());
                            
                            if (cube.color != -1)
                                Arrays.fill(colors, ColorARGB.toABGR(cube.color));
                            else if (quad.isTinted()) {
                                if (colorizer == null)
                                    colorizer = blockColors.getColorProvider(state);
                                
                                colors = ((BlockRendererAccessor) renderer).getColorBlender().getColors(renderLevel, pos, (ModelQuadView) quad, colorizer, state);
                            } else
                                Arrays.fill(colors, -1);
                            
                            ((BlockRendererAccessor) renderer)
                                    .callWriteGeometry(context, vertexBuffer, indexBuffer, Vec3.ZERO, (ModelQuadView) quad, colors, cachedQuadLightData.br, cachedQuadLightData.lm);
                            TextureAtlasSprite sprite = quad.getSprite();
                            if (sprite != null && ((SpriteExtended) sprite.contents()).hasAnimation())
                                sprites.add(sprite);
                            
                            faceCounters[modelFacing.ordinal()] += WINDING_LENGTH;
                        }
                    }
                }
                
                bakedQuadWrapper.setElement(null);
                
                OculusManager.resetBlockContext(buildBuffers);
                
                if (!LittleTiles.CONFIG.rendering.useQuadCache)
                    cube.deleteQuadCache();
            }
            
            if (v.getCount() > 0) {
                ByteBuffer buffer = MemoryUtil.memRealloc((ByteBuffer) null, v.getStride() * v.getCount());
                ByteBuffer threadBuffer = v.getBuffer();
                threadBuffer.limit(buffer.capacity());
                MemoryUtil.memCopy(v.getBuffer(), buffer);
                threadBuffer.limit(threadBuffer.capacity());
                IntArrayList[] list = new IntArrayList[ModelQuadFacing.COUNT];
                for (int i = 0; i < list.length; i++)
                    list[i] = new IntArrayList(builder.getIndexBuffer(ModelQuadFacing.VALUES[i]).pop());
                buffers.put(entry.key, new RubidiumByteBufferHolder(buffer, v.getStride() * v.getCount(), v.getCount(), indexes
                        .toIntArray(), list, sprites.isEmpty() ? Collections.EMPTY_LIST : new ArrayList<>(sprites)));
                indexes.clear();
            }
            sprites.clear();
        }
        
    }
    
    @Override
    public void reload() {
        ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) SodiumWorldRenderer.instance().getRenderSectionManager().getBuilder();
        buildBuffers = new ChunkBuildBuffers(chunkBuilder.getVertexType(), chunkBuilder.getRenderPassManager());
        buildBuffers.init(null, 0);
        renderer = new BlockRenderer(Minecraft.getInstance(), lighters = new LightPipelineProvider(lightAccess = new LittleLightDataAccess()), BlockRenderCacheAccessor
                .callCreateBiomeColorBlender());
    }
    
    @Override
    public void release() {
        buildBuffers.destroy();
    }
}
