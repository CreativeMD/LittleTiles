package team.creative.littletiles.common.entity.particle;

import net.minecraft.client.particle.ParticleRenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum LittleParticleRenderType {
    
    TERRAIN_SHEET,
    PARTICLE_SHEET_OPAQUE,
    PARTICLE_SHEET_TRANSLUCENT,
    PARTICLE_SHEET_LIT,
    CUSTOM,
    NO_RENDER;
    
    @OnlyIn(Dist.CLIENT)
    public ParticleRenderType toVanilla() {
        return switch (this) {
            case CUSTOM -> ParticleRenderType.CUSTOM;
            case NO_RENDER -> ParticleRenderType.NO_RENDER;
            case PARTICLE_SHEET_LIT -> ParticleRenderType.PARTICLE_SHEET_LIT;
            case PARTICLE_SHEET_OPAQUE -> ParticleRenderType.PARTICLE_SHEET_OPAQUE;
            case PARTICLE_SHEET_TRANSLUCENT -> ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
            case TERRAIN_SHEET -> ParticleRenderType.TERRAIN_SHEET;
        };
    }
    
}
