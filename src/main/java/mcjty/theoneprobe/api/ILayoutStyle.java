package mcjty.theoneprobe.api;

/**
 * Style for a horizonatl or vertical layout.
 */
public interface ILayoutStyle {
    /// The color that is used for the border of the progress bar
    ILayoutStyle borderColor(Integer c);

    /**
     * The spacing to use between elements in this panel. -1 means to use default depending
     * on vertical vs horizontal.
     */
    ILayoutStyle spacing(int f);

    /**
     * Set the alignment of the elements inside this element. Default is ALIGN_TOPLEFT
     */
    ILayoutStyle alignment(ElementAlignment alignment);

    Integer getBorderColor();

    int getSpacing();

    ElementAlignment getAlignment();
}
