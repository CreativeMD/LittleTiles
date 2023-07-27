package mcp.mobius.waila.api;

import javax.annotation.ParametersAreNonnullByDefault;

/** Main registration interface for Waila plugins. To register your plugin, annotate your main plugin class with
 * {@link WailaPlugin} and implement {@link IWailaPlugin}.
 * <p>
 * <b>Note: Deprecated information below.</b> It will still work, but the supported method will now be the annotation.
 * <p>
 * Main registration interface. An instance will be provided to a method specified in an IMC msg formatted as follow<br>
 * FMLInterModComms.sendMessage("Waila", "register", "fully.qualified.path.to.registration.method");<br>
 * The registration method need to follow this signature<br>
 * public static void callbackRegister({@link IWailaRegistrar} registrar)
 * <p>
 * If not specified otherwise, all the registration methods taking a class can take classes as well as interfaces.
 * Waila will do a lookup using instanceof on the registered classes, meaning that if all your targets inherit one interface, you only need
 * to specify it to cover the whole hierarchy.<br>
 * For the registration of blocks, both Blocks and TileEntities are accepted.
 * <p>
 * For the configuration keys :<br>
 * modname refers to a String used for display in Waila's config panel.<br>
 * keyname refers to an unique key used internally for config query (cf {@link IWailaConfigHandler}). Those keys are shared across Waila, keep them unique !<br>
 *
 * @author ProfMobius */
@ParametersAreNonnullByDefault
public interface IWailaRegistrar {
    /* Add a config option in the section modname with displayed text configtext and access key keyname */
    void addConfig(String modname, String keyname, String configtext);
    
    void addConfig(String modname, String keyname, String configtext, boolean defvalue);
    
    void addConfigRemote(String modname, String keyname, String configtext);
    
    void addConfigRemote(String modname, String keyname, String configtext, boolean defvalue);
    
    void addConfig(String modname, String keyname);
    
    void addConfig(String modname, String keyname, boolean defvalue);
    
    void addConfigRemote(String modname, String keyname);
    
    void addConfigRemote(String modname, String keyname, boolean defvalue);
    
    /* Register a stack overrider for the given blockID */
    void registerStackProvider(IWailaDataProvider dataProvider, Class block);
    
    /* Same thing, but works on a class hierarchy instead */
    void registerHeadProvider(IWailaDataProvider dataProvider, Class block);
    
    void registerBodyProvider(IWailaDataProvider dataProvider, Class block);
    
    void registerTailProvider(IWailaDataProvider dataProvider, Class block);
    
    /* Registering an NBT Provider provides a way to override the default "writeToNBT" way of doing things. */
    void registerNBTProvider(IWailaDataProvider dataProvider, Class block);
    
    /* Entity text registration methods */
    void registerHeadProvider(IWailaEntityProvider dataProvider, Class entity);
    
    void registerBodyProvider(IWailaEntityProvider dataProvider, Class entity);
    
    void registerTailProvider(IWailaEntityProvider dataProvider, Class entity);
    
    void registerOverrideEntityProvider(IWailaEntityProvider dataProvider, Class entity);
    
    /* Registering an NBT Provider provides a way to override the default "writeToNBT" way of doing things. */
    void registerNBTProvider(IWailaEntityProvider dataProvider, Class entity);
    
    /* FMP Providers */
    @Deprecated
    void registerHeadProvider(IWailaFMPProvider dataProvider, String name);
    
    @Deprecated
    void registerBodyProvider(IWailaFMPProvider dataProvider, String name);
    
    @Deprecated
    void registerTailProvider(IWailaFMPProvider dataProvider, String name);
    
    /* The block decorators */
    void registerDecorator(IWailaBlockDecorator decorator, Class block);
    
    @Deprecated
    void registerDecorator(IWailaFMPDecorator decorator, String name);
    
    void registerTooltipRenderer(String name, IWailaTooltipRenderer renderer);
    
    /* UNUSED FOR NOW (Will be used for the ingame wiki */
    //public void registerDocTextFile  (String filename);
    //public void registerShortDataProvider (IWailaSummaryProvider dataProvider, Class item);
}
