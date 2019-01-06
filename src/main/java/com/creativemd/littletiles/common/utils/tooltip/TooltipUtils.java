package com.creativemd.littletiles.common.utils.tooltip;

import com.creativemd.creativecore.common.utils.mc.ChatFormatting;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;

public class TooltipUtils {
	
	public static String printRGB(int color) {
		int r = ColorUtils.getRed(color);
		int g = ColorUtils.getGreen(color);
		int b = ColorUtils.getBlue(color);
		int a = ColorUtils.getAlpha(color);
		return "" + ChatFormatting.RED + r + " " + ChatFormatting.GREEN + g + " " + ChatFormatting.BLUE + b + (a < 255 ? " " + ChatFormatting.WHITE + a : "");
	}
	
}
