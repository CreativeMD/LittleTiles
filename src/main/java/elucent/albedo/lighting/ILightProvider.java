package elucent.albedo.lighting;

import elucent.albedo.event.GatherLightsEvent;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILightProvider {
    
    @SideOnly(Side.CLIENT)
    public abstract void gatherLights(GatherLightsEvent paramGatherLightsEvent, Entity paramEntity);
}