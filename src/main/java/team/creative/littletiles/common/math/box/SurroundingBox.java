package team.creative.littletiles.common.math.box;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;

public class SurroundingBox {
    
    protected Level level;
    protected int count = 0;
    protected LittleGrid grid = LittleGrid.min();
    protected long minX = Long.MAX_VALUE;
    protected long minY = Long.MAX_VALUE;
    protected long minZ = Long.MAX_VALUE;
    protected long maxX = Long.MIN_VALUE;
    protected long maxY = Long.MIN_VALUE;
    protected long maxZ = Long.MIN_VALUE;
    
    protected int minYPos = Integer.MAX_VALUE;
    protected int maxYPos = Integer.MIN_VALUE;
    
    protected boolean mapScannedLists = false;
    protected HashMap<BlockPos, Iterable<LittleTile>> map = new HashMap<>();
    
    public SurroundingBox(boolean mapScannedLists, Level level) {
        this.mapScannedLists = mapScannedLists;
        this.level = level;
    }
    
    public void clear() {
        count = 0;
        grid = LittleGrid.min();
        
        minX = Long.MAX_VALUE;
        minY = Long.MAX_VALUE;
        minZ = Long.MAX_VALUE;
        maxX = Long.MIN_VALUE;
        maxY = Long.MIN_VALUE;
        maxZ = Long.MIN_VALUE;
        
        minYPos = Integer.MAX_VALUE;
        maxYPos = Integer.MIN_VALUE;
        
        map.clear();
    }
    
    public void convertTo(LittleGrid to) {
        if (count == 0) {
            grid = to;
            return;
        }
        
        if (grid.count > to.count) {
            int modifier = grid.count / to.count;
            minX /= modifier;
            minY /= modifier;
            minZ /= modifier;
            maxX /= modifier;
            maxY /= modifier;
            maxZ /= modifier;
        } else {
            int modifier = to.count / grid.count;
            minX *= modifier;
            minY *= modifier;
            minZ *= modifier;
            maxX *= modifier;
            maxY *= modifier;
            maxZ *= modifier;
        }
        
        grid = to;
    }
    
    protected boolean insertContext(LittleGrid to) {
        if (grid.count > to.count)
            return false;
        
        if (grid.count < to.count)
            convertTo(to);
        return true;
    }
    
    public SurroundingBox add(IStructureParentCollection list) {
        add(list.getGrid(), list.getPos(), list);
        return this;
    }
    
    public SurroundingBox add(LittleGrid context, BlockPos pos, Iterable<LittleTile> tiles) {
        int modifier = 1;
        if (!insertContext(context))
            modifier = this.grid.count / context.count;
        
        for (LittleTile tile : tiles)
            for (LittleBox box : tile)
                add(box, modifier, pos);
            
        if (mapScannedLists)
            map.put(pos, tiles);
        return this;
    }
    
    protected void add(LittleBox box, int modifier, BlockPos pos) {
        minX = Math.min(minX, pos.getX() * grid.count + box.minX * modifier);
        minY = Math.min(minY, pos.getY() * grid.count + box.minY * modifier);
        minZ = Math.min(minZ, pos.getZ() * grid.count + box.minZ * modifier);
        
        maxX = Math.max(maxX, pos.getX() * grid.count + box.maxX * modifier);
        maxY = Math.max(maxY, pos.getY() * grid.count + box.maxY * modifier);
        maxZ = Math.max(maxZ, pos.getZ() * grid.count + box.maxZ * modifier);
        
        minYPos = Math.min(minYPos, pos.getY());
        maxYPos = Math.max(maxYPos, pos.getY());
        
        count++;
    }
    
    public LittleBoxAbsolute getAbsoluteBox() {
        BlockPos pos = getMinPos();
        return new LittleBoxAbsolute(pos, new LittleBox((int) (minX - grid.toGrid(pos.getX())), (int) (minY - grid.toGrid(pos.getY())), (int) (minZ - grid
                .toGrid(pos.getZ())), (int) (maxX - grid.toGrid(pos.getX())), (int) (maxY - grid.toGrid(pos.getY())), (int) (maxZ - grid.toGrid(pos.getZ()))), grid);
    }
    
    public AABB getAABB() {
        return new AABB(grid.toVanillaGrid(minX), grid.toVanillaGrid(minY), grid.toVanillaGrid(minZ), grid.toVanillaGrid(maxX), grid.toVanillaGrid(maxY), grid.toVanillaGrid(maxZ));
    }
    
    public VoxelShape getShape() {
        return Shapes
                .box(grid.toVanillaGrid(minX), grid.toVanillaGrid(minY), grid.toVanillaGrid(minZ), grid.toVanillaGrid(maxX), grid.toVanillaGrid(maxY), grid.toVanillaGrid(maxZ));
    }
    
    public double getPercentVolume() {
        return 0.125 * grid.toVanillaGrid(minX + maxX) * grid.toVanillaGrid(minY + maxY) * grid.toVanillaGrid(minZ + maxZ);
    }
    
    public LittleVecAbsolute getHighestCenterPoint() {
        int centerX = (int) Math.floor((minX + maxX) / (double) grid.count / 2D);
        int centerZ = (int) Math.floor((minZ + maxZ) / (double) grid.count / 2D);
        
        int centerTileX = (int) (Math.floor(minX + maxX) / 2D) - centerX * grid.count;
        int centerTileZ = (int) (Math.floor(minZ + maxZ) / 2D) - centerZ * grid.count;
        
        LittleVecAbsolute pos = new LittleVecAbsolute(new BlockPos(centerX, minYPos, centerZ), grid, new LittleVec(centerTileX, 0, centerTileZ));
        
        MutableBlockPos blockPos = new MutableBlockPos();
        
        for (int y = minYPos; y <= maxYPos; y++) {
            Iterable<LittleTile> tilesInCenter = map.get(blockPos.set(centerX, y, centerZ));
            if (tilesInCenter != null) {
                BETiles be = getBE(blockPos);
                
                be.convertTo(grid);
                LittleBox box = new LittleBox(centerTileX, 0, centerTileZ, centerTileX + 1, grid.count, centerTileZ + 1);
                if (grid.count <= centerTileX) {
                    box.minX = grid.count - 1;
                    box.maxX = grid.count;
                }
                
                if (grid.count <= centerTileZ) {
                    box.minZ = grid.count - 1;
                    box.maxZ = grid.count;
                }
                
                // int highest = LittleTile.minPos;
                for (LittleTile tile : tilesInCenter)
                    for (LittleBox littleBox : tile)
                        if (LittleBox.intersectsWith(box, littleBox)) {
                            pos.overwriteGrid(be.getGrid());
                            pos.getVec().y = Math.max((y - minYPos) * grid.count + littleBox.maxY, pos.getVec().y);
                        }
                be.convertToSmallest();
            }
        }
        
        pos.removeInternalBlockOffset();
        pos.convertToSmallest();
        return pos;
    }
    
    public Vec3d getHighestCenterVec() {
        int centerX = (int) Math.floor((minX + maxX) / (double) grid.count / 2D);
        int centerZ = (int) Math.floor((minZ + maxZ) / (double) grid.count / 2D);
        
        int centerTileX = (int) (Math.floor(minX + maxX) / 2D) - centerX * grid.count;
        int centerTileZ = (int) (Math.floor(minZ + maxZ) / 2D) - centerZ * grid.count;
        
        LittleVecAbsolute pos = new LittleVecAbsolute(new BlockPos(centerX, minYPos, centerZ), grid, new LittleVec(centerTileX, 0, centerTileZ));
        
        MutableBlockPos blockPos = new MutableBlockPos();
        
        for (int y = minYPos; y <= maxYPos; y++) {
            Iterable<LittleTile> tilesInCenter = map.get(blockPos.set(centerX, y, centerZ));
            if (tilesInCenter != null) {
                BETiles be = getBE(blockPos);
                be.convertTo(grid);
                LittleBox box = new LittleBox(centerTileX, 0, centerTileZ, centerTileX + 1, grid.count, centerTileZ + 1);
                if (grid.count >= centerTileX) {
                    box.minX = grid.count - 1;
                    box.maxX = grid.count;
                }
                
                if (grid.count >= centerTileZ) {
                    box.minZ = grid.count - 1;
                    box.maxZ = grid.count;
                }
                
                // int highest = LittleTile.minPos;
                for (LittleTile tile : tilesInCenter)
                    for (LittleBox littleBox : tile)
                        
                        if (LittleBox.intersectsWith(box, littleBox)) {
                            pos.overwriteGrid(be.getGrid());
                            pos.getVec().y = Math.max((y - minYPos) * grid.count + littleBox.maxY, pos.getVec().y);
                            
                        }
                be.convertToSmallest();
            }
        }
        
        return new Vec3d(grid.toVanillaGrid((minX + maxX) / 2D), pos.getPosY(), grid.toVanillaGrid((minZ + maxZ) / 2D));
    }
    
    private BETiles getBE(BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BETiles)
            return (BETiles) be;
        throw new RuntimeException("TileEntity does not exist anymore");
    }
    
    public long getMinX() {
        return minX;
    }
    
    public long getMinY() {
        return minY;
    }
    
    public long getMinZ() {
        return minZ;
    }
    
    public long getMaxX() {
        return maxX;
    }
    
    public long getMaxY() {
        return maxY;
    }
    
    public long getMaxZ() {
        return maxZ;
    }
    
    public long getMin(Axis axis) {
        switch (axis) {
            case X:
                return minX;
            case Y:
                return minY;
            case Z:
                return minZ;
        }
        return 0;
    }
    
    public long getMax(Axis axis) {
        switch (axis) {
            case X:
                return maxX;
            case Y:
                return maxY;
            case Z:
                return maxZ;
        }
        return 0;
    }
    
    public LittleVec getMinPosOffset() {
        return new LittleVec((int) (minX - grid.toBlockOffset(minX) * grid.count), (int) (minY - grid.toBlockOffset(minY) * grid.count), (int) (minZ - grid
                .toBlockOffset(minZ) * grid.count));
    }
    
    public LittleVec getMaxPosOffset() {
        return new LittleVec((int) (maxX - grid.toBlockOffset(maxX) * grid.count), (int) (maxY - grid.toBlockOffset(maxY) * grid.count), (int) (maxZ - grid
                .toBlockOffset(maxZ) * grid.count));
    }
    
    public LittleVec getSize() {
        return new LittleVec((int) (maxX - minX), (int) (maxY - minY), (int) (maxZ - minZ));
    }
    
    public BlockPos getMinPos() {
        return new BlockPos(grid.toBlockOffset(minX), grid.toBlockOffset(minY), grid.toBlockOffset(minZ));
    }
    
    public BlockPos getMaxPos() {
        return new BlockPos(grid.toBlockOffset(maxX), grid.toBlockOffset(maxY), grid.toBlockOffset(maxZ));
    }
    
    public LittleGrid getGrid() {
        return grid;
    }
    
    public int count() {
        return count;
    }
}
