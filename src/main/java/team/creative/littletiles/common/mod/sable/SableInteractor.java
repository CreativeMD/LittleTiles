package team.creative.littletiles.common.mod.sable;

import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.level.ISubLevel;

public class SableInteractor {
    
    public static ISableContext context(Level level, BlockPos pos) {
        if (level == null || pos == null)
            return null;
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        var subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel != null)
            return new SableContext(subLevel);
        return null;
    }
    
    public static ISableContext context(Level level, ChunkPos pos) {
        if (level == null || pos == null)
            return null;
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        var subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel != null)
            return new SableContext(subLevel);
        return null;
    }
    
    public static ISableContext context(Level level, Vec3 pos) {
        if (level == null || pos == null)
            return null;
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        var subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel != null)
            return new SableContext(subLevel);
        return null;
    }
    
    public static boolean isSubLevel(Level level, BlockPos pos) {
        return context(level, pos) != null;
    }
    
}
