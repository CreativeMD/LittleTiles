package team.creative.littletiles.common.math.measure;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public class LittleMeasurementVolume extends LittleMeasurementBox {
    
    public LittleMeasurementVolume(CompoundTag nbt) {
        super(nbt);
    }
    
    public LittleMeasurementVolume(List<LittleBoxAbsolute> positions) {
        super(positions);
    }
    
    @Override
    public void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        var bb = box.toAABB();
        if (!renderer.isVisible(bb))
            return;
        
        var center = bb.getCenter();
        
        overlay.renderLabel(cam, new Vec3d(center), Component.literal((bb.getXsize() * bb.getYsize() * bb.getZsize()) + ""), ColorUtils.WHITE);
    }
}
