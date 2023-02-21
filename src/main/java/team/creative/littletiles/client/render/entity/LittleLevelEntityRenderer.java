package team.creative.littletiles.client.render.entity;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.ForgeConfig;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.client.render.level.LittleRenderChunk;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;

public class LittleLevelEntityRenderer extends EntityRenderer<LittleLevelEntity> {
    
    public static Minecraft mc = Minecraft.getInstance();
    public static LittleLevelEntityRenderer INSTANCE;
    
    public LittleLevelEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        INSTANCE = this;
    }
    
    @Override
    public boolean shouldRender(LittleLevelEntity animation, Frustum frustum, double camX, double camY, double camZ) {
        if (!animation.hasLoaded())
            return false;
        if (animation.getRenderManager().isInSight == null)
            animation.getRenderManager().isInSight = animation.shouldRender(camX, camY, camZ) && frustum.isVisible(animation.getRealBB().inflate(0.5D));
        return animation.getRenderManager().isInSight;
    }
    
    @Override
    public ResourceLocation getTextureLocation(LittleLevelEntity animation) {
        return InventoryMenu.BLOCK_ATLAS;
    }
    
    public void compileChunks(LittleLevelEntity animation) {
        mc.getProfiler().push("compile_animation_chunks");
        LittleLevelRenderManager manager = animation.getRenderManager();
        List<LittleRenderChunk> schedule = Lists.newArrayList();
        
        Level level = (Level) animation.getSubLevel();
        
        for (LittleRenderChunk chunk : manager) {
            ChunkPos chunkpos = new ChunkPos(chunk.pos);
            if (chunk.isDirty() && (!animation.isReal() || level.getChunk(chunkpos.x, chunkpos.z).isClientLightReady())) {
                boolean immediate = false;
                if (mc.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED)
                    immediate = chunk.isDirtyFromPlayer();
                else if (mc.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
                    immediate = !ForgeConfig.CLIENT.alwaysSetupTerrainOffThread
                            .get() && (chunk.pos.offset(8, 8, 8).distSqr(manager.getCameraBlockPos()) < 768.0D || chunk.isDirtyFromPlayer()); // the target is the else block below, so invert the forge addition to get there early
                }
                
                if (immediate) {
                    chunk.compile();
                    chunk.setNotDirty();
                } else
                    schedule.add(chunk);
            }
        }
        
        for (LittleRenderChunk chunk : schedule) {
            chunk.compileASync();
            chunk.setNotDirty();
        }
        
        mc.getProfiler().pop();
    }
    
    public MultiBufferSource prepareBlockEntity(PoseStack pose, LittleClientLevel level, BlockPos pos, MultiBufferSource bufferSource) {
        SortedSet<BlockDestructionProgress> sortedset = level.renderManager.getDestructionProgress(pos);
        MultiBufferSource newSource = bufferSource;
        if (sortedset != null && !sortedset.isEmpty()) {
            int j1 = sortedset.last().getProgress();
            if (j1 >= 0) {
                PoseStack.Pose posestack$pose1 = pose.last();
                VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(mc.renderBuffers().crumblingBufferSource()
                        .getBuffer(ModelBakery.DESTROY_TYPES.get(j1)), posestack$pose1.pose(), posestack$pose1.normal(), 1.0F);
                
                newSource = (type) -> type.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, bufferSource.getBuffer(type)) : bufferSource.getBuffer(type);
            }
        }
        return newSource;
    }
    
    public void renderBlockEntitiesAndDestruction(PoseStack pose, LittleLevelEntity animation, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        pose.pushPose();
        animation.getOrigin().setupRendering(pose, animation, frameTime);
        LittleClientLevel level = (LittleClientLevel) animation.getSubLevel();
        
        for (LittleRenderChunk chunk : animation.getRenderManager()) {
            List<BlockEntity> list = chunk.getCompiledChunk().getRenderableBlockEntities();
            if (list.isEmpty())
                continue;
            
            for (BlockEntity blockentity : list) {
                if (!frustum.isVisible(blockentity.getRenderBoundingBox()))
                    continue;
                BlockPos blockpos4 = blockentity.getBlockPos();
                pose.pushPose();
                pose.translate(blockpos4.getX() - cam.x, blockpos4.getY() - cam.y, blockpos4.getZ() - cam.z);
                mc.getBlockEntityRenderDispatcher()
                        .render(blockentity, frameTime, pose, prepareBlockEntity(pose, (LittleClientLevel) animation.getSubLevel(), blockpos4, bufferSource));
                pose.popPose();
            }
        }
        
        for (Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> entry : animation.getRenderManager().getDestructions()) {
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
                    PoseStack.Pose last = pose.last();
                    VertexConsumer consumer = new SheetedDecalTextureGenerator(mc.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(k1)), last
                            .pose(), last.normal(), 1.0F);
                    ModelData modelData = level.getModelDataManager().getAt(blockpos2);
                    mc.getBlockRenderer().renderBreakingTexture(level.getBlockState(blockpos2), blockpos2, level, pose, consumer, modelData == null ? ModelData.EMPTY : modelData);
                    pose.popPose();
                }
            }
        }
        pose.popPose();
    }
    
    public void resortTransparency(LittleLevelEntity animation, RenderType layer, double x, double y, double z) {
        int i = 0;
        for (LittleRenderChunk chunk : animation.getRenderManager().visibleChunks()) {
            if (i > 14)
                return;
            if (chunk.resortTransparency(layer))
                i++;
        }
    }
    
    public void renderChunkLayer(LittleLevelEntity animation, RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix) {
        LittleLevelRenderManager manager = animation.getRenderManager();
        
        ShaderInstance shaderinstance = RenderSystem.getShader();
        Uniform uniform = shaderinstance.CHUNK_OFFSET;
        
        for (Iterator<LittleRenderChunk> iterator = layer == RenderType.translucent() ? manager.visibleChunksInverse() : manager.visibleChunks().iterator(); iterator.hasNext();) {
            LittleRenderChunk chunk = iterator.next();
            if (!chunk.getCompiledChunk().isEmpty(layer)) {
                VertexBuffer vertexbuffer = chunk.getVertexBuffer(layer);
                if (uniform != null) {
                    uniform.set((float) (chunk.pos.getX() - x), (float) (chunk.pos.getY() - y), (float) (chunk.pos.getZ() - z));
                    uniform.upload();
                }
                
                vertexbuffer.bind();
                vertexbuffer.draw();
            }
        }
        
        if (uniform != null)
            uniform.set(0F, 0F, 0F);
    }
    
}
