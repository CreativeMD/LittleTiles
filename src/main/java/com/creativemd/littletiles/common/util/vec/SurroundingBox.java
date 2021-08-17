package com.creativemd.littletiles.common.util.vec;

import java.util.HashMap;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public class SurroundingBox {
    
    protected World world;
    protected int count = 0;
    protected LittleGridContext context = LittleGridContext.getMin();
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
    
    public SurroundingBox(boolean mapScannedLists, World world) {
        this.mapScannedLists = mapScannedLists;
        this.world = world;
    }
    
    public void clear() {
        count = 0;
        context = LittleGridContext.getMin();
        
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
    
    public void convertTo(LittleGridContext to) {
        if (count == 0) {
            context = to;
            return;
        }
        
        if (context.size > to.size) {
            int modifier = context.size / to.size;
            minX /= modifier;
            minY /= modifier;
            minZ /= modifier;
            maxX /= modifier;
            maxY /= modifier;
            maxZ /= modifier;
        } else {
            int modifier = to.size / context.size;
            minX *= modifier;
            minY *= modifier;
            minZ *= modifier;
            maxX *= modifier;
            maxY *= modifier;
            maxZ *= modifier;
        }
        
        context = to;
    }
    
    protected boolean insertContext(LittleGridContext to) {
        if (context.size > to.size)
            return false;
        
        if (context.size < to.size)
            convertTo(to);
        return true;
    }
    
    public SurroundingBox add(IStructureTileList list) {
        add(list.getContext(), list.getPos(), list);
        return this;
    }
    
    public SurroundingBox add(LittleGridContext context, BlockPos pos, Iterable<LittleTile> tiles) {
        int modifier = 1;
        if (!insertContext(context))
            modifier = this.context.size / context.size;
        
        for (LittleTile tile : tiles)
            add(tile.getBox(), modifier, pos);
        
        if (mapScannedLists)
            map.put(pos, tiles);
        return this;
    }
    
    protected void add(LittleBox box, int modifier, BlockPos pos) {
        minX = Math.min(minX, pos.getX() * context.size + box.minX * modifier);
        minY = Math.min(minY, pos.getY() * context.size + box.minY * modifier);
        minZ = Math.min(minZ, pos.getZ() * context.size + box.minZ * modifier);
        
        maxX = Math.max(maxX, pos.getX() * context.size + box.maxX * modifier);
        maxY = Math.max(maxY, pos.getY() * context.size + box.maxY * modifier);
        maxZ = Math.max(maxZ, pos.getZ() * context.size + box.maxZ * modifier);
        
        minYPos = Math.min(minYPos, pos.getY());
        maxYPos = Math.max(maxYPos, pos.getY());
        
        count++;
    }
    
    public LittleBoxAbsolute getAbsoluteBox() {
        BlockPos pos = getMinPos();
        return new LittleBoxAbsolute(pos, new LittleBox((int) (minX - context.toGrid(pos.getX())), (int) (minY - context.toGrid(pos.getY())), (int) (minZ - context
            .toGrid(pos.getZ())), (int) (maxX - context.toGrid(pos.getX())), (int) (maxY - context.toGrid(pos.getY())), (int) (maxZ - context.toGrid(pos.getZ()))), context);
    }
    
    public AxisAlignedBB getAABB() {
        return new AxisAlignedBB(context.toVanillaGrid(minX), context.toVanillaGrid(minY), context.toVanillaGrid(minZ), context.toVanillaGrid(maxX), context
            .toVanillaGrid(maxY), context.toVanillaGrid(maxZ));
    }
    
    public double getPercentVolume() {
        return 0.125 * context.toVanillaGrid(minX + maxX) * context.toVanillaGrid(minY + maxY) * context.toVanillaGrid(minZ + maxZ);
    }
    
    public LittleVecAbsolute getHighestCenterPoint() {
        int centerX = (int) Math.floor((minX + maxX) / (double) context.size / 2D);
        int centerY = (int) Math.floor((minY + maxY) / (double) context.size / 2D);
        int centerZ = (int) Math.floor((minZ + maxZ) / (double) context.size / 2D);
        
        int centerTileX = (int) (Math.floor(minX + maxX) / 2D) - centerX * context.size;
        int centerTileY = (int) (Math.floor(minY + maxY) / 2D) - centerY * context.size;
        int centerTileZ = (int) (Math.floor(minZ + maxZ) / 2D) - centerZ * context.size;
        
        LittleVecAbsolute pos = new LittleVecAbsolute(new BlockPos(centerX, minYPos, centerZ), context, new LittleVec(centerTileX, 0, centerTileZ));
        
        MutableBlockPos blockPos = new MutableBlockPos();
        
        for (int y = minYPos; y <= maxYPos; y++) {
            Iterable<LittleTile> tilesInCenter = map.get(blockPos.setPos(centerX, y, centerZ));
            if (tilesInCenter != null) {
                TileEntityLittleTiles te = getTe(blockPos);
                
                te.convertTo(context);
                LittleBox box = new LittleBox(centerTileX, 0, centerTileZ, centerTileX + 1, context.maxPos, centerTileZ + 1);
                if (context.size <= centerTileX) {
                    box.minX = context.size - 1;
                    box.maxX = context.size;
                }
                
                if (context.size <= centerTileZ) {
                    box.minZ = context.size - 1;
                    box.maxZ = context.size;
                }
                
                // int highest = LittleTile.minPos;
                for (LittleTile tile : tilesInCenter) {
                    LittleBox littleBox = tile.getCollisionBox();
                    if (littleBox != null && LittleBox.intersectsWith(box, littleBox)) {
                        pos.overwriteContext(te.getContext());
                        pos.getVec().y = Math.max((y - minYPos) * context.size + littleBox.maxY, pos.getVec().y);
                    }
                }
                te.convertToSmallest();
            }
        }
        
        pos.removeInternalBlockOffset();
        pos.convertToSmallest();
        return pos;
    }
    
    public Vec3d getHighestCenterVec() {
        int centerX = (int) Math.floor((minX + maxX) / (double) context.size / 2D);
        int centerY = (int) Math.floor((minY + maxY) / (double) context.size / 2D);
        int centerZ = (int) Math.floor((minZ + maxZ) / (double) context.size / 2D);
        
        int centerTileX = (int) (Math.floor(minX + maxX) / 2D) - centerX * context.size;
        int centerTileY = (int) (Math.floor(minY + maxY) / 2D) - centerY * context.size;
        int centerTileZ = (int) (Math.floor(minZ + maxZ) / 2D) - centerZ * context.size;
        
        LittleVecAbsolute pos = new LittleVecAbsolute(new BlockPos(centerX, minYPos, centerZ), context, new LittleVec(centerTileX, 0, centerTileZ));
        
        MutableBlockPos blockPos = new MutableBlockPos();
        
        for (int y = minYPos; y <= maxYPos; y++) {
            Iterable<LittleTile> tilesInCenter = map.get(blockPos.setPos(centerX, y, centerZ));
            if (tilesInCenter != null) {
                TileEntityLittleTiles te = getTe(blockPos);
                te.convertTo(context);
                LittleBox box = new LittleBox(centerTileX, 0, centerTileZ, centerTileX + 1, context.maxPos, centerTileZ + 1);
                if (context.size >= centerTileX) {
                    box.minX = context.size - 1;
                    box.maxX = context.size;
                }
                
                if (context.size >= centerTileZ) {
                    box.minZ = context.size - 1;
                    box.maxZ = context.size;
                }
                
                // int highest = LittleTile.minPos;
                for (LittleTile tile : tilesInCenter) {
                    LittleBox littleBox = tile.getCollisionBox();
                    if (littleBox != null && LittleBox.intersectsWith(box, littleBox)) {
                        pos.overwriteContext(te.getContext());
                        pos.getVec().y = Math.max((y - minYPos) * context.size + littleBox.maxY, pos.getVec().y);
                    }
                }
                te.convertToSmallest();
            }
        }
        
        return new Vec3d(context.toVanillaGrid((minX + maxX) / 2D), pos.getPosY(), context.toVanillaGrid((minZ + maxZ) / 2D));
    }
    
    private TileEntityLittleTiles getTe(BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityLittleTiles)
            return (TileEntityLittleTiles) tileEntity;
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
        return new LittleVec((int) (minX - context.toBlockOffset(minX) * context.size), (int) (minY - context.toBlockOffset(minY) * context.size), (int) (minZ - context
            .toBlockOffset(minZ) * context.size));
    }
    
    public LittleVec getMaxPosOffset() {
        return new LittleVec((int) (maxX - context.toBlockOffset(maxX) * context.size), (int) (maxY - context.toBlockOffset(maxY) * context.size), (int) (maxZ - context
            .toBlockOffset(maxZ) * context.size));
    }
    
    public LittleVec getSize() {
        return new LittleVec((int) (maxX - minX), (int) (maxY - minY), (int) (maxZ - minZ));
    }
    
    public BlockPos getMinPos() {
        return new BlockPos(context.toBlockOffset(minX), context.toBlockOffset(minY), context.toBlockOffset(minZ));
    }
    
    public BlockPos getMaxPos() {
        return new BlockPos(context.toBlockOffset(maxX), context.toBlockOffset(maxY), context.toBlockOffset(maxZ));
    }
    
    public LittleGridContext getContext() {
        return context;
    }
    
    public int count() {
        return count;
    }
}
