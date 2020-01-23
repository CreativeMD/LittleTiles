package com.creativemd.littletiles.common.utils.selection.selector;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3i;

public class ColorSelector extends TileSelector {
	
	public Vec3i color;
	
	public ColorSelector(int color) {
		this.color = ColorUtils.IntToRGB(color);
	}
	
	public ColorSelector() {
		
	}
	
	@Override
	protected void saveNBT(NBTTagCompound nbt) {
		nbt.setInteger("color", ColorUtils.RGBToInt(color));
	}
	
	@Override
	protected void loadNBT(NBTTagCompound nbt) {
		color = ColorUtils.IntToRGB(nbt.getInteger("color"));
	}
	
	@Override
	public boolean is(LittleTile tile) {
		if (color.getX() == 255 && color.getY() == 255 && color.getZ() == 255)
			return !(tile instanceof LittleTileColored);
		else if (tile instanceof LittleTileColored)
			return ColorUtils.IntToRGB(((LittleTileColored) tile).color).equals(color);
		return false;
	}
	
}
