package team.creative.littletiles.common.ingredient;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.LittleGrid;

public class ColorIngredient extends LittleIngredient<ColorIngredient> {
    
    public static final double DYE_TO_BLOCK_PERCENTAGE = 4096;
    public static final int BOTTLE_SIZE = (int) (DYE_TO_BLOCK_PERCENTAGE * 64);
    
    public static ColorIngredient getColors(LittleElement tile, double volume) {
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
        double cmyk_scale = DYE_TO_BLOCK_PERCENTAGE;
        
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        int a = color >> 24 & 255;
        
        int alpha = (int) ((1 - (a / 255D)) * cmyk_scale);
        
        if (r == 0 && g == 0 && b == 0 && a == 0)
            return new ColorIngredient((int) cmyk_scale, 0, 0, 0, alpha);
        
        float c = 1 - r / 255F;
        float m = 1 - g / 255F;
        float y = 1 - b / 255F;
        
        float min_cmy = Math.min(c, Math.min(m, y));
        c = (c - min_cmy) / (1 - min_cmy);
        m = (m - min_cmy) / (1 - min_cmy);
        y = (y - min_cmy) / (1 - min_cmy);
        float k = min_cmy;
        return new ColorIngredient(Mth.ceil(k * cmyk_scale), Mth.ceil(c * cmyk_scale), Mth.ceil(m * cmyk_scale), Mth.ceil(y * cmyk_scale), alpha);
    }
    
    private int limitBlack = -1;
    private int limitCyan = -1;
    private int limitMagenta = -1;
    private int limitYellow = -1;
    private int limitAlpha = -1;
    
    public int black;
    public int cyan;
    public int magenta;
    public int yellow;
    public int alpha;
    
    public ColorIngredient() {
        this.black = this.cyan = this.magenta = this.yellow = this.alpha = 0;
    }
    
    public ColorIngredient(int[] array) {
        if (array.length < 4)
            throw new IllegalArgumentException("Invalid array " + array + "!");
        this.black = array[0];
        this.cyan = array[1];
        this.magenta = array[2];
        this.yellow = array[3];
        if (array.length > 4)
            this.alpha = array[4];
    }
    
    public ColorIngredient(int black, int cyan, int magenta, int yellow, int alpha) {
        this.black = black;
        this.cyan = cyan;
        this.magenta = magenta;
        this.yellow = yellow;
        this.alpha = alpha;
    }
    
    public ColorIngredient setLimit(int limit) {
        this.limitBlack = this.limitCyan = this.limitMagenta = this.limitYellow = this.limitAlpha = limit;
        return this;
    }
    
    public ColorIngredient setLimit(int black, int cyan, int magenta, int yellow, int alpha) {
        this.limitBlack = black;
        this.limitCyan = cyan;
        this.limitMagenta = magenta;
        this.limitYellow = yellow;
        this.limitAlpha = alpha;
        return this;
    }
    
    public int[] getArray() {
        return new int[] { black, cyan, magenta, yellow, alpha };
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
        if (alpha > 0)
            text.text(getAlphaDescription());
        return text;
    }
    
    private static String getUnit(int number) {
        if (number == 1)
            return LanguageUtils.translate("color.unit.single");
        return LanguageUtils.translate("color.unit.multiple");
    }
    
    public String getBlackDescription() {
        return TooltipUtils.print(black) + " " + ChatFormatting.DARK_GRAY + LanguageUtils.translate("color.unit.black") + ChatFormatting.WHITE + " " + getUnit(black);
    }
    
    public String getCyanDescription() {
        return TooltipUtils.print(cyan) + " " + ChatFormatting.AQUA + LanguageUtils.translate("color.unit.cyan") + ChatFormatting.WHITE + " " + getUnit(cyan);
    }
    
    public String getMagentaDescription() {
        return TooltipUtils.print(magenta) + " " + ChatFormatting.LIGHT_PURPLE + LanguageUtils.translate("color.unit.magenta") + ChatFormatting.WHITE + " " + getUnit(magenta);
    }
    
    public String getYellowDescription() {
        return TooltipUtils.print(yellow) + " " + ChatFormatting.YELLOW + LanguageUtils.translate("color.unit.yellow") + ChatFormatting.WHITE + " " + getUnit(yellow);
    }
    
    public String getAlphaDescription() {
        return TooltipUtils.print(alpha) + " " + ChatFormatting.GRAY + LanguageUtils.translate("color.unit.alpha") + ChatFormatting.WHITE + " " + getUnit(alpha);
    }
    
    @Override
    public String toString() {
        return "[back=" + black + ",cyan=" + cyan + ",magenta=" + magenta + ",yellow=" + yellow + ",alpha=" + alpha + "]";
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
        this.alpha += ingredient.alpha;
        if (this.limitAlpha >= 0 && this.alpha > limitAlpha) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.alpha = this.alpha - this.limitAlpha;
            this.alpha = limitAlpha;
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
            this.black = 0;
        }
        this.cyan -= ingredient.cyan;
        if (this.cyan < 0) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.cyan = -this.cyan;
            this.cyan = 0;
        }
        this.magenta -= ingredient.magenta;
        if (this.magenta < 0) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.magenta = -this.magenta;
            this.magenta = 0;
        }
        this.yellow -= ingredient.yellow;
        if (this.yellow < 0) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.yellow = -this.yellow;
            this.yellow = 0;
        }
        this.alpha -= ingredient.alpha;
        if (this.alpha < 0) {
            if (remaining == null)
                remaining = new ColorIngredient();
            remaining.alpha = -this.alpha;
            this.alpha = 0;
        }
        return remaining;
    }
    
    @Override
    public ColorIngredient copy() {
        ColorIngredient copy = new ColorIngredient(black, cyan, magenta, yellow, alpha);
        copy.limitBlack = limitBlack;
        copy.limitCyan = limitCyan;
        copy.limitMagenta = limitMagenta;
        copy.limitYellow = limitYellow;
        copy.limitAlpha = limitAlpha;
        return copy;
    }
    
    @Override
    public boolean isEmpty() {
        return black == 0 && cyan == 0 && magenta == 0 && yellow == 0 && alpha == 0;
    }
    
    @Override
    public void scale(int count) {
        this.black *= count;
        this.cyan *= count;
        this.magenta *= count;
        this.yellow *= count;
        this.alpha *= count;
    }
    
    @Override
    public void scaleAdvanced(double scale) {
        this.black = (int) Math.ceil(this.black * scale);
        this.cyan = (int) Math.ceil(this.cyan * scale);
        this.magenta = (int) Math.ceil(this.magenta * scale);
        this.yellow = (int) Math.ceil(this.yellow * scale);
        this.alpha = (int) Math.ceil(this.alpha * scale);
    }
    
    @Override
    public int getMinimumCount(ColorIngredient other, int availableCount) {
        int count = -1;
        if (this.black > 0 && other.black > 0)
            count = Math.max(count, Mth.ceil(this.black / (double) other.black));
        if (this.cyan > 0 && other.cyan > 0)
            count = Math.max(count, Mth.ceil(this.cyan / (double) other.cyan));
        if (this.magenta > 0 && other.magenta > 0)
            count = Math.max(count, Mth.ceil(this.magenta / (double) other.magenta));
        if (this.yellow > 0 && other.yellow > 0)
            count = Math.max(count, Mth.ceil(this.yellow / (double) other.yellow));
        if (this.alpha > 0 && other.alpha > 0)
            count = Math.max(count, Mth.ceil(this.alpha / (double) other.alpha));
        return Math.min(availableCount, count);
    }
    
    public void scale(double scale) {
        this.black = (int) Math.ceil(this.black * scale);
        this.cyan = (int) Math.ceil(this.cyan * scale);
        this.magenta = (int) Math.ceil(this.magenta * scale);
        this.yellow = (int) Math.ceil(this.yellow * scale);
        this.alpha = (int) Math.ceil(this.alpha * scale);
    }
    
    public void scaleLoose(double scale) {
        this.black = (int) Math.floor(this.black * scale);
        this.cyan = (int) Math.floor(this.cyan * scale);
        this.magenta = (int) Math.floor(this.magenta * scale);
        this.yellow = (int) Math.floor(this.yellow * scale);
        this.alpha = (int) Math.floor(this.alpha * scale);
    }
    
    @Override
    public void print(TextBuilder text) {
        String message = "";
        if (black > 0)
            message += getBlackDescription();
        if (cyan > 0)
            message += (message.isEmpty() ? "" : " ") + getCyanDescription();
        if (magenta > 0)
            message += (message.isEmpty() ? "" : " ") + getMagentaDescription();
        if (yellow > 0)
            message += (message.isEmpty() ? "" : " ") + getYellowDescription();
        if (alpha > 0)
            message += (message.isEmpty() ? "" : " ") + getAlphaDescription();
        text.text(message);
    }
    
}
