package team.creative.littletiles.common.world;

import net.minecraft.world.level.Level;

public interface ILevelProvider {
    
    public default boolean hasLevel() {
        return getLevel() != null;
    }
    
    public Level getLevel();
    
}
