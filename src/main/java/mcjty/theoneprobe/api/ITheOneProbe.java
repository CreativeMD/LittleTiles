package mcjty.theoneprobe.api;

/**
 * Main interface for this mod. Use this to build probe information
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "<whatever>.YourClass$GetTheOneProbe");
 */
public interface ITheOneProbe {

    /**
     * Optionally register a provider for your probe information. You don't have to do this. You
     * can also implement IProbeInfoAccessor in your block instead. If you register a provider
     * with the same string ID as one that already exists it will replace that provider. This
     * is one way to replace the standard providers. TheOneProbe has the following standard
     * providers (registered in the given order):
     *
     *   - "theoneprobe:default": this is the default provider that takes care of showing
     *     the block icon, name and modid as well as the harvest level of the block and other
     *     basic information
     *   - "theoneprobe:debug": this provider shows debug information if the creative probe
     *     is being used.
     *   - "theoneprobe:block": this provider will check if the block has an IProbeInfoAccessor
     *     and then use that. i.e. this is the provider that takes care of making sure
     *     that IProbeInfoAccessor works.
     *
     * @param provider
     */
    void registerProvider(IProbeInfoProvider provider);

    /**
     * Optionally register a provider for your probe information. You don't have to do this. You
     * can also implement IProbeInfoAccessor in your block instead. If you register a provider
     * with the same string ID as one that already exists it will replace that provider. This
     * is one way to replace the standard providers. TheOneProbe has the following standard
     * providers (registered in the given order):
     *
     *   - "theoneprobe:entity.default": this is the default provider that takes care of showing
     *     the entity name and modid as well as the current health of the entity.
     *   - "theoneprobe:entity.debug": this provider shows debug information if the creative probe
     *     is being used.
     *   - "theoneprobe:entity.entity": this provider will check if the entity has an IProbeInfoEntityAccessor
     *     and then use that. i.e. this is the provider that takes care of making sure
     *     that IProbeInfoEntityAccessor works.
     *
     * @param provider
     */
    void registerEntityProvider(IProbeInfoEntityProvider provider);

    /**
     * Register an element factory.
     * @return an id to use when defining elements using this factory
     */
    int registerElementFactory(IElementFactory factory);

    /**
     * Get the element factory for a given ID.
     */
    IElementFactory getElementFactory(int id);

    /**
     * Get the overlay renderer. You can use this to do your own custom overlay rendering
     * (besides what The One Probe already does).
     */
    IOverlayRenderer getOverlayRenderer();

    /**
     * Register a probe config provider so that you can override the default
     * config for your own blocks or entities. Note that you have to register
     * this server side.
     */
    void registerProbeConfigProvider(IProbeConfigProvider provider);

    /**
     * Register a block display override implementation so that you can change how the standard
     * block information is shown for your blocks.
     */
    void registerBlockDisplayOverride(IBlockDisplayOverride override);

    /**
     * Register an entity display override implementation so that you can change how the standard
     * entity information is shown for your entities.
     */
    void registerEntityDisplayOverride(IEntityDisplayOverride override);
}
