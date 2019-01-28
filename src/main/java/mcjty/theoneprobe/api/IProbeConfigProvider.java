package mcjty.theoneprobe.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Implement this interface if you want to override the default
 * probe config for some of your blocks or entities.
 */
public interface IProbeConfigProvider {

    /**
     * Possibly override the config for this entity. You can make modifications to the given 'config' which starts
     * from default.
     */
    void getProbeConfig(IProbeConfig config, EntityPlayer player, World world, Entity entity, IProbeHitEntityData data);

    /**
     * Possibly override the config for this block. You can make modifications to the given 'config' which starts
     * from default.
     */
    void getProbeConfig(IProbeConfig config, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data);

}
