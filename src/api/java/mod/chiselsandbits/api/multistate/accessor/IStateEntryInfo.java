package mod.chiselsandbits.api.multistate.accessor;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Parts of this file have been removed/ modified to not cause compiling errors **/

/**
 * Represents a single entry inside an area which can have multiple states.
 *
 * @see IAreaAccessor
 * @see mod.chiselsandbits.api.multistate.accessor.world.IWorldAreaAccessor
 * @see mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo
 */
public interface IStateEntryInfo
{
    /**
     * The state that this entry represents.
     *
     * @return The state.
     */
    BlockState getState();

    /**
     * The start (lowest on all three axi) position of the state that this entry occupies.
     *
     * @return The start position of this entry in the given block.
     */
    Vec3 getStartPoint();

    /**
     * The end (highest on all three axi) position of the state that this entry occupies.
     *
     * @return The start position of this entry in the given block.
     */
    Vec3 getEndPoint();

    /**
     * The center point of the entry in the current block.
     *
     * @return The center position of this entry in the given block.
     */
    default Vec3 getCenterPoint() {
        return getStartPoint().add(getEndPoint()).multiply(0.5,0.5,0.5);
    }

}
