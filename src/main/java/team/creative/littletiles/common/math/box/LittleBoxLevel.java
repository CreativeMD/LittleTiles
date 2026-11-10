package team.creative.littletiles.common.math.box;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.context.ILittleLevelContext;

public class LittleBoxLevel extends LittleBoxAbsolute {
    
    public static LittleBoxLevel tryOf(Level level, @Nullable Tag tag) {
        try {
            return of(level, tag);
        } catch (LittleActionException e) {
            return null;
        }
    }
    
    public static LittleBoxLevel of(Level level, Tag tag) throws LittleActionException {
        if (tag == null)
            return null;
        if (tag instanceof IntArrayTag t) {
            int[] array = t.getAsIntArray();
            var box = of(array);
            return new LittleBoxLevel(ILittleLevelContext.of(level, box.pos), box);
        }
        if (tag instanceof CompoundTag nbt) {
            int[] array = nbt.getIntArray("b");
            return new LittleBoxLevel(ILittleLevelContext.of(level, nbt.getString("c")), of(array));
        }
        return null;
    }
    
    public final ILittleLevelContext context;
    
    public LittleBoxLevel(ILittleLevelContext context, BlockPos pos) {
        super(pos);
        this.context = context;
    }
    
    public LittleBoxLevel(ILittleLevelContext context, BlockPos pos, LittleBox box, LittleGrid grid) {
        super(pos, box, grid);
        this.context = context;
    }
    
    public LittleBoxLevel(ILittleLevelContext context, BlockPos pos, LittleBoxGrid box) {
        super(pos, box);
        this.context = context;
    }
    
    public LittleBoxLevel(ILittleLevelContext context, LittleBoxAbsolute box) {
        super(box.pos, box.box, box.grid);
        this.context = context;
    }
    
    public Tag write() {
        if (!context.hasId())
            return new IntArrayTag(toArray());
        CompoundTag nbt = new CompoundTag();
        nbt.putIntArray("b", toArray());
        nbt.putString("c", context.getId());
        return nbt;
    }
    
    public LittleBoxLevel convertTo(ILittleLevelContext context) {
        var bb = context.toFakeWorld(this.context.toRealWorld(toAABB()));
        
        long minX = grid.toGridAccurate(bb.minX);
        long minY = grid.toGridAccurate(bb.minY);
        long minZ = grid.toGridAccurate(bb.minZ);
        long maxX = grid.toGridAccurate(bb.maxX);
        long maxY = grid.toGridAccurate(bb.maxY);
        long maxZ = grid.toGridAccurate(bb.maxZ);
        
        BlockPos pos = new BlockPos((int) (minX / grid.count), (int) (minY / grid.count), (int) (minZ / grid.count));
        minX -= pos.getX() * grid.count;
        minY -= pos.getY() * grid.count;
        minZ -= pos.getZ() * grid.count;
        maxX -= pos.getX() * grid.count;
        maxY -= pos.getY() * grid.count;
        maxZ -= pos.getZ() * grid.count;
        
        return new LittleBoxLevel(context, pos, new LittleBox((int) minX, (int) minY, (int) minZ, (int) maxX, (int) maxY, (int) maxZ), grid);
    }
    
    @Override
    public LittleBoxLevel copy() {
        return new LittleBoxLevel(context, pos, box.copy(), grid);
    }
    
}
