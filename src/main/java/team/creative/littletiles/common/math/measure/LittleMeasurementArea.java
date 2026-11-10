package team.creative.littletiles.common.math.measure;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.math.box.LittleBoxLevel;

public class LittleMeasurementArea extends LittleMeasurementBox {
    
    public LittleMeasurementArea(Level level, CompoundTag nbt) throws LittleActionException {
        super(level, nbt);
    }
    
    public LittleMeasurementArea(List<LittleBoxLevel> positions) {
        super(positions);
    }
    
    @Override
    public void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        float partialTickTime = renderer.partialTickTime();
        var bb = box.toAABB();
        
        if (!renderer.isVisible(box.context.toRealWorld(bb, partialTickTime)))
            return;
        
        var matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();
        var pos = first.pos;
        first.context.transformMatrix(matrix, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime);
        RenderSystem.applyModelViewMatrix();
        
        var center = bb.getCenter();
        var camFake = box.context.toFakeWorld(cam, partialTickTime);
        cam = camFake.subtract(camFake.x - pos.getX(), camFake.y - pos.getY(), camFake.z - pos.getZ());
        
        if (camFake.x > bb.maxX)
            displayFace(renderer, overlay, cam, bb, center, Facing.EAST);
        else if (camFake.x < bb.minX)
            displayFace(renderer, overlay, cam, bb, center, Facing.WEST);
        
        if (camFake.y > bb.maxY)
            displayFace(renderer, overlay, cam, bb, center, Facing.UP);
        else if (camFake.y < bb.minY)
            displayFace(renderer, overlay, cam, bb, center, Facing.DOWN);
        
        if (camFake.z > bb.maxZ)
            displayFace(renderer, overlay, cam, bb, center, Facing.SOUTH);
        else if (camFake.z < bb.minZ)
            displayFace(renderer, overlay, cam, bb, center, Facing.NORTH);
        
        matrix.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
    
    private void displayFace(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam, AABB bb, Vec3 center, Facing facing) {
        Vec3d vec = new Vec3d(center);
        vec.set(facing.axis, facing.get(bb));
        double area = facing.axis == Axis.X ? bb.getYsize() * bb.getZsize() : (facing.axis == Axis.Y ? bb.getXsize() * bb.getZsize() : bb.getXsize() * bb.getYsize());
        overlay.renderLabel(cam, vec, displayArea(area), ColorUtils.WHITE);
    }
    
}
