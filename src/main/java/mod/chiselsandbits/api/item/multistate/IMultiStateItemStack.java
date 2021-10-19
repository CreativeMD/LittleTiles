package mod.chiselsandbits.api.item.multistate;

import net.minecraft.world.item.ItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;

/** Parts of this file have been removed/ modified to not cause compiling errors **/

/**
 * The itemstack sensitive version of the multistate item.
 */
public interface IMultiStateItemStack extends IAreaAccessor
{

    /**
     * Converts this multistate itemstack data to an actual use able itemstack.
     *
     * @return The itemstack with the data of this multistate itemstack.
     */
    ItemStack toBlockStack();

    /**
     * Converts this multistate itemstack data into a pattern that can be reused.
     * By default converts this into a single use pattern.
     *
     * @return The single use patter from this multi state itemstack.
     */
    ItemStack toPatternStack();
}
