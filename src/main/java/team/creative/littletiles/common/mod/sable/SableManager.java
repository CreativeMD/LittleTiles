package team.creative.littletiles.common.mod.sable;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

public class SableManager {
    
    public static final String MODID = "sable";
    public static final boolean INSTALLED = ModList.get().isLoaded(MODID);
    
    public static @Nullable ISableContext context(LevelAccessor level, BlockPos pos) {
        if (INSTALLED)
            return SableInteractor.context((Level) level, pos);
        return null;
    }
    
    public static @Nullable ISableContext context(LevelAccessor level, ChunkPos pos) {
        if (INSTALLED)
            return SableInteractor.context((Level) level, pos);
        return null;
    }
    
    public static @Nullable ISableContext context(LevelAccessor level, Vec3 pos) {
        if (INSTALLED)
            return SableInteractor.context((Level) level, pos);
        return null;
    }
    
    public static boolean isSubLevel(LevelAccessor level, BlockPos pos) {
        if (INSTALLED)
            return SableInteractor.isSubLevel((Level) level, pos);
        return false;
    }
    
}
