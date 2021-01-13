package com.creativemd.littletiles.common.particle;

import java.util.Random;

import com.creativemd.creativecore.common.gui.GuiControl;

public enum LittleParticleTexture {
    
    dust_fade_out {
        
        @Override
        public void setTextureInit(LittleParticle particle) {}
        
        @Override
        public void setTextureTick(LittleParticle particle) {
            particle.setParticleTextureIndex(7 - particle.getAge() * 8 / particle.getMaxAge());
        }
        
    },
    dust {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex((int) (Math.random() * 8.0D));
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    dust_grow {
        
        @Override
        public void setTextureInit(LittleParticle particle) {}
        
        @Override
        public void setTextureTick(LittleParticle particle) {
            particle.setParticleTextureIndex(8 * particle.getAge() / particle.getMaxAge() - 1);
        }
        
    },
    bubble {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(32);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    diamond {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(176 + (8 - 1 - particle.getAge() * 8 / particle.getMaxAge()));
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    square {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(113);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    spark {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(160 + (8 - 1 - particle.getAge() * 8 / particle.getMaxAge()));
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    spiral {
        
        @Override
        public void setTextureInit(LittleParticle particle) {}
        
        @Override
        public void setTextureTick(LittleParticle particle) {
            particle.setParticleTextureIndex(128 + (7 - particle.getAge() * 8 / particle.getMaxAge()));
        }
        
    },
    star {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(65);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    note {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(64);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    flame {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(48);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    happy {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(82);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    angry {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(81);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    damage {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(67);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    letter {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex((int) (Math.random() * 26.0D + 1.0D + 224.0D));
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    water {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(19 + rand.nextInt(4));
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    heart {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(80);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    suspend {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(0);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    },
    lava {
        
        @Override
        public void setTextureInit(LittleParticle particle) {
            particle.setParticleTextureIndex(49);
        }
        
        @Override
        public void setTextureTick(LittleParticle particle) {}
        
    };
    
    private static Random rand = new Random();
    
    public abstract void setTextureInit(LittleParticle particle);
    
    public abstract void setTextureTick(LittleParticle particle);
    
    public String translatedName() {
        return GuiControl.translateOrDefault("particle.texture." + name(), name());
    }
    
    public static LittleParticleTexture get(String name) {
        for (int i = 0; i < values().length; i++)
            if (values()[i].name().equals(name))
                return values()[i];
        return dust_fade_out;
    }
    
}
