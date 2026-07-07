package team.creative.littletiles.common.mod.sable;

import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.phys.Vec3;

public class SableContext implements ISableContext {
    
    public final SubLevel level;
    
    public SableContext(SubLevel level) {
        this.level = level;
    }
    
    @Override
    public Vec3 toLocal(Vec3 vec) {
        return level.logicalPose().transformPositionInverse(vec);
    }
}
