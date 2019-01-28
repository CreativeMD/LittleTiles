package mcjty.theoneprobe.api;

/**
 * Using this interface (that you can get from ITheOneProbe) you can
 * do custom overlay rendering from within your mod.
 */
public interface IOverlayRenderer {

    /**
     * Return the default overlay style as configured in the The One Probe config.
     * You can make modifications to this and use it for your own overlays. The
     * default style will not be modified. Note that if you call this client side
     * then this will also contain the settings as the player modified it locally.
     * If you call this server side then you will get the default settings from the
     * global config.
     */
    IOverlayStyle createDefaultStyle();

    /**
     * Create an empty default IProbeInfo (which actually represents the
     * default vertical element that is always used to start with). You can then
     * modify this as you wish and give it to render().
     */
    IProbeInfo createProbeInfo();

    /**
     * Render an overlay with the given style and probe info.
     * This has to be called client side and you have to call it every
     * frame for as long as you want this overlay to be visible.
     *
     * Typically you might want to call this in a RenderGameOverlayEvent.
     *
     * Note that calling this does not prevent the normal overlay from
     * rendering.
     */
    void render(IOverlayStyle style, IProbeInfo probeInfo);
}
