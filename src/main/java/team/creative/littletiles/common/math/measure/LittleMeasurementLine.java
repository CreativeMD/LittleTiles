package team.creative.littletiles.common.math.measure;

import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.measure.LittleMeasurement.LittleMeasurementSimple;

public class LittleMeasurementLine extends LittleMeasurementSimple {
    
    private AABB bb;
    private Vec3d start;
    private Vec3d end;
    private Vec3d center;
    private double length;
    
    public LittleMeasurementLine(CompoundTag nbt) {
        super(nbt);
    }
    
    public LittleMeasurementLine(List<LittleBoxAbsolute> positions) {
        super(positions);
    }
    
    @Override
    public void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        if (!renderer.isVisible(bb))
            return;
        overlay.renderLabel(cam, center, Component.literal(length + ""), ColorUtils.WHITE);
    }
    
    @Override
    public void changed() {
        super.changed();
        LittleBoxAbsolute combined = this.first.copy();
        combined.include(second);
        
        var minFirst = this.first.getMin().getVec3d();
        var minSecond = this.second.getMin().getVec3d();
        
        bb = combined.toAABB();
        
        start = new Vec3d();
        end = new Vec3d();
        if (minFirst.x < minSecond.x) {
            start.x = bb.minX;
            end.x = bb.maxX;
        } else {
            start.x = bb.maxX;
            end.x = bb.minX;
        }
        if (minFirst.y < minSecond.y) {
            start.y = bb.minY;
            end.y = bb.maxY;
        } else {
            start.y = bb.maxY;
            end.y = bb.minY;
        }
        if (minFirst.z < minSecond.z) {
            start.z = bb.minZ;
            end.z = bb.maxZ;
        } else {
            start.z = bb.maxZ;
            end.z = bb.minZ;
        }
        var sizeOriginal = this.first.getSize().getVec3d();
        var size = combined.getSize().getVec3d();
        if (sizeOriginal.x == size.x)
            start.x = end.x = Mth.lerp(0.5, bb.minX, bb.maxX);
        if (sizeOriginal.y == size.y)
            start.y = end.y = Mth.lerp(0.5, bb.minY, bb.maxY);
        if (sizeOriginal.z == size.z)
            start.z = end.z = Mth.lerp(0.5, bb.minZ, bb.maxZ);
        center = new Vec3d((start.x + end.x) * 0.5, (start.y + end.y) * 0.5, (start.z + end.z) * 0.5);
        length = start.distance(end);
    }
    
    @Override
    public void build(PreviewRenderer renderer, PoseStack pose, BufferBuilder builder) {
        Vec3f normal = new Vec3f();
        VectorFan.setLineNormal(normal, (float) start.x, (float) start.y, (float) start.z, (float) end.x, (float) end.y, (float) end.z);
        builder.addVertex(pose.last().pose(), (float) start.x, (float) start.y, (float) start.z).setColor(color).setNormal(pose.last(), normal.x, normal.y, normal.z);
        builder.addVertex(pose.last().pose(), (float) end.x, (float) end.y, (float) end.z).setColor(color).setNormal(pose.last(), normal.x, normal.y, normal.z);
    }
    
}
