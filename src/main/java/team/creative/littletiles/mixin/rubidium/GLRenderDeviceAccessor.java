package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.gl.device.GLRenderDevice;

@Mixin(GLRenderDevice.class)
public interface GLRenderDeviceAccessor {
    
    @Accessor(remap = false)
    public boolean getIsActive();
    
}
