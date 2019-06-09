package com.creativemd.littletiles.common.mods.warpdrive;

import java.lang.reflect.InvocationTargetException;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.connection.IStructureConnector;
import com.creativemd.littletiles.common.structure.connection.StructureLinkBaseRelative;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
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
import net.minecraft.util.math.Vec3d;
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
		for (LittleTile tile : te.getTiles()) {
			for (int rotationStep = 0; rotationStep < rotationSteps; rotationStep++)
				transformTile(tile, transformation);
		}
		te.writeToNBT(nbtTileEntity);
		return metadata;
	}
	
	@Override
	public void restoreExternals(World world, BlockPos blockPos, IBlockState blockState, TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		
	}
	
	public static void transformTile(LittleTile tile, ITransformation transformation) {
		transformBox(tile.box, transformation);
		if (tile.connection != null)
			transformConnection(tile.connection, transformation);
	}
	
	public static void transformBox(LittleTileBox box, ITransformation transformation) {
		Vec3d min = transformation.apply((double) box.minX, (double) box.minY, (double) box.minZ);
		Vec3d max = transformation.apply((double) box.maxX, (double) box.maxY, (double) box.maxZ);
		box.minX = (int) Math.min(min.x, max.x);
		box.minY = (int) Math.min(min.y, max.y);
		box.minZ = (int) Math.min(min.z, max.z);
		box.maxX = (int) Math.max(min.x, max.x);
		box.maxY = (int) Math.max(min.y, max.y);
		box.maxZ = (int) Math.max(min.z, max.z);
	}
	
	public static void transformConnection(IStructureConnector<LittleTile> connector, ITransformation transformation) {
		if (connector instanceof StructureLinkBaseRelative) {
			StructureLinkBaseRelative connect = (StructureLinkBaseRelative) connector;
			connect.coord = transformation.apply(connect.coord.getX(), connect.coord.getY(), connect.coord.getZ());
			Vec3d vec = transformation.apply((double) connect.identifier[0], (double) connect.identifier[1], (double) connect.identifier[2]);
			connect.identifier[0] = (int) vec.x;
			connect.identifier[1] = (int) vec.y;
			connect.identifier[2] = (int) vec.z;
		} else if (connector instanceof LittleTileIdentifierStructureAbsolute) {
			LittleTileIdentifierStructureAbsolute connect = (LittleTileIdentifierStructureAbsolute) connector;
			connect.pos = transformation.apply(connect.pos.getX(), connect.pos.getY(), connect.pos.getZ());
			Vec3d vec = transformation.apply((double) connect.identifier[0], (double) connect.identifier[1], (double) connect.identifier[2]);
			connect.identifier[0] = (int) vec.x;
			connect.identifier[1] = (int) vec.y;
			connect.identifier[2] = (int) vec.z;
		}
	}
}
