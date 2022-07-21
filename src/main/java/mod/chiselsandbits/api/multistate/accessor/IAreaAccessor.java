package mod.chiselsandbits.api.multistate.accessor;

import java.util.stream.Stream;

/** Parts of this file have been removed/ modified to not cause compiling errors **/

/**
 * Gives access to all states in a given area.
 * Might be larger then a single block.
 */
public interface IAreaAccessor
{

    /**
     * Gives access to a stream with the entry state info inside the accessors range.
     *
     * @return The stream with the inner states.
     */
    Stream<IStateEntryInfo> stream();


}
