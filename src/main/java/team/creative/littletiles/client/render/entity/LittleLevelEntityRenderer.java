package team.creative.littletiles.client.render.entity;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.littletiles.common.entity.LittleLevelEntity;

public class LittleLevelEntityRenderer extends EntityRenderer<LittleLevelEntity> {
    
    public static Minecraft mc = Minecraft.getInstance();
    public static LittleLevelEntityRenderer INSTANCE;
    
    public static RenderType getLayerByStage(RenderLevelStageEvent.Stage stage) {
        if (stage == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS)
            return RenderType.solid();
        if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)
            return RenderType.cutoutMipped();
        if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)
            return RenderType.cutout();
        if (stage == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return RenderType.translucent();
        if (stage == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS)
            return RenderType.tripwire();
        throw new IllegalArgumentException("Unexpected value: " + stage);
    }
    
    public LittleLevelEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        INSTANCE = this;
    }
    
    @Override
    public boolean shouldRender(LittleLevelEntity animation, Frustum frustum, double camX, double camY, double camZ) {
        if (animation.renderManager.isInSight == null)
            animation.renderManager.isInSight = animation.shouldRender(camX, camY, camZ) && frustum.isVisible(animation.getRealBB().inflate(0.5D));
        return animation.renderManager.isInSight;
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
    
    public static BlockPos getRenderChunkPos(BlockPos blockPos) {
        return new BlockPos(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4);
    }
    
    public void renderBlockEntities(PoseStack pose, LittleLevelEntity animation, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource.BufferSource bufferSource) {
        for (LittleRenderChunk chunk : animation.renderManager) {
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
    
    public void resortTransparency(LittleLevelEntity animation,RenderType layer, double x, double y, double z) {
        for (LittleRenderChunk chunk : animation.renderManager)
            if (check distance < 15 && chunk.resortTransparency(layer))
    }
    
    public void renderChunkLayer(LittleLevelEntity animation, RenderLevelStageEvent event) {
        RenderType layer = getLayerByStage(event.getStage());
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        
        ShaderInstance shaderinstance = RenderSystem.getShader();
        Uniform uniform = shaderinstance.CHUNK_OFFSET;
        
        while (true) {
            if (flag) {
                if (!objectlistiterator.hasNext()) {
                    break;
                }
            } else if (!objectlistiterator.hasPrevious()) {
                break;
            }
            
            LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo1 = flag ? objectlistiterator.next() : objectlistiterator.previous();
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo1.chunk;
            if (!chunkrenderdispatcher$renderchunk.getCompiledChunk().isEmpty(layer)) {
                VertexBuffer vertexbuffer = chunkrenderdispatcher$renderchunk.getBuffer(layer);
                BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
                if (uniform != null) {
                    uniform.set((float) (blockpos.getX() - cam.x), (float) (blockpos.getY() - cam.y), (float) (blockpos.getZ() - cam.z));
                    uniform.upload();
                }
                
                vertexbuffer.bind();
                vertexbuffer.draw();
            }
        }
        
        if (uniform != null)
            uniform.set(Vector3f.ZERO);
    }
    
}
