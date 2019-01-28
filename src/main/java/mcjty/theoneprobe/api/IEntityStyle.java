package mcjty.theoneprobe.api;

/**
 * Style for the entity element.
 */
public interface IEntityStyle {
    /**
     * Change the width of the element. Default is 25
     */
    IEntityStyle width(int w);

    /**
     * Change the height of the element. Default is 25
     */
    IEntityStyle height(int h);

    /**
     * Change the scale of the entity inside the element. Default is 1.0 which
     * tries to fit as good as possible.
     */
    IEntityStyle scale(float scale);

    int getWidth();

    int getHeight();

    float getScale();
}
