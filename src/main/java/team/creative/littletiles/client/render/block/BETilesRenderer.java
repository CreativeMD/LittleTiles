package team.creative.littletiles.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;

public class BETilesRenderer implements BlockEntityRenderer<BETiles> {
    
    @Override
    public boolean shouldRender(BETiles be, Vec3 vec) {
        return Vec3.atCenterOf(be.getBlockPos()).closerThan(vec, be.render.getMaxRenderDistance());
    }
    
    @Override
    public boolean shouldRenderOffScreen(BETiles be) {
        AABB box = be.render.getRenderBoundingBox();
        if (box.maxX - box.minX > 16)
            return true;
        if (box.maxY - box.minY > 16)
            return true;
        if (box.maxZ - box.minZ > 16)
            return true;
        return false;
    }
    
    @Override
    public void render(BETiles be, float partialTickTime, PoseStack pose, MultiBufferSource buffer, int destroyStage, int p_112312_) {
        for (LittleStructure structure : be.loadedStructures(LittleStructureAttribute.TICK_RENDERING))
            structure.renderTick(pose, be.getBlockPos(), partialTickTime);
    }
}
