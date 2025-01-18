package team.creative.littletiles.common.entity.particle;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import team.creative.creativecore.common.util.mc.LanguageUtils;

public enum LittleParticleTexture {
    
    dust_fade_out(new ResourceLocation("minecraft", "smoke"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    dust(new ResourceLocation("minecraft", "smoke"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    dust_grow(new ResourceLocation("minecraft", "smoke"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAgeReverse(particle.sprites);
        }
        
    },
    bubble(new ResourceLocation("minecraft", "bubble"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    diamond(new ResourceLocation("minecraft", "totem_of_undying"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    spark(new ResourceLocation("minecraft", "firework"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.setSpriteFirst(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    spark_fade_out(new ResourceLocation("minecraft", "firework"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    spiral(new ResourceLocation("minecraft", "entity_effect"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    note(new ResourceLocation("minecraft", "note"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    flame(new ResourceLocation("minecraft", "flame"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    happy(new ResourceLocation("minecraft", "happy_villager"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    angry(new ResourceLocation("minecraft", "angry_villager"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    damage(new ResourceLocation("minecraft", "damage_indicator"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    letter(new ResourceLocation("minecraft", "enchant"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    dripping(new ResourceLocation("minecraft", "dripping_water"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    falling(new ResourceLocation("minecraft", "falling_water"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    heart(new ResourceLocation("minecraft", "heart"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    lava(new ResourceLocation("minecraft", "lava"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    soul(new ResourceLocation("minecraft", "soul"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    soul_flame(new ResourceLocation("minecraft", "soul_fire_flame"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
        @Override
        public void init(LittleParticle particle) {
            particle.pickSprite(particle.sprites);
        }
        
        @Override
        public void tick(LittleParticle particle) {}
        
    },
    big_smoke(new ResourceLocation("minecraft", "campfire_cosy_smoke"), LittleParticleRenderType.PARTICLE_SHEET_TRANSLUCENT) {
        
        @Override
        public void init(LittleParticle particle) {}
        
        @Override
        public void tick(LittleParticle particle) {
            particle.setSpriteFromAge(particle.sprites);
        }
        
    },
    nautilus(new ResourceLocation("minecraft", "nautilus"), LittleParticleRenderType.PARTICLE_SHEET_OPAQUE) {
        
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
    public final LittleParticleRenderType type;
    
    private LittleParticleTexture(ResourceLocation particleTexture, LittleParticleRenderType type) {
        this.particleTexture = particleTexture;
        this.type = type;
    }
    
    public abstract void init(LittleParticle particle);
    
    public abstract void tick(LittleParticle particle);
    
    public Component title() {
        return Component.literal(LanguageUtils.translateOr("particle.texture." + name(), name()));
    }
    
}
