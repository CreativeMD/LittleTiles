package mcjty.theoneprobe.api;

import net.minecraft.network.chat.Component;

/**
 * Style for the progress bar.
 * Do not create custom implementations of this interface. This interface is implemented by TOP
 * and you can get instances from either IProbeInfo or else the IStyleManager
 */
public interface IProgressStyle {
	
	/// Allows copying the state for easier template creation
	IProgressStyle copy();
    /// The color that is used for the border of the progress bar
    IProgressStyle borderColor(int c);
    default IProgressStyle borderColor(Color c) { return borderColor(c.getRGB()); }

    /// The color that is used for the background of the progress bar
    IProgressStyle backgroundColor(int c);
    default IProgressStyle backgroundColor(Color c) { return backgroundColor(c.getRGB()); }

    /// The color that is used for the filled part of the progress bar
    IProgressStyle filledColor(int c);
    default IProgressStyle filledColor(Color c) { return filledColor(c.getRGB()); }

    /// If this is different from the filledColor then the fill color will alternate
    IProgressStyle alternateFilledColor(int c);
    default IProgressStyle alternateFilledColor(Color c) { return alternateFilledColor(c.getRGB()); }
    
    /// Helper functions to compress code
    default IProgressStyle borderlessColor(int filled, int background) { return filledColor(filled).backgroundColor(background); }
    default IProgressStyle borderlessColor(int filled, int alternate, int background) { return filledColor(filled).alternateFilledColor(alternate).backgroundColor(background); }
    default IProgressStyle borderlessColor(Color filled, Color background) { return filledColor(filled).backgroundColor(background); }
    default IProgressStyle borderlessColor(Color filled, Color alternate, Color background) { return filledColor(filled).alternateFilledColor(alternate).backgroundColor(background); }
    default IProgressStyle color(int border, int filled, int background) { return borderColor(border).filledColor(filled).backgroundColor(background); }
    default IProgressStyle color(int border, int filled, int alternate, int background) { return borderColor(border).filledColor(filled).alternateFilledColor(alternate).backgroundColor(background); }
    default IProgressStyle color(Color border, Color filled, Color background) { return borderColor(border).filledColor(filled).backgroundColor(background); }
    default IProgressStyle color(Color border, Color filled, Color alternate, Color background) { return borderColor(border).filledColor(filled).alternateFilledColor(alternate).backgroundColor(background); }
    
    /// If true then text is shown inside the progress bar
    IProgressStyle showText(boolean b);

    /// The number format to use for the text inside the progress bar
    IProgressStyle numberFormat(NumberFormat f);
    
    IProgressStyle prefix(Component prefix);
    IProgressStyle suffix(Component suffix);
    
    default IProgressStyle prefix(String prefix, Object...args) { return prefix(Component.translatable(prefix, args)); }
    default IProgressStyle suffix(String suffix, Object...args){ return suffix(Component.translatable(suffix, args)); }
    default IProgressStyle prefix(String prefix) { return prefix(Component.translatable(prefix)); }
    default IProgressStyle suffix(String suffix){ return suffix(Component.translatable(suffix)); }
    
    /// If the progressbar is a lifebar then this is the maximum width
    default IProgressStyle bounds(int width, int height) { return width(width).height(height); }
    IProgressStyle width(int w);
    IProgressStyle height(int h);
    IProgressStyle alignment(ElementAlignment align);
    
    IProgressStyle lifeBar(boolean b);
    IProgressStyle armorBar(boolean b);

    int getBorderColor();
    int getBackgroundColor();
    int getFilledColor();
    int getAlternatefilledColor();
    
    boolean isShowText();
    NumberFormat getNumberFormat();
    
    String getPrefix();
    String getSuffix();
    Component getPrefixComp();
    Component getSuffixComp();
    
    int getWidth();
    int getHeight();
    ElementAlignment getAlignment();
    
    boolean isLifeBar();
    boolean isArmorBar();
}
