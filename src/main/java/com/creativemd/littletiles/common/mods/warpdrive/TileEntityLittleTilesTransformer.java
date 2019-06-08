package com.creativemd.littletiles.common.mods.warpdrive;

import java.lang.reflect.InvocationTargetException;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;

import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.WarpDriveText;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.model.ITransformation;

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
		TileEntityLittleTiles te = new TileEntityLittleTiles();
		te.readFromNBT(nbtTileEntity);
		for (LittleTile tile : te.getTiles()) {
			tile.transform(transformation);
		}
		te.writeToNBT(nbtTileEntity);
		return 0;
	}
	
	@Override
	public void restoreExternals(World world, BlockPos blockPos, IBlockState blockState, TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		
	}
	
}
