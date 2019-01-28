package mcjty.theoneprobe.api;

/**
 * The style for the overlay.
 */
public interface IOverlayStyle {

    /**
     * The offset of the border around the box. Use 0 to disable.
     */
    IOverlayStyle borderOffset(int offset);

    int getBorderOffset();

    /**
     * The thickness of the border around the overlay. Use 0 to disable.
     */
    IOverlayStyle borderThickness(int thick);

    int getBorderThickness();

    /**
     * The color of the border (if used). This is a 32-bit color value.
     * You can use alpha if you want transparency. For example
     * 0xFFFFFFFF is pure white, 0x22FF0000 is a very faint transparent red.
     */
    IOverlayStyle borderColor(int color);

    int getBorderColor();

    /**
     * The color of the box.
     */
    IOverlayStyle boxColor(int color);

    int getBoxColor();

    /**
     * The location of the overlay box.
     * You can use -1 to indicate some location is not to be used. If
     * the two locations on the same axis are -1 then the box will be centered
     * on that axis (i.e. if leftX and rightX are both -1 then the box will be
     * centered horizontally).
     * If rightX is -1 but leftX is not then leftX will be the X coordinate
     * of the left side of the overlay box. If rightX is specified then it will
     * be the X coordinate of the right side of the overlay box.
     */
    IOverlayStyle location(int leftX, int rightX, int topY, int bottomY);

    int getLeftX();

    int getRightX();

    int getTopY();

    int getBottomY();

}
