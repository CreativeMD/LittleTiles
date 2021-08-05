package team.creative.littletiles.common.level;

import net.minecraft.world.level.Level;

public interface ILevelProvider {
    
    public default boolean hasLevel() {
        return getLevel() != null;
    }
    
    public Level getLevel();
    
}
