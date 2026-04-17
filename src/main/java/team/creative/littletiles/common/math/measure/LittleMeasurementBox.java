package team.creative.littletiles.common.math.measure;

import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.measure.LittleMeasurement.LittleMeasurementSimple;

public class LittleMeasurementBox extends LittleMeasurementSimple {
    
    private LittleBoxAbsolute box;
    
    public LittleMeasurementBox(List<LittleBoxAbsolute> positions) {
        super(positions);
    }
    
    @Override
    public void changed() {
        super.changed();
        box = first.copy();
        box.include(second);
    }
    
    @Override
    public void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void build(PreviewRenderer renderer, PoseStack pose, BufferBuilder builder) {
        var renderBox = box.getRenderingBox();
        renderBox.color = color;
        renderer.buildBox(pose, renderBox, builder, 255, true);
    }
    
}
