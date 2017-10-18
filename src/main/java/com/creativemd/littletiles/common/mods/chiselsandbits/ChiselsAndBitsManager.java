package com.creativemd.littletiles.common.mods.chiselsandbits;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;

public class ChiselsAndBitsManager {
	
	private static final String chiselsandbitsID = "chiselsandbits";
	
	private static boolean isinstalled = Loader.isModLoaded(chiselsandbitsID);

	public static boolean isInstalled()
	{
		return isinstalled;
	}
	
	/**Keeping the grid size of Chisels & Bits variable, maybe it does change sometime**/
	public static int convertingFrom = 16;
	
	public static List<LittleTilePreview> getPreviews(ItemStack stack)
	{
		if(isInstalled())
			return ChiselsAndBitsInteractor.getPreviews(stack);
		return null;
	}
	
	public static boolean isChiselsAndBitsStructure(ItemStack stack)
	{
		if(isInstalled())
			return ChiselsAndBitsInteractor.isChiselsAndBitsStructure(stack);
		return false;
	}
	
	public static List<LittleTilePreview> getPreviews(TileEntity te)
	{
		if(isInstalled())
			return ChiselsAndBitsInteractor.getPreviews(te);
		return null;
	}
	
	public static List<LittleTile> getTiles(TileEntity te)
	{
		if(isInstalled())
			return ChiselsAndBitsInteractor.getTiles(te);
		return null;
	}
}
