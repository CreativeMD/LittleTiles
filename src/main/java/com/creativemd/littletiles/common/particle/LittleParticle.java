package com.creativemd.littletiles.common.particle;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LittleParticle extends Particle {
    
    public ParticleSettings settings;
    private float scaleDeviation;
    
    public LittleParticle(World world, Vector3d pos, Vector3d speed, ParticleSettings settings) {
        super(world, pos.x, pos.y, pos.z);
        this.motionX = speed.x * (Math.random() * 0.1 + 0.95);
        this.motionY = speed.y * (Math.random() * 0.1 + 0.95);
        this.motionZ = speed.z * (Math.random() * 0.1 + 0.95);
        this.particleScale = settings.startSize;
        this.particleMaxAge = (int) (settings.lifetime + settings.lifetimeDeviation * Math.random());
        this.particleGravity = settings.gravity;
        this.particleAlpha = ColorUtils.getAlphaDecimal(settings.color);
        this.particleRed = ColorUtils.getRedDecimal(settings.color);
        this.particleGreen = ColorUtils.getGreenDecimal(settings.color);
        this.particleBlue = ColorUtils.getBlueDecimal(settings.color);
        if (settings.randomColor) {
            this.particleRed *= Math.random();
            this.particleGreen *= Math.random();
            this.particleBlue *= Math.random();
        }
        this.settings = settings;
        this.scaleDeviation = (float) (Math.random() * settings.sizeDeviation);
        settings.texture.setTextureInit(this);
        this.setSize(0.2F * particleScale, 0.2F * particleScale);
    }
    
    @Override
    public void onUpdate() {
        settings.texture.setTextureTick(this);
        this.particleScale = scaleDeviation + getAge() / (float) getMaxAge() * (settings.endSize - settings.startSize) + settings.startSize;
        super.onUpdate();
    }
    
    public int getAge() {
        return particleAge;
    }
    
    public int getMaxAge() {
        return particleMaxAge;
    }
    
}
