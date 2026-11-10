package team.creative.littletiles.common.math.measure;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.LittleToolMeasure.RenderMeasurementResult;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.math.box.LittleBoxLevel;
import team.creative.littletiles.common.math.measure.LittleMeasurement.LittleMeasurementSimple;

public class LittleMeasurementBox extends LittleMeasurementSimple {
    
    protected LittleBoxLevel box;
    
    public LittleMeasurementBox(Level level, CompoundTag nbt) throws LittleActionException {
        super(level, nbt);
    }
    
    public LittleMeasurementBox(List<LittleBoxLevel> positions) {
        super(positions);
    }
    
    @Override
    public void changed() {
        super.changed();
        box = first.copy();
        if (!box.context.is(second.context))
            box.include(second.convertTo(box.context));
        else
            box.include(second);
    }
    
    @Override
    public void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        float partialTickTime = renderer.partialTickTime();
        var bb = box.toAABB();
        
        if (!renderer.isVisible(box.context.toRealWorld(bb, partialTickTime)))
            return;
        
        var matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();
        var pos = box.pos;
        box.context.transformMatrix(matrix, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime);
        RenderSystem.applyModelViewMatrix();
        
        cam = box.context.toFakeWorld(cam, partialTickTime);
        Facing x = cam.x < bb.maxX ? Facing.WEST : null;
        if (cam.x > bb.minX)
            x = x == null ? Facing.EAST : null;
        
        Facing y = cam.y < bb.maxY ? Facing.DOWN : null;
        if (cam.y > bb.minY)
            y = y == null ? Facing.UP : null;
        
        Facing z = cam.z < bb.maxZ ? Facing.NORTH : null;
        if (cam.z > bb.minZ)
            z = z == null ? Facing.SOUTH : null;
        
        cam = cam.subtract(cam.x - pos.getX(), cam.y - pos.getY(), cam.z - pos.getZ());
        
        displayLine(renderer, overlay, cam, bb, x, y, z);
        matrix.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
    
    @Override
    public RenderMeasurementResult build(PreviewRenderer renderer, PoseStack pose, BufferBuilder builder, Vec3 cam) {
        var renderBox = box.getRenderingBoxWithoutOffset();
        renderBox.color = color;
        renderer.buildBox(pose, renderBox, builder, 255, true);
        return new RenderMeasurementResult(box.context, box.pos, builder);
    }
    
    public static void displayLine(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam, AABB bb, Facing x, Facing y, Facing z) {
        if (x == null) {
            displayLine(renderer, overlay, cam, bb, Facing.WEST, y, z);
            displayLine(renderer, overlay, cam, bb, Facing.EAST, y, z);
            return;
        }
        
        if (y == null) {
            displayLine(renderer, overlay, cam, bb, x, Facing.DOWN, z);
            displayLine(renderer, overlay, cam, bb, x, Facing.UP, z);
            return;
        }
        
        if (z == null) {
            displayLine(renderer, overlay, cam, bb, x, y, Facing.NORTH);
            displayLine(renderer, overlay, cam, bb, x, y, Facing.SOUTH);
            return;
        }
        
        var corner = BoxCorner.getCorner(x, y, z);
        displayLine(renderer, overlay, cam, corner.get(bb), corner.mirror(Axis.X).get(bb));
        displayLine(renderer, overlay, cam, corner.get(bb), corner.mirror(Axis.Y).get(bb));
        displayLine(renderer, overlay, cam, corner.get(bb), corner.mirror(Axis.Z).get(bb));
    }
    
    private static void displayLine(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam, Vec3d start, Vec3d end) {
        var center = new Vec3d((start.x + end.x) * 0.5, (start.y + end.y) * 0.5, (start.z + end.z) * 0.5);
        var length = start.distance(end);
        overlay.renderLabel(cam, center, displayLength(length), ColorUtils.WHITE);
    }
    
}
