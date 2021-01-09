package mcp.mobius.waila.api;

/** Main interface used for Waila plugins. Provides a valid instance of {@link IWailaRegistrar}.
 * <p>
 * Annotate the implementing class with {@link WailaPlugin}. Implementing classes should have a default constructor. */
public interface IWailaPlugin {
    
    /** Called during {@link net.minecraftforge.fml.common.event.FMLLoadCompleteEvent}.
     *
     * @param registrar
     *            - An instance of IWailaRegistrar to register your providers with. */
    void register(IWailaRegistrar registrar);
}
