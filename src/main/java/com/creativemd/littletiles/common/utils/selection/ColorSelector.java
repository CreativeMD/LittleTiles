package com.creativemd.littletiles.common.utils.selection;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;

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
		if(color.getX() == 255 && color.getY() == 255 && color.getZ() == 255)
			return !(tile instanceof LittleTileBlockColored);
		else if(tile instanceof LittleTileBlockColored)
			return ColorUtils.IntToRGB(((LittleTileBlockColored) tile).color).equals(color);
		return false;
	}

}
