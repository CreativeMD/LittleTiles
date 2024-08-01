package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.gl.device.GLRenderDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GLRenderDevice.class)
public interface GLRenderDeviceAccessor {
    
    @Accessor(remap = false)
    public boolean getIsActive();
    
}
