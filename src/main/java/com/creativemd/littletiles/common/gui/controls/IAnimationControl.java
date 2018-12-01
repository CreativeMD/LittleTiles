package com.creativemd.littletiles.common.gui.controls;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.math.AxisAlignedBB;

public interface IAnimationControl {
	
	public void onLoaded(EntityAnimation animation, LittleTileBox entireBox, LittleGridContext context, AxisAlignedBB box, LittlePreviews previews);
	
}
