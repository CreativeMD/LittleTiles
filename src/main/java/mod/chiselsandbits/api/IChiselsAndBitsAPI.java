package mod.chiselsandbits.api;

import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;

/** Parts of this file have been removed/ modified to not cause compiling errors **/

/**
 * Do not implement, is passed to your {@link IChiselsAndBitsAddon},
 * and can be accessed via its {@link #getInstance()}-method.
 */
public interface IChiselsAndBitsAPI
{
    /**
     * Gives access to the api instance.
     *
     * @return The api.
     */
    static IChiselsAndBitsAPI getInstance()
    {
        return null;
    }


    /**
     * Manager which deals with converting eligible blocks, blockstates and IItemProviders into their chiseled
     * variants.
     *
     * @return The conversion manager.
     */
    IConversionManager getConversionManager();

   
}
