package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeBakedModel;
import team.creative.littletiles.common.block.little.element.LittleElement;

public class LittleRenderToolPreview extends LittleRenderToolBackground {
    
    private final Function<ItemStack, LittleElement> func;
    
    public LittleRenderToolPreview(ResourceLocation location, Function<ItemStack, LittleElement> function) {
        super(location);
        this.func = function;
    }
    
    @Override
    public List<? extends RenderBox> getBoxes(ItemStack stack, RenderType layer) {
        return Collections.EMPTY_LIST;
    }
    
    public boolean shouldRenderPreview(ItemStack stack) {
        return true;
    }
    
    @Override
    public void applyCustomOpenGLHackery(PoseStack pose, ItemStack stack, TransformType cameraTransformType) {
        if (!shouldRenderPreview(stack)) {
            super.applyCustomOpenGLHackery(pose, stack, cameraTransformType);
            return;
        }
        pose.pushPose();
        ForgeHooksClient
                .handleCameraTransforms(pose, model, cameraTransformType, cameraTransformType == TransformType.FIRST_PERSON_LEFT_HAND || cameraTransformType == TransformType.THIRD_PERSON_LEFT_HAND);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        mc.getItemRenderer().render(getFakeStack(), cameraTransformType, false, pose, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);
        multibuffersource$buffersource.endBatch();
        pose.popPose();
        
        if (cameraTransformType == TransformType.GUI) {
            pose.pushPose();
            pose.translate(0.1, 0.1, 0);
            pose.scale(0.7F, 0.7F, 0.7F);
            
            RenderSystem.disableDepthTest();
            
            LittleElement element = func.apply(stack);
            ItemStack blockStack = new ItemStack(element.getState().getBlock());
            BakedModel model = mc.getItemRenderer().getItemModelShaper().getItemModel(blockStack);
            if (!(model instanceof CreativeBakedModel))
                ForgeHooksClient.handleCameraTransforms(pose, model, cameraTransformType, false);
            
            // TODO IMPLEMENT COLOR
            mc.getItemRenderer().render(getFakeStack(), cameraTransformType, false, pose, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);
            multibuffersource$buffersource.endBatch();
            
            RenderSystem.enableDepthTest();
            pose.popPose();
        }
    }
    
}
