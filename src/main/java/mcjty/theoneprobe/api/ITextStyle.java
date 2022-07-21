package mcjty.theoneprobe.api;

/**
 * Style for the text element.
 * Do not create custom implementations of this interface. This interface is implemented by TOP
 * and you can get instances from either IProbeInfo or else the IStyleManager
 */
public interface ITextStyle {
	
	/// Allows copying the state for easier template creation
	ITextStyle copy();
	
	default ITextStyle padding(int padding) { return topPadding(padding).bottomPadding(padding).leftPadding(padding).rightPadding(padding); }
	default ITextStyle vPadding(int padding) { return topPadding(padding).bottomPadding(padding); }
	default ITextStyle hPadding(int padding) { return leftPadding(padding).rightPadding(padding); }

	ITextStyle topPadding(int padding);
	ITextStyle bottomPadding(int padding);
	ITextStyle leftPadding(int padding);
	ITextStyle rightPadding(int padding);
	default ITextStyle bounds(Integer width, Integer height) { return width(width).height(height); }
	ITextStyle width(Integer width);
	ITextStyle height(Integer height);
	ITextStyle alignment(ElementAlignment align);
	
	int getLeftPadding();
	int getRightPadding();
	int getTopPadding();
	int getBottomPadding();
	Integer getWidth();
	Integer getHeight();
	ElementAlignment getAlignment();
}
