package com.creativemd.littletiles.common.mods.warpdrive;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.StructureLinkBaseRelative;
import com.creativemd.littletiles.common.structure.connection.StructureLinkTile;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.StructureTypeRelative;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierRelative;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.api.WarpDriveText;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityLittleTilesTransformer implements IBlockTransformer {
	
	public static void init() {
		
		try {
			Class clazz = Class.forName("cr0s.warpdrive.config.WarpDriveConfig");
			clazz.getMethod("registerBlockTransformer", String.class, IBlockTransformer.class).invoke(null, LittleTiles.modid, new TileEntityLittleTilesTransformer());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isApplicable(Block block, int metadata, TileEntity tileEntity) {
		return tileEntity instanceof TileEntityLittleTiles;
	}
	
	@Override
	public boolean isJumpReady(Block block, int metadata, TileEntity tileEntity, WarpDriveText reason) {
		return true;
	}
	
	@Override
	public NBTBase saveExternals(World world, int x, int y, int z, Block block, int blockMeta, TileEntity tileEntity) {
		TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
		NBTTagCompound nbt = new NBTTagCompound();
		List<StructureLinkTile> connectors = new ArrayList<>();
		for (LittleTile tile : te.getTiles()) {
			if (tile.isChildOfStructure() && tile.isConnectedToStructure() && tile.connection instanceof LittleTileIdentifierRelative && !connectors.contains(tile.connection))
				connectors.add((StructureLinkTile) tile.connection);
		}
		NBTTagList list = new NBTTagList();
		for (StructureLinkTile connector : connectors) {
			NBTTagCompound connectNBT = connector.writeToNBT(new NBTTagCompound());
			connectNBT.setIntArray("mainTileBox", connector.getStructure(world).getMainTile().box.getArray());
			connectNBT.setInteger("mainTileContext", connector.getStructure(world).getMainTile().getContext().size);
			list.appendTag(connectNBT);
		}
		nbt.setTag("connectors", list);
		return nbt;
	}
	
	@Override
	public void removeExternals(World world, int x, int y, int z, Block block, int blockMeta, TileEntity tileEntity) {
		
	}
	
	@Override
	public void restoreExternals(World world, BlockPos blockPos, IBlockState blockState, TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		final byte rotationSteps = transformation.getRotationSteps();
		if (rotationSteps == 0)
			return;
		Rotation rotation = Rotation.Y_COUNTER_CLOCKWISE;
		int count = rotationSteps;
		if (rotationSteps == 3) {
			count = 1;
			rotation = Rotation.Y_CLOCKWISE;
		}
		
		TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
		NBTTagCompound nbt = (NBTTagCompound) nbtBase;
		List<LittleTileIdentifierRelative> connectors = new ArrayList<>();
		List<LittleTileBox> boxes = new ArrayList<>();
		List<LittleGridContext> contextes = new ArrayList<>();
		NBTTagList list = nbt.getTagList("connectors", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound connectorNBT = list.getCompoundTagAt(i);
			connectors.add(new LittleTileIdentifierRelative(connectorNBT));
			LittleTileBox box = LittleTileBox.createBox(connectorNBT.getIntArray("mainTileBox"));
			LittleGridContext context = LittleGridContext.get(connectorNBT.getInteger("mainTileContext"));
			for (int rotationStep = 0; rotationStep < count; rotationStep++)
				box.rotateBox(rotation, context.rotationCenter);
			
			boxes.add(box);
			contextes.add(context);
		}
		
		for (LittleTile tile : te.getTiles()) {
			if (tile.isChildOfStructure() && tile.connection instanceof StructureLinkBaseRelative) {
				@SuppressWarnings("unlikely-arg-type")
				int index = connectors.indexOf(tile.connection);
				if (index != -1) {
					StructureLinkBaseRelative connect = (StructureLinkBaseRelative) tile.connection;
					for (int rotationStep = 0; rotationStep < count; rotationStep++)
						connect.coord = new BlockPos(RotationUtils.rotate(connect.coord, rotation));
					
					connect.identifier = boxes.get(index).getIdentifier();
					connect.context = contextes.get(index);
				} else
					System.out.println("Could not find rotated connection ...");
			}
		}
		
		te.updateTiles();
	}
	
	@Override
	public int rotate(Block block, int metadata, NBTTagCompound nbtTileEntity, ITransformation transformation) {
		final byte rotationSteps = transformation.getRotationSteps();
		if (rotationSteps == 0) {
			return metadata;
		}
		
		TileEntityLittleTiles te = new TileEntityLittleTiles();
		te.readFromNBT(nbtTileEntity);
		
		Rotation rotation = Rotation.Y_COUNTER_CLOCKWISE;
		int count = rotationSteps;
		if (rotationSteps == 3) {
			count = 1;
			rotation = Rotation.Y_CLOCKWISE;
		}
		for (LittleTile tile : te.getTiles())
			for (int rotationStep = 0; rotationStep < count; rotationStep++)
				transformTile(tile, rotation);
		te.writeToNBT(nbtTileEntity);
		return metadata;
	}
	
	public static void transformTile(LittleTile tile, Rotation rotation) {
		LittleTileVec moved = tile.box.getMinVec();
		tile.box.rotateBox(rotation, tile.getContext().rotationCenter);
		moved.sub(tile.box.getMinVec());
		if (tile.isChildOfStructure()) {
			if (!tile.connection.isLink())
				transformStructure(tile.connection.getStructureWithoutLoading(), rotation, moved);
		}
	}
	
	public static void transformStructure(LittleStructure structure, Rotation rotation, LittleTileVec moved) {
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
		}
		
		LittleGridContext context = structure.getMainTile().getContext();
		LittleTileVec center = context.rotationCenter.copy();
		LittleTileVec offset = structure.getMainTile().getMinVec();
		offset.scale(2);
		center.sub(offset);
		for (StructureTypeRelative relative : structure.type.relatives) {
			StructureRelative relativeST = relative.getRelative(structure);
			if (relativeST == null)
				continue;
			relativeST.onMove(structure, context, moved);
			relativeST.onRotate(structure, context, rotation, center);
		}
	}
}
