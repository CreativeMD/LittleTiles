package mcjty.theoneprobe.api;

/**
 * Style for the item element.
 * Do not create custom implementations of this interface. This interface is implemented by TOP
 * and you can get instances from either IProbeInfo or else the IStyleManager
 */
public interface IItemStyle {

	IItemStyle copy();
	
	IItemStyle width(int w);
    IItemStyle height(int h);
    default IItemStyle bounds(int width, int height) { return width(width).height(height); }
    
    int getWidth();
    int getHeight();
}
