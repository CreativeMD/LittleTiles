package team.creative.littletiles.client.mod.sodium.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationRenderManagerSodium extends LittleEntityRenderManager<LittleAnimationEntity> {
    
    private Long2ObjectMap<LittleSodiumSection> sections = new Long2ObjectArrayMap<>();
    
    protected List<BlockEntity> renderableBlockEntities = new ArrayList<>();
    
    protected final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.CHUNK_BUFFER_LAYERS.size());
    protected boolean needsUpdate = false;
    
    private GlVertexAttributeBinding[] vertexAttributeBindings;
    private GlVertexFormat format;
    
    public LittleAnimationRenderManagerSodium(LittleAnimationEntity entity) {
        super(entity);
    }
    
    public void prepare(GlVertexAttributeBinding[] vertexAttributeBindings, GlVertexFormat format) {
        this.vertexAttributeBindings = vertexAttributeBindings;
        this.format = format;
    }
    
    public GlVertexAttributeBinding[] getBindings() {
        return vertexAttributeBindings;
    }
    
    public GlVertexFormat getFormat() {
        return format;
    }
    
    @Override
    public RenderChunkExtender getRenderChunk(long pos) {
        return getOrCreateSection(pos);
    }
    
    private LittleSodiumSection getOrCreateSection(long pos) {
        var section = sections.get(pos);
        if (section == null)
            sections.put(pos, section = new LittleSodiumSection(this, pos));
        return section;
    }
    
    @Override
    public void compileSections(Camera camera) {
        if (!needsUpdate || vertexAttributeBindings == null)
            return;
        
        needsUpdate = false;
        hasBlocks.clear();
        renderableBlockEntities.clear();
        RebuildTask rebuild = new RebuildTask();
        Vec3 cam = camera.getPosition();
        rebuild.compile((float) cam.x, (float) cam.y, (float) cam.z);
        globalBlockEntities.clear();
        globalBlockEntities.addAll(rebuild.globalBlockEntities);
        renderableBlockEntities = rebuild.blockEntities;
        
        LongSet original = new LongArraySet(sections.keySet());
        if (!rebuild.isEmpty()) {
            for (Entry<CompiledSodiumSection> entry : rebuild.compiledSections.long2ObjectEntrySet()) {
                original.remove(entry.getLongKey());
                entry.getValue().upload(hasBlocks);
            }
            VertexBuffer.unbind();
        }
        
        if (!original.isEmpty())
            for (LongIterator iterator = original.longIterator(); iterator.hasNext();) {
                long pos = iterator.nextLong();
                var s = sections.remove(pos);
                if (s != null)
                    s.unload();
            }
        
    }
    
    @Override
    public boolean isSmall() {
        return true;
    }
    
    @Override
    protected void renderAllBlockEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        if (renderableBlockEntities != null)
            for (BlockEntity blockEntity : renderableBlockEntities)
                renderBlockEntity(blockEntity, pose, frustum, cam, frameTime, bufferSource);
    }
    
    @Override
    public void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, Uniform offset) {
        throw new UnsupportedOperationException();
    }
    
    public void renderChunkLayerSodium(RenderType layer, PoseStack pose, double camx, double camy, double camz, Matrix4fc projectionMatrix, ChunkShaderInterface shader,
            CameraTransform camera) {
        if (hasBlocks.contains(layer))
            for (LittleSodiumSection s : sections.values())
                s.renderChunkLayerSodium(layer, shader, camera);
    }
    
    @Override
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public LittleAnimationLevel getLevel() {
        return (LittleAnimationLevel) super.getLevel();
    }
    
    public void setHasBlock(RenderType layer) {
        hasBlocks.add(layer);
    }
    
    @Override
    protected void setBlockDirty(BlockPos pos, boolean playerChanged) {
        needsUpdate = true;
    }
    
    @Override
    public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        needsUpdate = true;
        
    }
    
    @Override
    public void setBlockDirty(BlockPos pos, BlockState actualState, BlockState setState) {
        needsUpdate = true;
    }
    
    @Override
    protected void setSectionDirty(int x, int y, int z, boolean playerChanged) {
        needsUpdate = true;
    }
    
    @Override
    public void unload() {
        super.unload();
        for (LittleSodiumSection s : sections.values())
            s.unload();
    }
    
    @Override
    public void allChanged() {
        super.allChanged();
        
        for (LittleSodiumSection s : sections.values())
            s.unload();
        sections.clear();
        
        for (BETiles block : getLevel())
            block.render.sectionUpdate(SectionPos.asLong(block.getBlockPos()));
        
        needsUpdate = true;
        vertexAttributeBindings = null;
        format = null;
    }
    
    private class RebuildTask {
        
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        public final Long2ObjectMap<CompiledSodiumSection> compiledSections = new Long2ObjectArrayMap<>();
        private boolean hasRenderData = false;
        
        private void compile(float x, float y, float z) {
            
            for (BETiles block : getLevel())
                handleBlockEntity(block);
            
            for (CompiledSodiumSection s : compiledSections.values())
                if (s.finish())
                    hasRenderData = true;
        }
        
        private void handleBlockEntity(BETiles entity) {
            long pos = SectionPos.asLong(entity.getBlockPos());
            var c = getCompiled(pos);
            LittleRenderPipelineType.compileUploaded(pos, entity, x -> c.getOrCreateBuffers(x));
            BlockEntityRenderer blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
            if (blockentityrenderer != null)
                if (blockentityrenderer.shouldRenderOffScreen(entity))
                    globalBlockEntities.add(entity);
                else
                    blockEntities.add(entity); //FORGE: Fix MC-112730
        }
        
        public boolean isEmpty() {
            return !hasRenderData && globalBlockEntities.isEmpty() && blockEntities.isEmpty();
        }
        
        private CompiledSodiumSection getCompiled(long pos) {
            var c = compiledSections.get(pos);
            if (c == null)
                compiledSections.put(pos, c = new CompiledSodiumSection(getOrCreateSection(pos)));
            return c;
        }
        
    }
    
}
