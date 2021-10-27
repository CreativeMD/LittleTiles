package team.creative.littletiles.common.entity.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;

@OnlyIn(Dist.CLIENT)
public class LittleParticle extends Particle {
    
    public ParticleSettings settings;
    private float scaleDeviation;
    
    public LittleParticle(ClientLevel level, Vec3d pos, Vec3d speed, ParticleSettings settings) {
        super(level, pos.x, pos.y, pos.z);
        this.xd = speed.x * (Math.random() * 0.1 + 0.95);
        this.yd = speed.y * (Math.random() * 0.1 + 0.95);
        this.zd = speed.z * (Math.random() * 0.1 + 0.95);
        this.scaleDeviation = settings.startSize;
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
        this.settings = settings;
        this.scaleDeviation = (float) (Math.random() * settings.sizeDeviation);
        settings.texture.setTextureInit(this);
    }
    
    @Override
    public void tick() {
        settings.texture.setTextureTick(this);
        this.scaleDeviation = scaleDeviation + getAge() / (float) getMaxAge() * (settings.endSize - settings.startSize) + settings.startSize;
        super.tick();
    }
    
    public int getAge() {
        return age;
    }
    
    public int getMaxAge() {
        return lifetime;
    }
    
    @Override
    public void render(VertexConsumer p_107261_, Camera p_107262_, float p_107263_) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public ParticleRenderType getRenderType() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
