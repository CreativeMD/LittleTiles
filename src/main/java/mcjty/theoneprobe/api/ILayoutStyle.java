package mcjty.theoneprobe.api;

/**
 * Style for a horizontal or vertical layout.
 * Do not create custom implementations of this interface. This interface is implemented by TOP
 * and you can get instances from either IProbeInfo or else the IStyleManager
 */
public interface ILayoutStyle {
	
	ILayoutStyle copy();
	
	default ILayoutStyle padding(int padding) { return topPadding(padding).bottomPadding(padding).leftPadding(padding).rightPadding(padding); }
	default ILayoutStyle vPadding(int padding) { return topPadding(padding).bottomPadding(padding); }
	default ILayoutStyle hPadding(int padding) { return leftPadding(padding).rightPadding(padding); }
	
	/// Padding Methods that allow you to add padding within the border color
	ILayoutStyle topPadding(int padding);
	ILayoutStyle bottomPadding(int padding);
	ILayoutStyle leftPadding(int padding);
	ILayoutStyle rightPadding(int padding);
	
    /// The color that is used for the border of the progress bar
    ILayoutStyle borderColor(Integer c);
    default ILayoutStyle borderColor(Color c) { return borderColor(c == null ? null : Integer.valueOf(c.getRGB())); }
    
    /**
     * The spacing to use between elements in this panel. -1 means to use default depending
     * on vertical vs horizontal.
     */
    ILayoutStyle spacing(int f);

    /**
     * Set the alignment of the elements inside this element. Default is ALIGN_TOPLEFT
     */
    ILayoutStyle alignment(ElementAlignment alignment);
    
	int getLeftPadding();
	int getRightPadding();
	int getTopPadding();
	int getBottomPadding();
    
    Integer getBorderColor();

    int getSpacing();

    ElementAlignment getAlignment();
}