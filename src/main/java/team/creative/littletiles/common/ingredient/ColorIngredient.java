package team.creative.littletiles.common.ingredient;

import java.util.List;

import net.minecraft.ChatFormatting;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.tile.LittleTile;

public class ColorIngredient extends LittleIngredient<ColorIngredient> {
    
    private int limitBlack = -1;
    private int limitCyan = -1;
    private int limitMagenta = -1;
    private int limitYellow = -1;
    
    public int black;
    public int cyan;
    public int magenta;
    public int yellow;
    
    public ColorIngredient() {
        this.black = this.cyan = this.magenta = this.yellow = 0;
    }
    
    public ColorIngredient(int[] array) {
        if (array.length != 4)
            throw new IllegalArgumentException("Invalid array " + array + "!");
        this.black = array[0];
        this.cyan = array[1];
        this.magenta = array[2];
        this.yellow = array[3];
    }
    
    public ColorIngredient(int black, int cyan, int magenta, int yellow) {
        this.black = black;
        this.cyan = cyan;
        this.magenta = magenta;
        this.yellow = yellow;
    }
    
    public ColorIngredient setLimit(int limit) {
        this.limitBlack = this.limitCyan = this.limitMagenta = this.limitYellow = limit;
        return this;
    }
    
    public ColorIngredient setLimit(int black, int cyan, int magenta, int yellow) {
        this.limitBlack = black;
        this.limitCyan = cyan;
        this.limitMagenta = magenta;
        this.limitYellow = yellow;
        return this;
    }
    
    public int[] getArray() {
        return new int[] { black, cyan, magenta, yellow };
    }
    
    @Override
    public TextBuilder toText() {
        TextBuilder text = new TextBuilder();
        if (black > 0)
            text.text(getBlackDescription());
        if (cyan > 0)
            text.text(getCyanDescription());
        if (magenta > 0)
            text.text(getMagentaDescription());
        if (yellow > 0)
            text.text(getYellowDescription());
        return text;
    }
    
    private static String getUnit(int number) {
        if (number == 1)
            return LanguageUtils.translate("color.unit.single");
        return LanguageUtils.translate("color.unit.multiple");
    }
    
    public String getBlackDescription() {
        return TooltipUtils.printNumber(black) + " " + ChatFormatting.DARK_GRAY + LanguageUtils.translate("color.unit.black") + ChatFormatting.WHITE + " " + getUnit(black);
    }
    
    public String getCyanDescription() {
        return TooltipUtils.printNumber(cyan) + " " + ChatFormatting.AQUA + LanguageUtils.translate("color.unit.cyan") + ChatFormatting.WHITE + " " + getUnit(cyan);
    }
    
    public String getMagentaDescription() {
        return TooltipUtils.printNumber(magenta) + " " + ChatFormatting.LIGHT_PURPLE + LanguageUtils
                .translate("color.unit.magenta") + ChatFormatting.WHITE + " " + getUnit(magenta);
    }
    
    public String getYellowDescription() {
        return TooltipUtils.printNumber(yellow) + " " + ChatFormatting.YELLOW + LanguageUtils.translate("color.unit.yellow") + ChatFormatting.WHITE + " " + getUnit(yellow);
    }
    
    @Override
    public String toString() {
        return "[back=" + black + ",cyan=" + cyan + ",magenta=" + magenta + ",yellow=" + yellow + "]";
    }
    
    @Override
    public ColorIngredient add(ColorIngredient ingredient) {
        if (ingredient == null || ingredient.isEmpty())
            return null;
        
        ColorIngredient remaining = null;
        this.black += ingredient.black;
        if (this.limitBlack >= 0 && this.black > limitBlack) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.black = this.black - this.limitBlack;
            this.black = limitBlack;
        }
        
        this.cyan += ingredient.cyan;
        if (this.limitCyan >= 0 && this.cyan > limitCyan) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.cyan = this.cyan - this.limitCyan;
            this.cyan = limitCyan;
        }
        this.magenta += ingredient.magenta;
        if (this.limitMagenta >= 0 && this.magenta > limitMagenta) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.magenta = this.magenta - this.limitMagenta;
            this.magenta = limitMagenta;
        }
        this.yellow += ingredient.yellow;
        if (this.limitYellow >= 0 && this.yellow > limitYellow) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.yellow = this.yellow - this.limitYellow;
            this.yellow = limitYellow;
        }
        return remaining;
    }
    
    @Override
    public ColorIngredient sub(ColorIngredient ingredient) {
        if (ingredient == null || ingredient.isEmpty())
            return null;
        
        ColorIngredient remaining = null;
        this.black -= ingredient.black;
        if (this.black < 0) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.black = -this.black;
            this.black = limitBlack;
        }
        this.cyan -= ingredient.cyan;
        if (this.cyan < 0) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.cyan = -this.cyan;
            this.cyan = limitCyan;
        }
        this.magenta -= ingredient.magenta;
        if (this.magenta < 0) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.magenta = -this.magenta;
            this.magenta = limitMagenta;
        }
        this.yellow -= ingredient.yellow;
        if (this.yellow < 0) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.yellow = -this.yellow;
            this.yellow = limitYellow;
        }
        return remaining;
    }
    
    @Override
    public ColorIngredient copy() {
        ColorIngredient copy = new ColorIngredient(black, cyan, magenta, yellow);
        copy.limitBlack = limitBlack;
        copy.limitCyan = limitCyan;
        copy.limitMagenta = limitMagenta;
        copy.limitYellow = limitYellow;
        return copy;
    }
    
    @Override
    public boolean isEmpty() {
        return black == 0 && cyan == 0 && magenta == 0 && yellow == 0;
    }
    
    @Override
    public void scale(int count) {
        this.black *= count;
        this.cyan *= count;
        this.magenta *= count;
        this.yellow *= count;
    }
    
    @Override
    public void scaleAdvanced(double scale) {
        this.black = (int) Math.ceil(this.black * scale);
        this.cyan = (int) Math.ceil(this.cyan * scale);;
        this.magenta = (int) Math.ceil(this.magenta * scale);;
        this.yellow = (int) Math.ceil(this.yellow * scale);;
    }
    
    @Override
    public int getMinimumCount(ColorIngredient other, int availableCount) {
        int count = -1;
        if (this.black > 0 && other.black > 0)
            count = Math.max(count, this.black / other.black);
        if (this.cyan > 0 && other.cyan > 0)
            count = Math.max(count, this.cyan / other.cyan);
        if (this.magenta > 0 && other.magenta > 0)
            count = Math.max(count, this.magenta / other.magenta);
        if (this.yellow > 0 && other.yellow > 0)
            count = Math.max(count, this.yellow / other.yellow);
        return Math.min(availableCount, count);
    }
    
    public void scale(double scale) {
        this.black = (int) Math.ceil(this.black * scale);
        this.cyan = (int) Math.ceil(this.cyan * scale);
        this.magenta = (int) Math.ceil(this.magenta * scale);
        this.yellow = (int) Math.ceil(this.yellow * scale);
    }
    
    public void scaleLoose(double scale) {
        this.black = (int) Math.floor(this.black * scale);
        this.cyan = (int) Math.floor(this.cyan * scale);
        this.magenta = (int) Math.floor(this.magenta * scale);
        this.yellow = (int) Math.floor(this.yellow * scale);
    }
    
    @Override
    public String print(List<Object> objects) {
        String text = "";
        if (black > 0)
            text += getBlackDescription();
        if (cyan > 0)
            text += (text.isEmpty() ? "" : " ") + getCyanDescription();
        if (magenta > 0)
            text += (text.isEmpty() ? "" : " ") + getMagentaDescription();
        if (yellow > 0)
            text += (text.isEmpty() ? "" : " ") + getYellowDescription();
        return text;
    }
    
    public static float dyeToBlockPercentage = 4096;
    public static int bottleSize = (int) (dyeToBlockPercentage * 64);
    
    public static ColorIngredient getColors(LittleTile tile, double volume) {
        if (tile.hasColor()) {
            ColorIngredient color = getColors(tile.color);
            color.scale(volume);
            return color;
        }
        return null;
    }
    
    public static ColorIngredient getColors(int color, int defaultColor, double volume) {
        if (color != defaultColor) {
            ColorIngredient ingredient = getColors(color);
            ingredient.scale(volume);
            return ingredient;
        }
        return null;
    }
    
    public static ColorIngredient getColors(LittleGrid grid, LittleTile tile) {
        return getColors(tile, tile.getPercentVolume(grid));
    }
    
    public static ColorIngredient getColors(int color) {
        float cmyk_scale = dyeToBlockPercentage;
        
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        
        if (r == 0 && g == 0 && b == 0)
            return new ColorIngredient((int) cmyk_scale, 0, 0, 0);
        
        float c = 1 - r / 255F;
        float m = 1 - g / 255F;
        float y = 1 - b / 255F;
        
        float min_cmy = Math.min(c, Math.min(m, y));
        c = (c - min_cmy) / (1 - min_cmy);
        m = (m - min_cmy) / (1 - min_cmy);
        y = (y - min_cmy) / (1 - min_cmy);
        float k = min_cmy;
        return new ColorIngredient((int) (k * cmyk_scale), (int) (c * cmyk_scale), (int) (m * cmyk_scale), (int) (y * cmyk_scale));
    }
    
}
