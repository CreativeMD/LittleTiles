package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class PlacementMode {
	
	private static LinkedHashMap<String, PlacementMode> modes = new LinkedHashMap<>();
	
	/** Tries to place all tiles, fails if the main block pos (the player aimed at)
	 * cannot be placed entirely. **/
	public static final PlacementMode normal = new PlaceModeNormal("placement.mode.default", PreviewMode.PREVIEWS, false);
	
	/** Tries to fill in the tiles where it is possible. **/
	public static final PlacementMode fill = new PlaceModeFill("placement.mode.fill", PreviewMode.PREVIEWS);
	
	/** Used for placing structures, should fail if it cannot place all tiles. **/
	public static final PlacementMode all = new PlaceModeAll("placement.mode.all", PreviewMode.PREVIEWS);
	
	/** Places all tiles no matter what is in the way. **/
	public static final PlacementMode overwrite = new PlaceModeOverwrite("placement.mode.overwrite", PreviewMode.PREVIEWS);
	
	/** Similar to overwrite only that replace will not place any tiles in the air. **/
	public static final PlacementMode replace = new PlaceModeReplace("placement.mode.replace", PreviewMode.LINES);
	
	public static PlacementMode getDefault() {
		return normal;
	}
	
	public static PlacementMode getStructureDefault() {
		return all;
	}
	
	public static PlacementMode getModeOrDefault(String name) {
		PlacementMode mode = getMode(name);
		if (mode != null)
			return mode;
		return getDefault();
	}
	
	public static PlacementMode getMode(String name) {
		return modes.get(name);
	}
	
	public static Collection<String> getModeNames() {
		return modes.keySet();
	}
	
	public static List<String> getLocalizedModeNames() {
		List<String> names = new ArrayList<>();
		for (String mode : getModeNames()) {
			names.add(I18n.translateToLocal(mode));
		}
		return names;
	}
	
	public final String name;
	public final boolean placeInside;
	private final PreviewMode mode;
	
	public PlacementMode(String name, PreviewMode mode, boolean placeInside) {
		this.name = name;
		this.mode = mode;
		this.placeInside = placeInside;
		this.modes.put(name, this);
	}
	
	public PreviewMode getPreviewMode() {
		if (LittleTiles.CONFIG.rendering.previewLines)
			return PreviewMode.LINES;
		return mode;
	}
	
	public abstract List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos);
	
	public abstract List<LittleTile> placeTile(TileEntityLittleTiles te, LittleTile tile, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, boolean requiresCollisionTest);
	
	@SideOnly(Side.CLIENT)
	public PlacementMode place() {
		return this;
	}
	
	public boolean canPlaceStructures() {
		return false;
	}
	
	public boolean checkAll() {
		return true;
	}
	
	public boolean shouldConvertBlock() {
		return false;
	}
	
	public static enum PreviewMode {
		LINES, PREVIEWS;
	}
	
}