package com.creativemd.littletiles.common.utils.placing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.combine.BasicCombiner;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.placing.PlacementMode.SelectionMode;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class PlacementMode {
	
	private static LinkedHashMap<String, PlacementMode> modes = new LinkedHashMap<>();
	
	/**Tries to place all tiles, fails if the main blockpos (the player aimed at) cannot be placed entirely.**/
	public static PlacementMode normal = new PlaceModeNormal("placement.mode.default", SelectionMode.PREVIEWS);
	
	/**Tries to fill in the tiles where it is possible.**/
	public static PlacementMode fill = new PlaceModeFill("placement.mode.fill", SelectionMode.PREVIEWS);
	
	/**Used for placing structures, should fail if it cannot place all tiles.**/
	public static PlacementMode all = new PlaceModeAll("placement.mode.all", SelectionMode.PREVIEWS);
	
	/**Places all tiles no matter what is in the way.**/
	public static PlacementMode overwrite = new PlaceModeOverwrite("placement.mode.overwrite", SelectionMode.PREVIEWS);
	
	/**Similar to overwrite only that replace will not place any tiles in the air.**/
	public static PlacementMode replace = new PlaceModeReplace("placement.mode.replace", SelectionMode.LINES);
	
	public static PlacementMode getDefault()
	{
		return normal;
	}
	
	public static PlacementMode getStructureDefault()
	{
		return all;
	}
	
	public static PlacementMode getModeOrDefault(String name)
	{
		PlacementMode mode = getMode(name);
		if(mode != null)
			return mode;
		return getDefault();
	}
	
	public static PlacementMode getMode(String name)
	{
		return modes.get(name);
	}
	
	public static Collection<String> getModeNames()
	{
		return modes.keySet();
	}
	
	public static List<String> getLocalizedModeNames()
	{
		List<String> names = new ArrayList<>();
		for (String mode : getModeNames()) {
			names.add(I18n.translateToLocal(mode));
		}
		return names;
	}
	
	public final String name;
	
	public final SelectionMode mode;
	
	public PlacementMode(String name, SelectionMode mode) {
		this.name = name;
		this.mode = mode;
		this.modes.put(name, this);
	}
	
	public abstract List<BlockPos> getCoordsToCheck(HashMapList<BlockPos, PlacePreviewTile> splittedTiles, BlockPos pos);
	
	public abstract List<LittleTile> placeTile(TileEntityLittleTiles te, LittleTile tile, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, boolean requiresCollisionTest);
	
	@SideOnly(Side.CLIENT)
	public PlacementMode place()
	{
		return this;
	}
	
	public boolean canPlaceStructures()
	{
		return false;
	}
	
	public boolean checkAll()
	{
		return true;
	}
	
	public static enum SelectionMode {
		LINES,
		PREVIEWS;
	}
}