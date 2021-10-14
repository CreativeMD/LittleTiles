package mcjty.theoneprobe.api;

/**
 * Style for the icon element.
 * Do not create custom implementations of this interface. This interface is implemented by TOP
 * and you can get instances from either IProbeInfo or else the IStyleManager
 */
public interface IIconStyle {

	IIconStyle copy();

    /**
     * Change the width of the icon. Default is 16
     */
    IIconStyle width(int w);

    /**
     * Change the height of the icon. Default is 16
     */
    IIconStyle height(int h);
    default IIconStyle bounds(int w, int h) { return width(w).height(h); }

    int getWidth();
    int getHeight();

    /**
     * Change the total width of the texture on which the icon sits. Default is 256
     */
    IIconStyle textureWidth(int w);

    /**
     * Change the total height of the texture on which the icon sits. Default is 256
     */
    IIconStyle textureHeight(int h);
    default IIconStyle textureBounds(int w, int h) { return textureWidth(w).textureHeight(h); }

    int getTextureWidth();
    int getTextureHeight();
    
    IIconStyle color(int color);
    default IIconStyle color(Color color) { return color(color.getRGB());}
    
    int getColor();
}
