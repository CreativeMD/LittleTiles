package team.creative.littletiles.common.mod.sable;

import javax.annotation.Nullable;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.littletiles.common.level.context.ILittleLevelContext;

public class SableInteractor {
    
    public static ILittleLevelContext context(Level level, BlockPos pos) {
        if (level == null || pos == null)
            return ILittleLevelContext.STANDARD;
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        var subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel != null)
            return new SableContext(subLevel);
        return ILittleLevelContext.STANDARD;
    }
    
    public static ILittleLevelContext context(Level level, ChunkPos pos) {
        if (level == null || pos == null)
            return ILittleLevelContext.STANDARD;
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        var subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel != null)
            return new SableContext(subLevel);
        return ILittleLevelContext.STANDARD;
    }
    
    public static ILittleLevelContext context(Level level, Vec3 pos) {
        if (level == null || pos == null)
            return ILittleLevelContext.STANDARD;
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        var subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel != null)
            return new SableContext(subLevel);
        return ILittleLevelContext.STANDARD;
    }
    
    public static boolean isSubLevel(Level level, BlockPos pos) {
        return context(level, pos).isSubLevel();
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void markDirty(Level level, BlockPos pos) {
        if (level instanceof ISubLevel)
            return;
        var c = context(level, pos);
        if (c instanceof SableContext s) {
            int sx = SectionPos.blockToSectionCoord(pos.getX());
            int sy = SectionPos.blockToSectionCoord(pos.getY());
            int sz = SectionPos.blockToSectionCoord(pos.getZ());
            Minecraft.getInstance().execute(() -> ((ClientSubLevel) s.level).getRenderData().setDirty(sx, sy, sz, true));
        }
    }
    
    public static @Nullable IVecOrigin originWrapper(Level level, Vec3 pos, IVecOrigin origin) {
        var c = context(level, pos);
        if (c != null && c.isSubLevel())
            return new VecOriginSable(origin, (SableContext) c);
        return origin;
    }
    
}
