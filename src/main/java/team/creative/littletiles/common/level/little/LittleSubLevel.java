package team.creative.littletiles.common.level.little;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.matrix.ChildVecOrigin;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.matrix.VecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.mod.sable.SableManager;

public interface LittleSubLevel extends ISubLevel, LittleLevel {
    
    public default IVecOrigin createOrigin(Vec3d center) {
        var parent = getParent();
        IVecOrigin origin;
        if (parent instanceof IOrientatedLevel o)
            origin = new ChildVecOrigin(o.getOrigin(), center);
        else
            origin = new VecOrigin(center);
        return SableManager.originWrapper(parent, center.toVanilla(), origin);
    }
    
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
