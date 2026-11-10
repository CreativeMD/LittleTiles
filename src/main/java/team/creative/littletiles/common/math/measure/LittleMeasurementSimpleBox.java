package team.creative.littletiles.common.math.measure;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.LittleToolMeasure.RenderMeasurementResult;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.math.box.LittleBoxLevel;

public class LittleMeasurementSimpleBox extends LittleMeasurement {
    
    protected LittleBoxLevel box;
    
    public LittleMeasurementSimpleBox(Level level, CompoundTag nbt) throws LittleActionException {
        super(level, nbt);
        box = LittleBoxLevel.of(level, nbt.get("box"));
    }
    
    public LittleMeasurementSimpleBox(LittleBoxLevel box) {
        super();
        this.box = box;
    }
    
    @Override
    public void collectPositions(List<LittleBoxLevel> positions) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.put("box", box.write());
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
        
        LittleMeasurementBox.displayLine(renderer, overlay, cam, bb, x, y, z);
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
    
}
