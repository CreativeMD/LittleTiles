package com.creativemd.littletiles.common.utils.tooltip;

import com.creativemd.creativecore.common.utils.mc.ChatFormatting;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.text.translation.I18n;

public class TooltipUtils {
	
	public static String printRGB(int color) {
		int r = ColorUtils.getRed(color);
		int g = ColorUtils.getGreen(color);
		int b = ColorUtils.getBlue(color);
		int a = ColorUtils.getAlpha(color);
		return "" + ChatFormatting.RED + r + " " + ChatFormatting.GREEN + g + " " + ChatFormatting.BLUE + b + (a < 255 ? " " + ChatFormatting.WHITE + a : "");
	}
	
	public static String printVolume(double volume, boolean extended) {
		String text = "";
		int fullBlocks = (int) volume;
		int pixels = (int) Math.ceil(((volume - fullBlocks) * LittleGridContext.get().maxTilesPerBlock));
		
		if (fullBlocks > 0)
			text += fullBlocks + (extended ? " " + (fullBlocks == 1 ? I18n.translateToLocal("volume.unit.big.single") : I18n.translateToLocal("volume.unit.big.multiple")) : I18n.translateToLocal("volume.unit.big.short"));
		if (pixels > 0) {
			if (fullBlocks > 0)
				text += " ";
			text += pixels + (extended ? " " + (pixels == 1 ? I18n.translateToLocal("volume.unit.small.single") : I18n.translateToLocal("volume.unit.small.multiple")) : I18n.translateToLocal("volume.unit.small.short"));
		}
		
		return text;
	}
}
