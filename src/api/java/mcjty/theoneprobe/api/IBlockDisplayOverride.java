package mcjty.theoneprobe.api;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;

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
    boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data);

}
