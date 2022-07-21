package mcjty.theoneprobe.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Implement this interface if you want a custom display for your own entities instead of the standard
 * display.
 */
public interface IEntityDisplayOverride {

    /**
     * This function returns true if you handled the probe info yourselves and TOP doesn't have to add its
     * own info.
     */
    boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, Entity entity, IProbeHitEntityData data);

}
