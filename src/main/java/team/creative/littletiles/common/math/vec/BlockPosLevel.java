package team.creative.littletiles.common.math.vec;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.level.context.ILittleLevelContext;

public class BlockPosLevel {
    
    public static BlockPosLevel tryOf(Level level, @Nullable Tag tag) {
        try {
            return of(level, tag);
        } catch (LittleActionException e) {
            return null;
        }
    }
    
    @Nullable
    public static BlockPosLevel of(Level level, @Nullable Tag tag) throws LittleActionException {
        if (tag == null)
            return null;
        if (tag instanceof IntArrayTag t) {
            int[] array = t.getAsIntArray();
            BlockPos pos = new BlockPos(array[0], array[1], array[2]);
            return new BlockPosLevel(ILittleLevelContext.of(level, pos), pos);
        }
        if (tag instanceof CompoundTag nbt) {
            int[] array = nbt.getIntArray("p");
            return new BlockPosLevel(ILittleLevelContext.of(level, nbt.getString("c")), new BlockPos(array[0], array[1], array[2]));
        }
        return null;
    }
    
    public final ILittleLevelContext context;
    public final BlockPos pos;
    
    public BlockPosLevel(ILittleLevelContext context, BlockPos pos) {
        this.context = context;
        this.pos = pos;
    }
    
    public Tag write() {
        if (!context.hasId())
            return new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() });
        CompoundTag nbt = new CompoundTag();
        nbt.putIntArray("p", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.putString("c", context.getId());
        return nbt;
    }
    
    public int getX() {
        return pos.getX();
    }
    
    public int getY() {
        return pos.getY();
    }
    
    public int getZ() {
        return pos.getZ();
    }
    
    public BlockPosLevel convertTo(ILittleLevelContext context) {
        var vec = this.context.toRealWorld(pos.getCenter());
        return new BlockPosLevel(context, BlockPos.containing(context.toFakeWorld(vec)));
    }
}
