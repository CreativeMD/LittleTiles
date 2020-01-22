package com.creativemd.littletiles.common.utils.vec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.math.box.LittleBox;
import com.creativemd.littletiles.common.tiles.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tiles.math.vec.LittleVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;

public class SurroundingBox {
	
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
	protected HashMap<BlockPos, List<LittleTile>> map = new HashMap<>();
	
	public SurroundingBox(boolean mapScannedLists) {
		this.mapScannedLists = mapScannedLists;
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
	
	protected void convertTo(LittleGridContext to) {
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
	
	public SurroundingBox add(Set<Entry<BlockPos, ArrayList<LittleTile>>> entrySet) {
		for (Entry<BlockPos, ArrayList<LittleTile>> entry : entrySet) {
			if (entry.getValue().isEmpty())
				continue;
			
			TileEntityLittleTiles te = entry.getValue().get(0).te;
			add(te.getContext(), entry.getKey(), entry.getValue());
		}
		return this;
	}
	
	public SurroundingBox add(LittleGridContext context, BlockPos pos, List<LittleTile> tiles) {
		int modifier = 1;
		if (!insertContext(context))
			modifier = this.context.size / context.size;
		
		for (LittleTile tile : tiles) {
			add(tile.box, modifier, pos);
		}
		
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
	
	public AxisAlignedBB getSurroundingBox() {
		return new AxisAlignedBB(context.toVanillaGrid(minX), context.toVanillaGrid(minY), context.toVanillaGrid(minZ), context.toVanillaGrid(maxX), context.toVanillaGrid(maxY), context.toVanillaGrid(maxZ));
	}
	
	public LittleAbsoluteVec getHighestCenterPoint() {
		int centerX = (int) Math.floor((minX + maxX) / (double) context.size / 2D);
		int centerY = (int) Math.floor((minY + maxY) / (double) context.size / 2D);
		int centerZ = (int) Math.floor((minZ + maxZ) / (double) context.size / 2D);
		
		int centerTileX = (int) (Math.floor(minX + maxX) / 2D) - centerX * context.size;
		int centerTileY = (int) (Math.floor(minY + maxY) / 2D) - centerY * context.size;
		int centerTileZ = (int) (Math.floor(minZ + maxZ) / 2D) - centerZ * context.size;
		
		LittleAbsoluteVec pos = new LittleAbsoluteVec(new BlockPos(centerX, minYPos, centerZ), context, new LittleVec(centerTileX, 0, centerTileZ));
		
		MutableBlockPos blockPos = new MutableBlockPos();
		
		for (int y = minYPos; y <= maxYPos; y++) {
			List<LittleTile> tilesInCenter = map.get(blockPos.setPos(centerX, y, centerZ));
			if (tilesInCenter != null && !tilesInCenter.isEmpty()) {
				
				TileEntityLittleTiles te = tilesInCenter.get(0).te;
				
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
				for (int i = 0; i < tilesInCenter.size(); i++) {
					List<LittleBox> collision = tilesInCenter.get(i).getCollisionBoxes();
					for (int j = 0; j < collision.size(); j++) {
						LittleBox littleBox = collision.get(j);
						if (LittleBox.intersectsWith(box, littleBox)) {
							pos.overwriteContext(te.getContext());
							pos.getVec().y = Math.max((y - minYPos) * context.size + littleBox.maxY, pos.getVec().y);
						}
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
		
		LittleAbsoluteVec pos = new LittleAbsoluteVec(new BlockPos(centerX, minYPos, centerZ), context, new LittleVec(centerTileX, 0, centerTileZ));
		
		MutableBlockPos blockPos = new MutableBlockPos();
		
		for (int y = minYPos; y <= maxYPos; y++) {
			List<LittleTile> tilesInCenter = map.get(blockPos.setPos(centerX, y, centerZ));
			if (tilesInCenter != null && !tilesInCenter.isEmpty()) {
				TileEntityLittleTiles te = tilesInCenter.get(0).te;
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
				for (int i = 0; i < tilesInCenter.size(); i++) {
					List<LittleBox> collision = tilesInCenter.get(i).getCollisionBoxes();
					for (int j = 0; j < collision.size(); j++) {
						LittleBox littleBox = collision.get(j);
						if (LittleBox.intersectsWith(box, littleBox)) {
							pos.overwriteContext(te.getContext());
							pos.getVec().y = Math.max((y - minYPos) * context.size + littleBox.maxY, pos.getVec().y);
						}
					}
				}
				te.convertToSmallest();
			}
		}
		
		return new Vec3d(context.toVanillaGrid((minX + maxX) / 2D), pos.getPosY(), context.toVanillaGrid((minZ + maxZ) / 2D));
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
	
	public LittleGridContext getContext() {
		return context;
	}
	
	public int count() {
		return count;
	}
}
