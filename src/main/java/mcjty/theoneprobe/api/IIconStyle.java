package mcjty.theoneprobe.api;

/**
 * Style for the icon element.
 */
public interface IIconStyle {
    /**
     * Change the width of the icon. Default is 16
     */
    IIconStyle width(int w);

    /**
     * Change the height of the icon. Default is 16
     */
    IIconStyle height(int h);

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

    int getTextureWidth();

    int getTextureHeight();
}
