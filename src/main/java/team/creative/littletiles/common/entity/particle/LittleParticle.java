package team.creative.littletiles.common.entity.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;
import team.creative.littletiles.mixin.client.render.ParticleEngineAccessor;

@OnlyIn(Dist.CLIENT)
public class LittleParticle extends TextureSheetParticle {
    
    private static final Minecraft mc = Minecraft.getInstance();
    
    public ParticleSettings settings;
    public SpriteSet sprites;
    private float scaleDeviation;
    
    public LittleParticle(ClientLevel level, Vec3d pos, Vec3d speed, ParticleSettings settings) {
        super(level, pos.x, pos.y, pos.z);
        this.xd = speed.x * (Math.random() * 0.1 + 0.95);
        this.yd = speed.y * (Math.random() * 0.1 + 0.95);
        this.zd = speed.z * (Math.random() * 0.1 + 0.95);
        this.lifetime = (int) (settings.lifetime + settings.lifetimeDeviation * Math.random());
        this.gravity = settings.gravity;
        this.alpha = ColorUtils.alphaF(settings.color);
        this.rCol = ColorUtils.redF(settings.color);
        this.gCol = ColorUtils.greenF(settings.color);
        this.bCol = ColorUtils.blueF(settings.color);
        if (settings.randomColor) {
            this.rCol *= Math.random();
            this.gCol *= Math.random();
            this.bCol *= Math.random();
        }
        this.hasPhysics = settings.collision;
        this.settings = settings;
        this.sprites = ((ParticleEngineAccessor) mc.particleEngine).getSpriteSets().get(settings.texture.particleTexture);
        this.scaleDeviation = (float) (Math.random() * settings.sizeDeviation);
        settings.texture.init(this);
        settings.texture.tick(this);
        this.setSize(0.2F * settings.startSize, 0.2F * settings.startSize);
    }
    
    @Override
    public void tick() {
        settings.texture.tick(this);
        float lifePercentage = getAge() / (float) getMaxAge();
        this.quadSize = scaleDeviation + lifePercentage * (settings.endSize - settings.startSize) + settings.startSize;
        super.tick();
    }
    
    public int getAge() {
        return age;
    }
    
    public int getMaxAge() {
        return lifetime;
    }
    
    public void setSpriteFirst(SpriteSet set) {
        this.setSprite(set.get(0, this.lifetime));
    }
    
    public void setSpriteFromAgeReverse(SpriteSet set) {
        if (!this.removed)
            this.setSprite(set.get(this.lifetime - this.age, this.lifetime));
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        return settings.texture.type;
    }
    
}
