package mcjty.theoneprobe.api;

/**
 * Use this interface (which you can get from ITheOneProbe) to create styles that
 * you can use in your probe providers
 */
public interface IStyleManager {

    IEntityStyle entityStyleDefault();
    IEntityStyle entityStyleBounded(int width, int height);
    IEntityStyle entityStyleScaled(float scale);

    IIconStyle iconStyleDefault();
    IIconStyle iconStyleColored(Color color);
    IIconStyle iconStyleColored(int color);
    IIconStyle iconStyleBounded(int width, int height);
    IIconStyle iconStyleTextureBounded(int texWidth, int texHeight);

    IItemStyle itemStyleDefault();
    IItemStyle itemStyleBounded(int width, int height);

    ILayoutStyle layoutStyleDefault();
    ILayoutStyle layoutStyleBordered(Color color);
    ILayoutStyle layoutStyleBordered(Integer color);
    ILayoutStyle layoutStyleSpaced(int spacing);
    ILayoutStyle layoutStyleAligned(ElementAlignment align);
    ILayoutStyle layoutStylePadded(int padding);
    ILayoutStyle layoutStylePadded(int xPadding, int yPadding);
    ILayoutStyle layoutStylePadded(int top, int bottom, int left, int right);

    IProgressStyle progressStyleDefault();
    IProgressStyle progressStyleArmor();
    IProgressStyle progressStyleLife();
    IProgressStyle progressStyleAligned(ElementAlignment align);
    IProgressStyle progressStyleBounded(int width, int height);
    IProgressStyle progressStyleTextOnly(String prefix);
    IProgressStyle progressStyleText(String prefix, String suffix);

    ITextStyle textStyleDefault();
    ITextStyle textStyleAligned(ElementAlignment align);
    ITextStyle textStyleWidth(Integer width);
    ITextStyle textStyleHeight(Integer height);
    ITextStyle textStyleBounded(Integer width, Integer height);
    ITextStyle textStylePadded(int padding);
    ITextStyle textStylePadded(int xPadding, int yPadding);
    ITextStyle textStylePadded(int top, int bottom, int left, int right);

}
