package mcjty.theoneprobe.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Implement this interface if you want a custom display for your own blocks instead of the standard
 * display. This can be useful if you (for example) have a multiblock and want to show a picture of the
 * entire multiblock instead of the itemstack that getPickBlock() would return.
 */
public interface IBlockDisplayOverride {

    /**
     * This function returns true if you handled the probe info yourselves and TOP doesn't have to add its
     * own info.
     */
    boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data);

}
