package mcjty.theoneprobe.api;

/**
 * Style for the entity element.
 * Do not create custom implementations of this interface. This interface is implemented by TOP
 * and you can get instances from either IProbeInfo or else the IStyleManager
 */
public interface IEntityStyle {

	IEntityStyle copy();
	
    /**
     * Change the width of the element. Default is 25
     */
    IEntityStyle width(int w);

    /**
     * Change the height of the element. Default is 25
     */
    IEntityStyle height(int h);
    default IEntityStyle bounds(int width, int height) { return width(width).height(height); }

    /**
     * Change the scale of the entity inside the element. Default is 1.0 which
     * tries to fit as good as possible.
     */
    IEntityStyle scale(float scale);

    int getWidth();

    int getHeight();

    float getScale();
}
