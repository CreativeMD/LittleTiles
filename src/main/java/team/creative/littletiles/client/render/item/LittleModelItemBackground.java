package team.creative.littletiles.client.render.item;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.model.CreativeItemModel;
import team.creative.littletiles.api.common.tool.ILittlePlacer;

@OnlyIn(Dist.CLIENT)
public class LittleModelItemBackground extends CreativeItemModel {
    
    public final Supplier<ItemStack> top;
    protected ItemStack stack;
    
    public LittleModelItemBackground(ModelResourceLocation location, Supplier<ItemStack> top) {
        super(location);
        this.top = top;
    }
    
    protected ItemStack getFakeStack(ItemStack current) {
        if (stack == null)
            stack = top.get();
        stack.setTag(current.getTag());
        return stack;
    }
    
    public boolean shouldRenderFake(ItemStack stack) {
        return ((ILittlePlacer) stack.getItem()).hasTiles(stack);
    }
    
    @Override
    public void applyCustomOpenGLHackery(PoseStack pose, ItemStack stack, TransformType cameraTransformType) {
        if (cameraTransformType == TransformType.GUI || shouldRenderFake(stack)) {
            
            if (cameraTransformType == TransformType.GUI)
                RenderSystem.disableDepthTest();
            
            pose.pushPose();
            
            ItemStack toFake = getFakeStack(stack);
            Minecraft mc = Minecraft.getInstance();
            BakedModel model = mc.getItemRenderer().getModel(toFake, null, null, 0);
            
            prepareRenderer(pose);
            
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            mc.getItemRenderer().render(toFake, cameraTransformType, false, pose, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);
            multibuffersource$buffersource.endBatch();
            
            if (cameraTransformType == TransformType.GUI)
                RenderSystem.enableDepthTest();
            
            pose.popPose();
        }
    }
    
    public void prepareRenderer(PoseStack pose) {}
    
}
