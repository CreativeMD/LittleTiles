package mcjty.theoneprobe.api;

/**
 * Style for the progress bar.
 */
public interface IProgressStyle {
    /// The color that is used for the border of the progress bar
    IProgressStyle borderColor(int c);

    /// The color that is used for the background of the progress bar
    IProgressStyle backgroundColor(int c);

    /// The color that is used for the filled part of the progress bar
    IProgressStyle filledColor(int c);

    /// If this is different from the filledColor then the fill color will alternate
    IProgressStyle alternateFilledColor(int c);

    /// If true then text is shown inside the progress bar
    IProgressStyle showText(boolean b);

    /// The number format to use for the text inside the progress bar
    IProgressStyle numberFormat(NumberFormat f);

    IProgressStyle prefix(String prefix);

    IProgressStyle suffix(String suffix);

    /// If the progressbar is a lifebar then this is the maximum width
    IProgressStyle width(int w);

    IProgressStyle height(int h);

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

    int getWidth();

    int getHeight();

    boolean isLifeBar();

    boolean isArmorBar();
}
