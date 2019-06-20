package com.creativemd.littletiles.common.mods.warpdrive;

import java.lang.reflect.InvocationTargetException;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.connection.IStructureConnector;
import com.creativemd.littletiles.common.structure.connection.StructureLinkBaseRelative;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierStructureAbsolute;

import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.api.WarpDriveText;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
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
		return null;
	}
	
	@Override
	public void removeExternals(World world, int x, int y, int z, Block block, int blockMeta, TileEntity tileEntity) {
		
	}
	
	@Override
	public int rotate(Block block, int metadata, NBTTagCompound nbtTileEntity, ITransformation transformation) {
		final byte rotationSteps = transformation.getRotationSteps();
		if (rotationSteps == 0) {
			return metadata;
		}
		
		TileEntityLittleTiles te = new TileEntityLittleTiles();
		te.readFromNBT(nbtTileEntity);
		for (LittleTile tile : te.getTiles())
			for (int rotationStep = 0; rotationStep < rotationSteps; rotationStep++)
				transformTile(tile, Rotation.Y_CLOCKWISE);
		te.writeToNBT(nbtTileEntity);
		return metadata;
	}
	
	@Override
	public void restoreExternals(World world, BlockPos blockPos, IBlockState blockState, TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		
	}
	
	public static void transformTile(LittleTile tile, Rotation rotation) {
		tile.box.rotateBox(rotation, tile.getContext().rotationCenter);
		if (tile.connection != null)
			transformConnection(tile.connection, rotation);
	}
	
	public static void transformConnection(IStructureConnector<LittleTile> connector, Rotation rotation) {
		if (connector instanceof StructureLinkBaseRelative) {
			StructureLinkBaseRelative connect = (StructureLinkBaseRelative) connector;
			connect.coord = new BlockPos(RotationUtils.rotate(connect.coord, rotation));
			Vec3i vec = RotationUtils.rotate(new Vec3i((double) connect.identifier[0], (double) connect.identifier[1], (double) connect.identifier[2]), rotation);
			connect.identifier[0] = (int) vec.getX();
			connect.identifier[1] = (int) vec.getY();
			connect.identifier[2] = (int) vec.getZ();
		} else if (connector instanceof LittleTileIdentifierStructureAbsolute) {
			LittleTileIdentifierStructureAbsolute connect = (LittleTileIdentifierStructureAbsolute) connector;
			connect.pos = new BlockPos(RotationUtils.rotate(connect.pos, rotation));
			Vec3i vec = RotationUtils.rotate(new Vec3i((double) connect.identifier[0], (double) connect.identifier[1], (double) connect.identifier[2]), rotation);
			connect.identifier[0] = (int) vec.getX();
			connect.identifier[1] = (int) vec.getY();
			connect.identifier[2] = (int) vec.getZ();
		}
	}
}
