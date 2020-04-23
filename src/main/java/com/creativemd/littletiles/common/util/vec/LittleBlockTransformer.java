package com.creativemd.littletiles.common.util.vec;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectionalField;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class LittleBlockTransformer {
	
	public static void flipTE(TileEntityLittleTiles te, Axis axis, boolean connected) {
		if (axis == null)
			return;
		
		for (LittleTile tile : te)
			flipTile(tile, axis, connected);
	}
	
	public static void rotateTE(TileEntityLittleTiles te, Rotation rotation, int steps, boolean connected) {
		if (rotation == null)
			return;
		
		for (LittleTile tile : te)
			for (int rotationStep = 0; rotationStep < steps; rotationStep++)
				rotateTile(tile, rotation, connected);
	}
	
	public static void flipTile(LittleTile tile, Axis axis, boolean connected) {
		LittleVec moved = tile.box.getMinVec();
		tile.box.flipBox(axis, tile.getContext().rotationCenter);
		moved.sub(tile.box.getMinVec());
		if (!connected && tile.isChildOfStructure()) {
			if (!tile.connection.isLink())
				flipStructure(tile.connection.getStructureWithoutLoading(), axis, moved);
		}
	}
	
	public static void rotateTile(LittleTile tile, Rotation rotation, boolean connected) {
		LittleVec moved = tile.box.getMinVec();
		tile.box.rotateBox(rotation, tile.getContext().rotationCenter);
		moved.sub(tile.box.getMinVec());
		if (!connected && tile.isChildOfStructure()) {
			if (!tile.connection.isLink())
				rotateStructure(tile.connection.getStructureWithoutLoading(), rotation, moved);
		}
	}
	
	public static void flipStructure(LittleStructure structure, Axis axis, LittleVec moved) {
		BlockPos mainPos = structure.getMainTile().te.getPos();
		
		if (structure.tilesToLoad != null) {
			LinkedHashMap<BlockPos, Integer> newTilesToLoad = new LinkedHashMap<>();
			for (Entry<BlockPos, Integer> entry : structure.tilesToLoad.entrySet()) {
				BlockPos pos = entry.getKey();
				pos = pos.subtract(mainPos);
				pos = RotationUtils.flip(pos, axis);
				pos = pos.add(mainPos);
				newTilesToLoad.put(pos, entry.getValue());
			}
			structure.tilesToLoad = newTilesToLoad;
			structure.blockTiles().clear();
		}
		
		LittleGridContext context = structure.getMainTile().getContext();
		LittleVec center = context.rotationCenter.copy();
		LittleVec offset = structure.getMainTile().getMinVec();
		offset.scale(2);
		center.sub(offset);
		for (StructureDirectionalField relative : structure.type.directional) {
			Object value = relative.get(structure);
			relative.move(value, context, moved);
			relative.flip(value, context, axis, center);
		}
	}
	
	public static void rotateStructure(LittleStructure structure, Rotation rotation, LittleVec moved) {
		BlockPos mainPos = structure.getMainTile().te.getPos();
		if (structure.tilesToLoad != null) {
			LinkedHashMap<BlockPos, Integer> newTilesToLoad = new LinkedHashMap<>();
			for (Entry<BlockPos, Integer> entry : structure.tilesToLoad.entrySet()) {
				BlockPos pos = entry.getKey();
				pos = pos.subtract(mainPos);
				pos = RotationUtils.rotate(pos, rotation);
				pos = pos.add(mainPos);
				newTilesToLoad.put(pos, entry.getValue());
			}
			structure.tilesToLoad = newTilesToLoad;
			structure.blockTiles().clear();
		}
		
		LittleGridContext context = structure.getMainTile().getContext();
		LittleVec center = context.rotationCenter.copy();
		LittleVec offset = structure.getMainTile().getMinVec();
		offset.scale(2);
		center.sub(offset);
		for (StructureDirectionalField relative : structure.type.directional) {
			Object value = relative.get(structure);
			relative.move(value, context, moved);
			relative.rotate(value, context, rotation, center);
		}
	}
}
