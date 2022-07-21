package mcjty.theoneprobe.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * You can implement IProbeInfoAccessor in your blocks or else you can use
 * this and register that to the ITheOneProbe. Note that TheOneProbe already
 * adds default providers which gives basic block information.
 */
public interface IProbeInfoProvider {

    /**
     * Return a unique ID
     */
    ResourceLocation getID();

    /**
     * Add information for the probe info for the given block. This is always called
     * server side.
     * The given probeInfo object represents a vertical layout. So adding elements to that
     * will cause them to be grouped vertically.
     */
    void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data);
}
