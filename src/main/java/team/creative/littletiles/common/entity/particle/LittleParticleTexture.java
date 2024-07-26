package team.creative.littletiles.common.entity.particle;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import team.creative.creativecore.common.util.mc.LanguageUtils;

public enum LittleParticleTexture {
    
    dust_fade_out(ResourceLocation.withDefaultNamespace("smoke"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    dust(ResourceLocation.withDefaultNamespace("smoke"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    dust_grow(ResourceLocation.withDefaultNamespace("smoke"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAgeReverse(particle.sprites);
        }
        
    },
    bubble(ResourceLocation.withDefaultNamespace("bubble"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    diamond(ResourceLocation.withDefaultNamespace("totem_of_undying"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    spark(ResourceLocation.withDefaultNamespace("firework"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.setSpriteFirst(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    spark_fade_out(ResourceLocation.withDefaultNamespace("firework"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    spiral(ResourceLocation.withDefaultNamespace("entity_effect"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    note(ResourceLocation.withDefaultNamespace("note"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    flame(ResourceLocation.withDefaultNamespace("flame"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    happy(ResourceLocation.withDefaultNamespace("happy_villager"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    angry(ResourceLocation.withDefaultNamespace("angry_villager"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    damage(ResourceLocation.withDefaultNamespace("damage_indicator"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    letter(ResourceLocation.withDefaultNamespace("enchant"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    dripping(ResourceLocation.withDefaultNamespace("dripping_water"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    falling(ResourceLocation.withDefaultNamespace("falling_water"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    heart(ResourceLocation.withDefaultNamespace("heart"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    lava(ResourceLocation.withDefaultNamespace("lava"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    soul(ResourceLocation.withDefaultNamespace("soul"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    soul_flame(ResourceLocation.withDefaultNamespace("soul_fire_flame"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    big_smoke(ResourceLocation.withDefaultNamespace("campfire_cosy_smoke"), ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    nautilus(ResourceLocation.withDefaultNamespace("nautilus"), ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
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
    
    public Component title() {
        return Component.literal(LanguageUtils.translateOr("particle.texture." + name(), name()));
    }
    
}
