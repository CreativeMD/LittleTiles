package team.creative.littletiles.client.render.entity;

import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfig;
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
    public void render(LittleLevelEntity animation, float p_114486_, float p_114487_, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        super.render(animation, p_114486_, p_114487_, pose, buffer, packedLight);
        
        // TODO Render entities (not sub animations)
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
    
    public void renderBlockEntities(PoseStack pose, LittleLevelEntity animation, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource.BufferSource bufferSource) {
        for (LittleRenderChunk chunk : animation.getRenderManager()) {
            List<BlockEntity> list = chunk.getCompiledChunk().getRenderableBlockEntities();
            if (list.isEmpty())
                continue;
            
            for (BlockEntity blockentity : list) {
                if (!frustum.isVisible(blockentity.getRenderBoundingBox()))
                    continue;
                BlockPos blockpos4 = blockentity.getBlockPos();
                MultiBufferSource multibuffersource1 = bufferSource;
                pose.pushPose();
                pose.translate(blockpos4.getX() - cam.x, blockpos4.getY() - cam.y, blockpos4.getZ() - cam.z);
                mc.getBlockEntityRenderDispatcher().render(blockentity, frameTime, pose, multibuffersource1);
                pose.popPose();
            }
        }
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
