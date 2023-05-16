package team.creative.littletiles.common.structure.animation.context;

import net.minecraft.sounds.SoundEvent;
import team.creative.littletiles.common.structure.LittleStructure;

public interface AnimationContext {
    
    public boolean isClient();
    
    public boolean isGui();
    
    public void play(SoundEvent event, float volume, float pitch);
    
    public AnimationContext getChild(int id);
    
    public LittleStructure getChildStructure(int id);
    
}
