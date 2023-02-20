package team.creative.littletiles.common.level.little;

import net.minecraft.world.flag.FeatureFlagSet;
import team.creative.creativecore.common.level.ISubLevel;

public interface LittleSubLevel extends ISubLevel, LittleLevel {
    
    @Override
    public default FeatureFlagSet enabledFeatures() {
        return getParent().enabledFeatures();
    }
    
}
