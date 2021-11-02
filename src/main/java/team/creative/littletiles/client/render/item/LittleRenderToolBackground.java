package team.creative.littletiles.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.ForgeHooksClient;
import team.creative.littletiles.common.api.tool.ILittlePlacer;

public class LittleRenderToolBackground extends LittleRenderTool {
    
    private ItemStack stack;
    public final ResourceLocation location;
    public BakedModel model;
    
    public LittleRenderToolBackground(ResourceLocation location) {
        this.location = location;
    }
    
    public void load() {
        model = mc.getItemRenderer().getItemModelShaper().getModelManager().getModel(new ModelResourceLocation(location, "inventory"));
    }
    
    protected ItemStack getFakeStack() {
        if (stack == null)
            stack = new ItemStack(Items.PAPER);
        return stack;
    }
    
    @Override
    public void applyCustomOpenGLHackery(PoseStack pose, ItemStack stack, TransformType cameraTransformType) {
        ILittlePlacer placer = (ILittlePlacer) stack.getItem();
        
        if (cameraTransformType == TransformType.GUI || placer.hasTiles(stack)) {
            if (cameraTransformType == TransformType.GUI)
                RenderSystem.disableDepthTest();
            if (model == null)
                load();
            
            pose.pushPose();
            ForgeHooksClient.handleCameraTransforms(pose, model, cameraTransformType, false);
            
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            mc.getItemRenderer().render(getFakeStack(), cameraTransformType, false, pose, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);
            multibuffersource$buffersource.endBatch();
            
            if (cameraTransformType == TransformType.GUI)
                RenderSystem.enableDepthTest();
            
            pose.popPose();
        }
    }
    
    @Override
    public void reload() {
        model = null;
    }
    
}
