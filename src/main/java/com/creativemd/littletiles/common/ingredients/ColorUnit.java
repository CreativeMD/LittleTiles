package com.creativemd.littletiles.common.ingredients;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.text.translation.I18n;

public class ColorUnit {
	
	public int BLACK;
	public int RED;
	public int GREEN;
	public int BLUE;
	
	public ColorUnit() {
		this.BLACK = 0;
		this.RED = 0;
		this.GREEN = 0;
		this.BLUE = 0;
	}
	
	public ColorUnit(int[] array) {
		if(array.length != 4)
			throw new IllegalArgumentException("Invalid array " + array + "!");
		this.BLACK = array[0];
		this.RED = array[1];
		this.GREEN = array[2];
		this.BLUE = array[3];
	}
	
	public ColorUnit(int black, int red, int green, int blue) {
		this.BLACK = black;
		this.RED = red;
		this.GREEN = green;
		this.BLUE = blue;
	}
	
	public int[] getArray()
	{
		return new int[]{BLACK, RED, GREEN, BLUE};
	}
	
	public String getDescription()
	{
		String description = "";
		if(BLACK > 0)
			description += BLACK + " " + I18n.translateToLocal("color.unit.black") + " units";
		if(RED > 0)
			description += RED + " " + I18n.translateToLocal("color.unit.red") + " units";
		if(GREEN > 0)
			description += GREEN + " " + I18n.translateToLocal("color.unit.green") + " units";
		if(BLUE > 0)
			description += BLUE + " " + I18n.translateToLocal("color.unit.blue") + " units";
		return description;
	}
	
	@Override
	public String toString() {
		return "[back=" + BLACK + ",red=" + RED + ",green=" + GREEN + ",blue=" + BLUE + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ColorUnit)
			return BLACK == ((ColorUnit) obj).BLACK && RED == ((ColorUnit) obj).RED && GREEN == ((ColorUnit) obj).GREEN && BLUE == ((ColorUnit) obj).BLUE;
		return false;
	}
	
	@Override
	public int hashCode() {
		return BLACK + RED + GREEN + BLUE;
	}
	
	public void scale(double scale)
	{
		this.BLACK *= scale;
		this.RED *= scale;
		this.GREEN *= scale;
		this.BLUE *= scale;
	}
	
	public void subColorUnit(ColorUnit unit)
	{
		if(unit == null)
			return ;
		this.BLACK -= unit.BLACK;
		this.RED -= unit.RED;
		this.GREEN -= unit.GREEN;
		this.BLUE -= unit.BLUE;
	}
	
	public void addColorUnit(ColorUnit unit)
	{
		if(unit == null)
			return ;
		this.BLACK += unit.BLACK;
		this.RED += unit.RED;
		this.GREEN += unit.GREEN;
		this.BLUE += unit.BLUE;
	}
	
	public ColorUnit copy()
	{
		return new ColorUnit(BLACK, RED, GREEN, BLUE);
	}
	
	public static int dyeToBlockPercentage = 1;
	
	public static ColorUnit getColors(int color)
	{
		double percent = (255 * 0.1 * dyeToBlockPercentage)/ 3D;
		int maxPerColor = (int) (percent*255);
		int r = (int) ((color >> 16 & 255) * percent);
		int g = (int) ((color >> 8 & 255) * percent);
		int b = (int) ((color & 255) * percent);
        return new ColorUnit(maxPerColor - r + maxPerColor - g + maxPerColor - b, r, g, b);
	}
	
	public static ColorUnit getRequiredColors(LittleTilePreview preview, double volume)
	{
		if(preview.hasColor())
		{
			ColorUnit color = getRequiredColors(preview.getColor());
			color.BLACK *= volume;
			color.RED *= volume;
			color.GREEN *= volume;
			color.BLUE *= volume;
			return color;
		}
		return null;
	}
	
	public static ColorUnit getRequiredColors(LittleTilePreview preview)
	{
		if(preview.hasColor())
		{
			ColorUnit color = getRequiredColors(preview.getColor());
			color.BLACK *= preview.size.getPercentVolume();
			color.RED *= preview.size.getPercentVolume();
			color.GREEN *= preview.size.getPercentVolume();
			color.BLUE *= preview.size.getPercentVolume();
			return color;
		}
		return null;
	}
	
	public static ColorUnit getRequiredColors(int color)
	{
		double percent = (255 * 0.1 * dyeToBlockPercentage)/ 3D;
		int maxPerColor = (int) (percent*255);
		int r = (int) ((color >> 16 & 255) * percent);
		int g = (int) ((color >> 8 & 255) * percent);
		int b = (int) ((color & 255) * percent);
		int lowest = Math.min(r, Math.min(g, b));
		r -= lowest;
		g -= lowest;
		b -= lowest;
		int remaining = maxPerColor - lowest;
		
		return new ColorUnit(maxPerColor - lowest + remaining - r + remaining - g + remaining - b, r, g, b);
	}

	public boolean isEmpty() {
		return BLACK == 0 && RED == 0 && GREEN == 0 && BLUE == 0;
	}

	public void drain(ColorUnit toDrain)
	{
		int drain = Math.min(BLACK, toDrain.BLACK);
		BLACK -= drain;
		toDrain.BLACK -= drain;
		
		drain = Math.min(RED, toDrain.RED);
		RED -= drain;
		toDrain.RED -= drain;
		
		drain = Math.min(GREEN, toDrain.GREEN);
		GREEN -= drain;
		toDrain.GREEN -= drain;
		
		drain = Math.min(BLUE, toDrain.BLUE);
		BLUE -= drain;
		toDrain.BLUE -= drain;
	}
	
}
