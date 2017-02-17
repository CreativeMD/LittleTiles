package com.creativemd.littletiles.common.mods.chiselsandbits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ChiselsAndBitsInteractor {
	
	public static boolean isChiselsAndBitsStructure(ItemStack stack)
	{
		Block block = Block.getBlockFromItem(stack.getItem());
		Map blocks = ChiselsAndBits.getBlocks().getConversions();
		for (Iterator iterator = blocks.values().iterator(); iterator.hasNext();) {
			Block block2 = (Block) iterator.next();
			if(block == block2)
				return true;
		}
		return false;
	}
	
	public static ArrayList<LittleTilePreview> getPreviews(VoxelBlob blob)
	{
		ArrayList<LittleTile> tiles = new ArrayList<>();
		for (int x = 0; x < ChiselsAndBitsManager.convertingFrom; x++) {
			for (int y = 0; y < ChiselsAndBitsManager.convertingFrom; y++) {
				for (int z = 0; z < ChiselsAndBitsManager.convertingFrom; z++) {
					IBlockState state = ModUtil.getStateById(blob.get(x, y, z));
					if(state.getBlock() != Blocks.AIR)
					{
						LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
						tile.boundingBoxes.add(new LittleTileBox(new LittleTileVec(x, y, z)));
						tiles.add(tile);
					}
				}
			}
		}
		TileEntityLittleTiles.combineTilesList(tiles);
		ArrayList<LittleTilePreview> previews = new ArrayList<>();
		for (int i = 0; i < tiles.size(); i++) {
			previews.add(tiles.get(i).getPreviewTile());
		}
		return previews;
	}
	
	
	public static ArrayList<LittleTilePreview> getPreviews(ItemStack stack)
	{
		if(isChiselsAndBitsStructure(stack))
			return getPreviews(ModUtil.getBlobFromStack(stack, null));
		return null;
	}
	
	public static ArrayList<LittleTilePreview> getPreviews(TileEntity te)
	{
		if(te instanceof TileEntityBlockChiseled)
			return getPreviews(((TileEntityBlockChiseled) te).getBlob());
		return null;
	}
	
}
