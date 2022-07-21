package team.creative.littletiles.common.entity.particle;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.ResourceLocation;
import team.creative.creativecore.common.util.mc.LanguageUtils;

public enum LittleParticleTexture {
    
    dust_fade_out(new ResourceLocation("minecraft", "smoke"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    dust(new ResourceLocation("minecraft", "smoke"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    dust_grow(new ResourceLocation("minecraft", "smoke"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAgeReverse(particle.sprites);
        }
        
    },
    bubble(new ResourceLocation("minecraft", "bubble"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    diamond(new ResourceLocation("minecraft", "totem_of_undying"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    square(new ResourceLocation("minecraft", "smoke"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.setSpriteFirst(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    spark(new ResourceLocation("minecraft", "firework"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.setSpriteFirst(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    spark_fade_out(new ResourceLocation("minecraft", "firework"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    spiral(new ResourceLocation("minecraft", "entity_effect"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    note(new ResourceLocation("minecraft", "note"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    flame(new ResourceLocation("minecraft", "flame"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    happy(new ResourceLocation("minecraft", "happy_villager"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    angry(new ResourceLocation("minecraft", "angry_villager"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    damage(new ResourceLocation("minecraft", "damage_indicator"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    letter(new ResourceLocation("minecraft", "enchant"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    dripping(new ResourceLocation("minecraft", "dripping_water"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    falling(new ResourceLocation("minecraft", "falling_water"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    heart(new ResourceLocation("minecraft", "heart"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    lava(new ResourceLocation("minecraft", "lava"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    soul(new ResourceLocation("minecraft", "soul"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    soul_flame(new ResourceLocation("minecraft", "soul_fire_flame"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    big_smoke(new ResourceLocation("minecraft", "campfire_cosy_smoke"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    nautilus(new ResourceLocation("minecraft", "nautilus"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    };
    
    public static LittleParticleTexture get(String name) {
        for (int i = 0; i < values().length; i++)
            if (values()[i].name().equals(name))
                return values()[i];
        return dust_fade_out;
    }
    
    public final ResourceLocation particleTexture;
    public final ParticleRenderType type;
    
    private LittleParticleTexture(ResourceLocation particleTexture, ParticleRenderType type) {
        this.particleTexture = particleTexture;
        this.type = type;
    }
    
    public abstract void init(LittleParticle particle);
    
    public abstract void tick(LittleParticle particle);
    
    public String translatedName() {
        return LanguageUtils.translateOr("particle.texture." + name(), name());
    }
    
}
