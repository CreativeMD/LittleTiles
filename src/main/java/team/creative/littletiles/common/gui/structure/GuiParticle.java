package team.creative.littletiles.common.gui.structure;

import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter;

public class GuiParticle extends GuiLayer {
    
    public LittleParticleEmitter particle;
    
    public GuiParticle(LittleParticleEmitter particle) {
        super("particle", 200, 230);
        this.particle = particle;
    }
    
    @Override
    public void create() {}
    
}
