package team.creative.littletiles.client.render.item;

import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.model.CreativeItemModel;
import team.creative.littletiles.api.common.tool.ILittlePlacer;

@OnlyIn(Dist.CLIENT)
public class LittleModelItemBackground extends CreativeItemModel {
    
    private final Function<ItemStack, ItemStack> top;
    
    public LittleModelItemBackground(ModelResourceLocation location, Function<ItemStack, ItemStack> top) {
        super(location);
        this.top = top;
    }
    
    protected ItemStack getFakeStack(ItemStack current) {
        return top.apply(current);
    }
    
    public boolean shouldRenderFake(ItemStack stack) {
        return ((ILittlePlacer) stack.getItem()).hasTiles(stack);
    }
    
    @Override
    public void applyCustomOpenGLHackery(PoseStack pose, ItemStack stack, ItemDisplayContext cameraTransformType) {
        if (cameraTransformType == ItemDisplayContext.GUI || shouldRenderFake(stack)) {
            
            if (cameraTransformType == ItemDisplayContext.GUI)
                RenderSystem.disableDepthTest();
            
            pose.pushPose();
            
            ItemStack toFake = getFakeStack(stack);
            Minecraft mc = Minecraft.getInstance();
            BakedModel model = mc.getItemRenderer().getModel(toFake, null, null, 0);
            
            prepareRenderer(cameraTransformType, pose);
            
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            mc.getItemRenderer().render(toFake, cameraTransformType, false, pose, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);
            multibuffersource$buffersource.endBatch();
            
            if (cameraTransformType == ItemDisplayContext.GUI)
                RenderSystem.enableDepthTest();
            
            pose.popPose();
        }
    }
    
    public void prepareRenderer(ItemDisplayContext context, PoseStack pose) {
        if (context == ItemDisplayContext.GUI)
            pose.translate(0, 0, 100);
    }
    
}
