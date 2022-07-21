package team.creative.littletiles.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import team.creative.littletiles.common.entity.LittleLevelEntity;

public class LittleLevelEntityRenderer extends EntityRenderer<LittleLevelEntity> {
    
    public static Minecraft mc = Minecraft.getInstance();
    public static LittleLevelEntityRenderer INSTANCE;
    
    public LittleLevelEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        INSTANCE = this;
    }
    
    @Override
    public boolean shouldRender(LittleLevelEntity animation, Frustum frustum, double camX, double camY, double camZ) {
        return animation.shouldRender(camX, camY, camZ) && frustum.isVisible(animation.getBoundingBox().inflate(0.5D));
    }
    
    @Override
    public void render(LittleLevelEntity animation, float p_114486_, float p_114487_, PoseStack pose, MultiBufferSource buffer, int p_114490_) {
        super.render(animation, p_114486_, p_114487_, pose, buffer, p_114490_);
        
        // TODO Render animations
    }
    
    @Override
    public ResourceLocation getTextureLocation(LittleLevelEntity animation) {
        return InventoryMenu.BLOCK_ATLAS;
    }
    
    public static BlockPos getRenderChunkPos(BlockPos blockPos) {
        return new BlockPos(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4);
    }
}
