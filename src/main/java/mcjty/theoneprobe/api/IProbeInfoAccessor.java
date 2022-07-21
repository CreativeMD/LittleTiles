package mcjty.theoneprobe.api;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;

/**
 * You can implement this in your block implementation if you want to support
 * the probe. An alternative to this is to make an IProveInfoProvider.
 * Note that if you implement this then it will be called last (after all the providers)
 */
public interface IProbeInfoAccessor {

    /**
     * Add information for the probe info for the given block. This is always
     * called server side.
     * The given probeInfo object represents a vertical layout. So adding elements to that
     * will cause them to be grouped vertically.
     */
    void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data);
}
