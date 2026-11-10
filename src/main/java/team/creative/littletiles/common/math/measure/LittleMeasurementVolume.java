package team.creative.littletiles.common.math.measure;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.math.box.LittleBoxLevel;

public class LittleMeasurementVolume extends LittleMeasurementBox {
    
    public LittleMeasurementVolume(Level level, CompoundTag nbt) throws LittleActionException {
        super(level, nbt);
    }
    
    public LittleMeasurementVolume(List<LittleBoxLevel> positions) {
        super(positions);
    }
    
    @Override
    public void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        float partialTickTime = renderer.partialTickTime();
        var bb = box.toAABB();
        
        if (!renderer.isVisible(box.context.toRealWorld(bb, partialTickTime)))
            return;
        
        var center = bb.getCenter();
        
        var matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();
        var pos = box.pos;
        first.context.transformMatrix(matrix, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime);
        RenderSystem.applyModelViewMatrix();
        
        cam = box.context.toFakeWorld(cam, partialTickTime);
        cam = cam.subtract(cam.x - pos.getX(), cam.y - pos.getY(), cam.z - pos.getZ());
        overlay.renderLabel(cam, new Vec3d(center), displayVolume(bb.getXsize() * bb.getYsize() * bb.getZsize()), ColorUtils.WHITE);
        matrix.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
}
