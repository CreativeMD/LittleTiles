package team.creative.littletiles.common.structure.relative;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class StructureAbsolute extends StructureRelative {
    
    public static int intFloorDiv(int coord, int bucketSize) {
        return coord < 0 ? -((-coord - 1) / bucketSize) - 1 : coord / bucketSize;
    }
    
    public final LittleVecGrid inBlockOffset;
    public final BlockPos baseOffset;
    public final SectionPos chunkOffset;
    public final BlockPos inChunkOffset;
    
    public final Vec3d rotationCenter;
    public final Vec3d rotationCenterInsideBlock;
    
    public StructureAbsolute(BlockPos pos, LittleBox box, LittleGrid grid) {
        super(box, grid);
        
        LittleVecGrid minVec = getMinVec();
        BlockPos minPosOffset = minVec.getBlockPos();
        sub(minPosOffset);
        
        this.inBlockOffset = minVec;
        
        this.baseOffset = pos.offset(minPosOffset);
        
        this.chunkOffset = SectionPos.of(baseOffset);
        int chunkX = intFloorDiv(baseOffset.getX(), 16);
        int chunkY = intFloorDiv(baseOffset.getY(), 16);
        int chunkZ = intFloorDiv(baseOffset.getZ(), 16);
        
        this.inChunkOffset = new BlockPos(baseOffset.getX() - (chunkX * 16), baseOffset.getY() - (chunkY * 16), baseOffset.getZ() - (chunkZ * 16));
        
        this.rotationCenterInsideBlock = getCenter();
        this.rotationCenter = new Vec3d(rotationCenterInsideBlock);
        this.rotationCenter.x += baseOffset.getX();
        this.rotationCenter.y += baseOffset.getY();
        this.rotationCenter.z += baseOffset.getZ();
    }
    
    public StructureAbsolute(LittleVecAbsolute pos, LittleBox box, LittleGrid grid) {
        super(box, grid);
        add(pos.getVecGrid());
        
        LittleVecGrid minVec = getMinVec();
        BlockPos minPosOffset = minVec.getBlockPos();
        sub(minPosOffset);
        minVec.sub(minPosOffset);
        
        this.inBlockOffset = minVec;
        
        this.baseOffset = pos.getPos().offset(minPosOffset);
        
        this.chunkOffset = SectionPos.of(baseOffset);
        int chunkX = intFloorDiv(baseOffset.getX(), 16);
        int chunkY = intFloorDiv(baseOffset.getY(), 16);
        int chunkZ = intFloorDiv(baseOffset.getZ(), 16);
        
        this.inChunkOffset = new BlockPos(baseOffset.getX() - (chunkX * 16), baseOffset.getY() - (chunkY * 16), baseOffset.getZ() - (chunkZ * 16));
        
        this.rotationCenterInsideBlock = getCenter();
        this.rotationCenter = new Vec3d(rotationCenterInsideBlock);
        this.rotationCenter.x += baseOffset.getX();
        this.rotationCenter.y += baseOffset.getY();
        this.rotationCenter.z += baseOffset.getZ();
    }
    
    public StructureAbsolute(LittleVecAbsolute pos, StructureRelative relative) {
        this(pos, relative.box.copy(), relative.grid);
    }
    
    public StructureAbsolute(String name, CompoundTag nbt) {
        this(getPos(nbt.getIntArray(name + "_pos")), LittleBox.create(nbt.getIntArray(name + "_box")), LittleGrid.get(nbt.getInt(name + "_grid")));
    }
    
    public StructureAbsolute(LittleVecAbsolute axis, LittleVec additional) {
        this(axis.getPos(), convertAxisToBox(axis.getVecGrid(), additional), axis.getGrid());
    }
    
    public void save(String name, CompoundTag nbt) {
        nbt.putIntArray(name + "_pos", new int[] { baseOffset.getX(), baseOffset.getY(), baseOffset.getZ() });
        nbt.putInt(name + "_grid", grid.count);
        nbt.putIntArray(name + "_box", box.getArray());
    }
    
    @Override
    public LittleVec getDoubledCenterVec() {
        return new LittleVec((box.maxX * 2 - box.minX * 2) / 2, (box.maxY * 2 - box.minY * 2) / 2, (box.maxZ * 2 - box.minZ * 2) / 2);
    }
    
    private static BlockPos getPos(int[] array) {
        return new BlockPos(array[0], array[1], array[2]);
    }
    
    public static LittleBox convertAxisToBox(LittleVecGrid vec, LittleVec additional) {
        if (additional.x == 0)
            return new LittleBox(vec.getVec().x - 1, vec.getVec().y - 1, vec.getVec().z - 1, vec.getVec().x + 1, vec.getVec().y + 1, vec.getVec().z + 1);
        return new LittleBox(additional.x > 0 ? vec.getVec().x : vec.getVec().x - 1, additional.y > 0 ? vec.getVec().y : vec.getVec().y - 1, additional.z > 0 ? vec.getVec().z : vec
                .getVec().z - 1, additional.x > 0 ? vec.getVec().x + 1 : vec.getVec().x, additional.y > 0 ? vec.getVec().y + 1 : vec.getVec().y, additional.z > 0 ? vec
                        .getVec().z + 1 : vec.getVec().z);
    }
}
