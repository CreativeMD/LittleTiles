package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {
    
    @Accessor
    public NativeImage getLightPixels();
    
    @Accessor
    public DynamicTexture getLightTexture();
    
}
