package com.creativemd.littletiles.common.ingredients;

import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.util.text.translation.I18n;

public class ColorUnit {
	
	public int BLACK;
	public int CYAN;
	public int MAGENTA;
	public int YELLOW;
	
	public ColorUnit() {
		this.BLACK = 0;
		this.CYAN = 0;
		this.MAGENTA = 0;
		this.YELLOW = 0;
	}
	
	public ColorUnit(int[] array) {
		if (array.length != 4)
			throw new IllegalArgumentException("Invalid array " + array + "!");
		this.BLACK = array[0];
		this.CYAN = array[1];
		this.MAGENTA = array[2];
		this.YELLOW = array[3];
	}
	
	public ColorUnit(int black, int cyan, int magenta, int yellow) {
		this.BLACK = black;
		this.CYAN = cyan;
		this.MAGENTA = magenta;
		this.YELLOW = yellow;
	}
	
	public int[] getArray() {
		return new int[] { BLACK, CYAN, MAGENTA, YELLOW };
	}
	
	private static String getUnit(int number) {
		if (number == 1)
			return I18n.translateToLocal("color.unit.single");
		return I18n.translateToLocal("color.unit.multiple");
	}
	
	public String getBlackDescription() {
		return BLACK + " " + ChatFormatting.DARK_GRAY + I18n.translateToLocal("color.unit.black") + ChatFormatting.WHITE + " " + getUnit(BLACK);
	}
	
	public String getCyanDescription() {
		return CYAN + " " + ChatFormatting.AQUA + I18n.translateToLocal("color.unit.cyan") + ChatFormatting.WHITE + " " + getUnit(CYAN);
	}
	
	public String getMagentaDescription() {
		return MAGENTA + " " + ChatFormatting.LIGHT_PURPLE + I18n.translateToLocal("color.unit.magenta") + ChatFormatting.WHITE + " " + getUnit(MAGENTA);
	}
	
	public String getYellowDescription() {
		return YELLOW + " " + ChatFormatting.YELLOW + I18n.translateToLocal("color.unit.yellow") + ChatFormatting.WHITE + " " + getUnit(YELLOW);
	}
	
	public String getDescription() {
		String description = "";
		if (BLACK > 0)
			description += getBlackDescription();
		if (CYAN > 0)
			description += getCyanDescription();
		if (MAGENTA > 0)
			description += getMagentaDescription();
		if (YELLOW > 0)
			description += getYellowDescription();
		return description;
	}
	
	@Override
	public String toString() {
		return "[back=" + BLACK + ",cyan=" + CYAN + ",magenta=" + MAGENTA + ",yellow=" + YELLOW + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ColorUnit)
			return BLACK == ((ColorUnit) obj).BLACK && CYAN == ((ColorUnit) obj).CYAN && MAGENTA == ((ColorUnit) obj).MAGENTA && YELLOW == ((ColorUnit) obj).YELLOW;
		return false;
	}
	
	@Override
	public int hashCode() {
		return BLACK + CYAN + MAGENTA + YELLOW;
	}
	
	public void scale(double scale) {
		this.BLACK = (int) Math.ceil(this.BLACK * scale);
		this.CYAN = (int) Math.ceil(this.CYAN * scale);
		this.MAGENTA = (int) Math.ceil(this.MAGENTA * scale);
		this.YELLOW = (int) Math.ceil(this.YELLOW * scale);
	}
	
	public void scaleLoose(double scale) {
		this.BLACK = (int) Math.floor(this.BLACK * scale);
		this.CYAN = (int) Math.floor(this.CYAN * scale);
		this.MAGENTA = (int) Math.floor(this.MAGENTA * scale);
		this.YELLOW = (int) Math.floor(this.YELLOW * scale);
	}
	
	public void subColorUnit(ColorUnit unit) {
		if (unit == null)
			return;
		this.BLACK -= unit.BLACK;
		this.CYAN -= unit.CYAN;
		this.MAGENTA -= unit.MAGENTA;
		this.YELLOW -= unit.YELLOW;
	}
	
	public void addColorUnit(ColorUnit unit) {
		if (unit == null)
			return;
		this.BLACK += unit.BLACK;
		this.CYAN += unit.CYAN;
		this.MAGENTA += unit.MAGENTA;
		this.YELLOW += unit.YELLOW;
	}
	
	public ColorUnit copy() {
		return new ColorUnit(BLACK, CYAN, MAGENTA, YELLOW);
	}
	
	public static float dyeToBlockPercentage = 4096;
	
	/* public static ColorUnit getColors(int color) { double percent = (255 * 0.1 *
	 * dyeToBlockPercentage)/ 3D; int maxPerColor = (int) (percent*255); int r =
	 * (int) ((color >> 16 & 255) * percent); int g = (int) ((color >> 8 & 255) *
	 * percent); int b = (int) ((color & 255) * percent); return new
	 * ColorUnit(maxPerColor - r + maxPerColor - g + maxPerColor - b, r, g, b); } */
	
	public static ColorUnit getColors(LittleTilePreview preview, double volume) {
		if (preview.hasColor()) {
			ColorUnit color = getColors(preview.getColor());
			color.scale(volume);
			return color;
		}
		return null;
	}
	
	public static ColorUnit getColors(LittleGridContext context, LittleTilePreview preview) {
		return getColors(preview, preview.getPercentVolume(context));
	}
	
	public static ColorUnit getColors(int color) {
		/* if(ColorUtils.isWhite(color)) return new ColorUnit(); double percent = (255 *
		 * 0.1 * dyeToBlockPercentage)/ 3D; int maxPerColor = (int)
		 * Math.ceil(percent*255); int r = (int) ((color >> 16 & 255) * percent); int g
		 * = (int) ((color >> 8 & 255) * percent); int b = (int) ((color & 255) *
		 * percent); int lowest = Math.min(r, Math.min(g, b)); r -= lowest; g -= lowest;
		 * b -= lowest; int remaining = maxPerColor - lowest;
		 * 
		 * return new ColorUnit(maxPerColor - lowest + remaining - r + remaining - g +
		 * remaining - b, r, g, b); */
		
		float cmyk_scale = dyeToBlockPercentage;
		
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		
		if (r == 0 && g == 0 && b == 0)
			return new ColorUnit((int) cmyk_scale, 0, 0, 0);
		
		float c = 1 - r / 255F;
		float m = 1 - g / 255F;
		float y = 1 - b / 255F;
		
		float min_cmy = Math.min(c, Math.min(m, y));
		c = (c - min_cmy) / (1 - min_cmy);
		m = (m - min_cmy) / (1 - min_cmy);
		y = (y - min_cmy) / (1 - min_cmy);
		float k = min_cmy;
		return new ColorUnit((int) (k * cmyk_scale), (int) (c * cmyk_scale), (int) (m * cmyk_scale), (int) (y * cmyk_scale));
	}
	
	public boolean isEmpty() {
		return BLACK == 0 && CYAN == 0 && MAGENTA == 0 && YELLOW == 0;
	}
	
	public void drain(ColorUnit toDrain) {
		int drain = Math.min(BLACK, toDrain.BLACK);
		BLACK -= drain;
		toDrain.BLACK -= drain;
		
		drain = Math.min(CYAN, toDrain.CYAN);
		CYAN -= drain;
		toDrain.CYAN -= drain;
		
		drain = Math.min(MAGENTA, toDrain.MAGENTA);
		MAGENTA -= drain;
		toDrain.MAGENTA -= drain;
		
		drain = Math.min(YELLOW, toDrain.YELLOW);
		YELLOW -= drain;
		toDrain.YELLOW -= drain;
	}
	
}
