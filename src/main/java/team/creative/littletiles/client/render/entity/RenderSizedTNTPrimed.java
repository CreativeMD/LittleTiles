package team.creative.littletiles.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.world.entity.item.PrimedTnt;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;

public class RenderSizedTNTPrimed extends TntRenderer {
    
    public RenderSizedTNTPrimed(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(PrimedTnt entity, float p_116178_, float p_116179_, PoseStack pose, MultiBufferSource buffer, int p_116182_) {
        pose.pushPose();
        LittleVec size = ((PrimedSizedTnt) entity).size;
        LittleGrid grid = ((PrimedSizedTnt) entity).grid;
        pose.scale((float) size.getPosX(grid), (float) size.getPosY(grid), (float) size.getPosZ(grid));
        super.render(entity, p_116178_, p_116179_, pose, buffer, p_116182_);
        pose.popPose();
    }
    
}
