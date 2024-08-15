package team.creative.littletiles.client.render.entity;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LittleSubLevel;

@OnlyIn(Dist.CLIENT)
public abstract class LittleEntityRenderManager<T extends LittleEntity> {
    
    public static final Minecraft mc = Minecraft.getInstance();
    
    public Boolean isInSight;
    public boolean needsFullRenderChunkUpdate = false;
    
    protected final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    
    private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
    private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
    private int ticks;
    
    public final T entity;
    
    public LittleEntityRenderManager(T entity) {
        this.entity = entity;
    }
    
    public LittleSubLevel getLevel() {
        return entity.getSubLevel();
    }
    
    public abstract RenderChunkExtender getRenderChunk(long pos);
    
    public abstract boolean isSmall();
    
    public void setupRender(Camera camera, @Nullable Frustum frustum, boolean capturedFrustum, boolean spectator) {
        Vec3 cam = camera.getPosition();
        if (frustum != null)
            isInSight = LittleEntityRenderer.isVisible(entity, frustum, cam.x, cam.y, cam.z); // needs to original camera position
        else
            isInSight = true;
    }
    
    public void unload() {
        globalBlockEntities.clear();
    }
    
    public void allChanged() {
        synchronized (this.globalBlockEntities) {
            this.globalBlockEntities.clear();
        }
    }
    
    public Iterable<Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>>> getDestructions() {
        return destructionProgress.long2ObjectEntrySet();
    }
    
    public SortedSet<BlockDestructionProgress> getDestructionProgress(BlockPos pos) {
        return destructionProgress.get(pos.asLong());
    }
    
    public void clientTick() {
        this.ticks++;
        if (this.ticks % 20 == 0) {
            Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();
            
            while (iterator.hasNext()) {
                BlockDestructionProgress destruction = iterator.next();
                int i = destruction.getUpdatedRenderTick();
                if (this.ticks - i > 400) {
                    iterator.remove();
                    this.removeProgress(destruction);
                }
            }
            
        }
    }
    
    public void destroyBlockProgress(int id, BlockPos pos, int progress) {
        if (progress >= 0 && progress < 10) {
            BlockDestructionProgress destruction = this.destroyingBlocks.get(id);
            if (destruction != null)
                this.removeProgress(destruction);
            
            if (destruction == null || !destruction.getPos().equals(pos)) {
                destruction = new BlockDestructionProgress(id, pos);
                this.destroyingBlocks.put(id, destruction);
            }
            
            destruction.setProgress(progress);
            destruction.updateTick(this.ticks);
            this.destructionProgress.computeIfAbsent(destruction.getPos().asLong(), (x) -> Sets.newTreeSet()).add(destruction);
        } else {
            BlockDestructionProgress destruction = this.destroyingBlocks.remove(id);
            if (destruction != null)
                this.removeProgress(destruction);
        }
        
    }
    
    private void removeProgress(BlockDestructionProgress destruction) {
        long i = destruction.getPos().asLong();
        Set<BlockDestructionProgress> set = this.destructionProgress.get(i);
        set.remove(destruction);
        if (set.isEmpty())
            this.destructionProgress.remove(i);
    }
    
    public abstract void compileSections(Camera camera);
    
    protected MultiBufferSource prepareBlockEntity(PoseStack pose, LittleSubLevel level, BlockPos pos, MultiBufferSource bufferSource) {
        SortedSet<BlockDestructionProgress> sortedset = getDestructionProgress(pos);
        MultiBufferSource newSource = bufferSource;
        if (sortedset != null && !sortedset.isEmpty()) {
            int j1 = sortedset.last().getProgress();
            if (j1 >= 0) {
                VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(mc.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(j1)), pose
                        .last(), 1.0F);
                
                newSource = (type) -> type.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, bufferSource.getBuffer(type)) : bufferSource.getBuffer(type);
            }
        }
        return newSource;
    }
    
    protected abstract void renderAllBlockEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource);
    
    protected void renderBlockEntity(BlockEntity blockentity, PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        if (ClientHooks.isBlockEntityRendererVisible(mc.getBlockEntityRenderDispatcher(), blockentity, frustum))
            return;
        BlockPos blockpos4 = blockentity.getBlockPos();
        pose.pushPose();
        pose.translate(blockpos4.getX() - cam.x, blockpos4.getY() - cam.y, blockpos4.getZ() - cam.z);
        mc.getBlockEntityRenderDispatcher().render(blockentity, frameTime, pose, prepareBlockEntity(pose, getLevel(), blockpos4, bufferSource));
        pose.popPose();
    }
    
    public void renderBlockEntitiesAndDestruction(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        pose.pushPose();
        entity.getOrigin().setupRendering(pose, cam.x, cam.y, cam.z, frameTime);
        
        renderAllBlockEntities(pose, frustum, cam, frameTime, bufferSource);
        
        LittleSubLevel level = entity.getSubLevel();
        for (Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> entry : getDestructions()) {
            BlockPos blockpos2 = BlockPos.of(entry.getLongKey());
            double d3 = blockpos2.getX() - cam.x;
            double d4 = blockpos2.getY() - cam.y;
            double d5 = blockpos2.getZ() - cam.z;
            if (!(d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)) {
                SortedSet<BlockDestructionProgress> sortedset1 = entry.getValue();
                if (sortedset1 != null && !sortedset1.isEmpty()) {
                    int k1 = sortedset1.last().getProgress();
                    pose.pushPose();
                    pose.translate(blockpos2.getX() - cam.x, blockpos2.getY() - cam.y, blockpos2.getZ() - cam.z);
                    VertexConsumer consumer = new SheetedDecalTextureGenerator(mc.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(k1)), pose
                            .last(), 1.0F);
                    ModelDataManager manager = level.getModelDataManager();
                    ModelData modelData = null;
                    if (manager != null)
                        modelData = manager.getAt(blockpos2);
                    mc.getBlockRenderer().renderBreakingTexture(level.getBlockState(blockpos2), blockpos2, level, pose, consumer, modelData == null ? ModelData.EMPTY : modelData);
                    pose.popPose();
                }
            }
        }
        pose.popPose();
    }
    
    public void renderGlobalEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        synchronized (this.globalBlockEntities) {
            if (globalBlockEntities.isEmpty())
                return;
            
            pose.pushPose();
            entity.getOrigin().setupRendering(pose, cam.x, cam.y, cam.z, frameTime);
            
            for (BlockEntity blockEntity : globalBlockEntities)
                renderBlockEntity(blockEntity, pose, frustum, cam, frameTime, bufferSource);
            pose.popPose();
        }
    }
    
    public abstract void resortTransparency(RenderType layer, double x, double y, double z);
    
    public abstract void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, Uniform offset);
    
    public void blockChanged(BlockGetter level, BlockPos pos, BlockState actualState, BlockState setState, int updateType) {
        this.setBlockDirty(pos, (updateType & 8) != 0);
    }
    
    protected void setBlockDirty(BlockPos pos, boolean playerChanged) {
        for (int i = pos.getZ() - 1; i <= pos.getZ() + 1; ++i)
            for (int j = pos.getX() - 1; j <= pos.getX() + 1; ++j)
                for (int k = pos.getY() - 1; k <= pos.getY() + 1; ++k)
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), playerChanged);
    }
    
    public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int i = minZ - 1; i <= maxZ + 1; ++i)
            for (int j = minX - 1; j <= maxX + 1; ++j)
                for (int k = minY - 1; k <= maxY + 1; ++k)
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i));
                
    }
    
    public void setBlockDirty(BlockPos pos, BlockState actualState, BlockState setState) {
        if (mc.getModelManager().requiresRender(actualState, setState))
            this.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }
    
    public void setSectionDirtyWithNeighbors(int x, int y, int z) {
        this.setSectionDirty(x - 1, y - 1, z - 1);
        this.setSectionDirty(x - 1, y, z - 1);
        this.setSectionDirty(x - 1, y + 1, z - 1);
        
        this.setSectionDirty(x, y - 1, z - 1);
        this.setSectionDirty(x, y, z - 1);
        this.setSectionDirty(x, y + 1, z - 1);
        
        this.setSectionDirty(x + 1, y - 1, z - 1);
        this.setSectionDirty(x + 1, y, z - 1);
        this.setSectionDirty(x + 1, y + 1, z - 1);
        
        this.setSectionDirty(x - 1, y - 1, z);
        this.setSectionDirty(x - 1, y, z);
        this.setSectionDirty(x - 1, y + 1, z);
        
        this.setSectionDirty(x, y - 1, z);
        this.setSectionDirty(x, y, z);
        this.setSectionDirty(x, y + 1, z);
        
        this.setSectionDirty(x + 1, y - 1, z);
        this.setSectionDirty(x + 1, y, z);
        this.setSectionDirty(x + 1, y + 1, z);
        
        this.setSectionDirty(x - 1, y - 1, z + 1);
        this.setSectionDirty(x - 1, y, z + 1);
        this.setSectionDirty(x - 1, y + 1, z + 1);
        
        this.setSectionDirty(x, y - 1, z + -1);
        this.setSectionDirty(x, y, z + 1);
        this.setSectionDirty(x, y + 1, z + 1);
        
        this.setSectionDirty(x + 1, y - 1, z + 1);
        this.setSectionDirty(x + 1, y, z + 1);
        this.setSectionDirty(x + 1, y + 1, z + 1);
    }
    
    public void setSectionDirty(int x, int y, int z) {
        this.setSectionDirty(x, y, z, false);
    }
    
    protected abstract void setSectionDirty(int x, int y, int z, boolean playerChanged);
}
