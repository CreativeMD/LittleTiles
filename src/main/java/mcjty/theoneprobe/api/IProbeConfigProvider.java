package mcjty.theoneprobe.api;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Implement this interface if you want to override the default
 * probe config for some of your blocks or entities.
 */
public interface IProbeConfigProvider {

    /**
     * Possibly override the config for this entity. You can make modifications to the given 'config' which starts
     * from default.
     */
    void getProbeConfig(IProbeConfig config, Player player, Level world, Entity entity, IProbeHitEntityData data);

    /**
     * Possibly override the config for this block. You can make modifications to the given 'config' which starts
     * from default.
     */
    void getProbeConfig(IProbeConfig config, Player player, Level world, BlockState blockState, IProbeHitData data);

}
