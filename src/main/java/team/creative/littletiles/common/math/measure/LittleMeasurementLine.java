package team.creative.littletiles.common.math.measure;

import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
        overlay.renderLabel(cam, center, Component.literal(length + ""), ColorUtils.WHITE);
    }
    
    @Override
    public void changed() {
        super.changed();
        start = this.first.getVanillaCenter();
        end = this.second.getVanillaCenter();
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
