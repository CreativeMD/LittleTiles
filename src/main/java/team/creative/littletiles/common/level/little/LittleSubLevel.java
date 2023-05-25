package team.creative.littletiles.common.level.little;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import team.creative.creativecore.common.level.ISubLevel;

public interface LittleSubLevel extends ISubLevel, LittleLevel {
    
    public void setParent(Level level);
    
    public LevelEntityGetter<Entity> getEntityGetter();
    
    @Override
    public default FeatureFlagSet enabledFeatures() {
        return getParent().enabledFeatures();
    }
    
    public default boolean shouldUseLightingForRenderig() {
        return true;
    }
    
}
