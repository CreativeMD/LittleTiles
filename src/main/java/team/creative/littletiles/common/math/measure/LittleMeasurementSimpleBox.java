package team.creative.littletiles.common.math.measure;

import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public class LittleMeasurementSimpleBox extends LittleMeasurement {
    
    protected LittleBoxAbsolute box;
    
    public LittleMeasurementSimpleBox(CompoundTag nbt) {
        super(nbt);
        box = LittleBoxAbsolute.of(nbt.getIntArray("box"));
    }
    
    public LittleMeasurementSimpleBox(LittleBoxAbsolute box) {
        super();
        this.box = box;
    }
    
    @Override
    public void collectPositions(List<LittleBoxAbsolute> positions) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putIntArray("box", box.toArray());
    }
    
    @Override
    public void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        var bb = box.toAABB();
        if (!renderer.isVisible(bb))
            return;
        
        Facing x = cam.x < bb.maxX ? Facing.WEST : null;
        if (cam.x > bb.minX)
            x = x == null ? Facing.EAST : null;
        
        Facing y = cam.y < bb.maxY ? Facing.DOWN : null;
        if (cam.y > bb.minY)
            y = y == null ? Facing.UP : null;
        
        Facing z = cam.z < bb.maxZ ? Facing.NORTH : null;
        if (cam.z > bb.minZ)
            z = z == null ? Facing.SOUTH : null;
        
        LittleMeasurementBox.displayLine(renderer, overlay, cam, bb, x, y, z);
    }
    
    @Override
    public void build(PreviewRenderer renderer, PoseStack pose, BufferBuilder builder) {
        var renderBox = box.getRenderingBox();
        renderBox.color = color;
        renderer.buildBox(pose, renderBox, builder, 255, true);
    }
    
}
