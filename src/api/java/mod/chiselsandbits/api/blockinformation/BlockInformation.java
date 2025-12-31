package mod.chiselsandbits.api.blockinformation;

import java.util.Optional;

import mod.chiselsandbits.api.variant.state.IStateVariant;
import net.minecraft.world.level.block.state.BlockState;

public record BlockInformation(BlockState blockState, Optional<IStateVariant> variant) {}