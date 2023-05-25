package team.creative.littletiles.api.client.entity;

import net.minecraft.world.level.Level;

public interface LevelTransitionListener {
    
    public void prepareChangeLevel(Level oldLevel, Level newLevel);
    
    public void changedLevel(Level oldLevel, Level newLevel);
    
}
