package team.creative.littletiles.common.mod.sable;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import team.creative.littletiles.common.level.context.ILittleLevelContext;

public class SableManager {
    
    public static final String MODID = "sable";
    public static final boolean INSTALLED = ModList.get().isLoaded(MODID);
    
    public static @Nullable ILittleLevelContext context(LevelAccessor level, BlockPos pos) {
        if (INSTALLED)
            return SableInteractor.context((Level) level, pos);
        return ILittleLevelContext.STANDARD;
    }
    
    public static @Nullable ILittleLevelContext context(LevelAccessor level, ChunkPos pos) {
        if (INSTALLED)
            return SableInteractor.context((Level) level, pos);
        return ILittleLevelContext.STANDARD;
    }
    
    public static @Nullable ILittleLevelContext context(LevelAccessor level, Vec3 pos) {
        if (INSTALLED)
            return SableInteractor.context((Level) level, pos);
        return ILittleLevelContext.STANDARD;
    }
    
    public static boolean isSubLevel(LevelAccessor level, BlockPos pos) {
        if (INSTALLED)
            return SableInteractor.isSubLevel((Level) level, pos);
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void markDirty(LevelAccessor level, BlockPos pos) {
        if (INSTALLED)
            SableInteractor.markDirty((Level) level, pos);
    }
    
}
