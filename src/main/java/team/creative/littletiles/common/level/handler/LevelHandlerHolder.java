package team.creative.littletiles.common.level.handler;

import net.minecraft.world.level.Level;

public class LevelHandlerHolder {
    
    public final Level level;
    
    public LevelHandlerHolder(Level level) {
        this.level = level;
    }
    
    public void unload() {}
    
}
