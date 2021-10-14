package mcjty.theoneprobe.api;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

/**
 * Access information about where the probe hit the block
 */
public interface IProbeHitData {

    BlockPos getPos();

    Vec3 getHitVec();

    Direction getSideHit();

    /**
     * Access the client-side result of getPickBlock() for the given block. That way
     * you don't have to call this server side because that can sometimes be
     * problematic
     * @return the picked block or ItemStack.EMPTY
     */
    @Nonnull
    ItemStack getPickBlock();
}
