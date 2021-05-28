package team.creative.littletiles.common.world;

import net.minecraft.world.World;

public interface ILevelProvider {
    
    public default boolean hasLevel() {
        return getLevel() != null;
    }
    
    public World getLevel();
    
}
