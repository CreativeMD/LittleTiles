package team.creative.littletiles.common.entity.particle;

import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpread;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpreadRandom;

public enum LittleParticlePresets {
    
    SMOKE(
            1,
            5,
            new ParticleSettings(-0.08F, ColorUtils.rgba(20, 20, 20, 255), 40, 5, 0.4F, 0.6F, 0.1F, LittleParticleTexture.dust_fade_out, true),
            new ParticleSpreadRandom(0, 0, 0, 0.2F)),
    FLAME(1, 20, new ParticleSettings(0F, ColorUtils.WHITE, 40, 5, 1F, 1F, 0.1F, LittleParticleTexture.flame, false), new ParticleSpreadRandom(0.005F, 0, 0, 0F)),
    WATER_DROP(
            1,
            20,
            new ParticleSettings(0F, ColorUtils.rgba(0, 85, 255, 255), 40, 5, 2F, 2F, 0.3F, LittleParticleTexture.square, false),
            new ParticleSpreadRandom(0.1F, 0, 0, 0F)),
    NOTE(1, 10, new ParticleSettings(0F, ColorUtils.WHITE, 40, 5, 1F, 0.5F, 0.3F, LittleParticleTexture.note, true), new ParticleSpreadRandom(0.04F, 0, 0, 0.03F)),
    BIG_SMOKE(
            1,
            5,
            new ParticleSettings(-0.1F, ColorUtils.rgba(54, 54, 54, 210), 88, 10, 3F, 4F, 0.3F, LittleParticleTexture.dust_fade_out, false),
            new ParticleSpreadRandom(0.04F, 0, 0, 0.03F)),
    SAND(1, 5, new ParticleSettings(0.6F, ColorUtils.rgba(54, 54, 54, 210), 50, 8, 1, 1, 0.3F, LittleParticleTexture.dust, false), new ParticleSpreadRandom(0.1F, 0, 0, 0.03F)),
    CONFETTI(
            20,
            20,
            new ParticleSettings(0F, ColorUtils.rgba(255, 255, 255, 255), 84, 10, 0F, 1F, 2F, LittleParticleTexture.diamond, true),
            new ParticleSpreadRandom(0.1F, 0, 0, 0.1F));
    
    public final ParticleSettings settings;
    public final int count;
    public final int delay;
    public final ParticleSpread spread;
    
    private LittleParticlePresets(int count, int delay, ParticleSettings settings, ParticleSpread spread) {
        this.settings = settings;
        this.count = count;
        this.delay = delay;
        this.spread = spread;
    }
}
