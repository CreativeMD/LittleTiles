package team.creative.littletiles.client.render.material;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

/** Deliberately limited to the verified Yuushya cube-column and concrete beam resources. */
public final class ConnectedMaterialState {
    private static final Map<Block, Boolean> SUPPORTED = new ConcurrentHashMap<>();
    private ConnectedMaterialState() {}

    public static boolean supports(BlockState state) {
        return SUPPORTED.computeIfAbsent(state.getBlock(), block -> supportsBlock(block.defaultBlockState()));
    }

    private static boolean supportsBlock(BlockState state) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!id.getNamespace().equals("yuushya")) return false;
        String name = id.getPath();
        boolean column = name.equals("chiseled_vertical_marble") || name.matches("[ab]_[a-z_]+_vertical_compact");
        boolean beam = name.matches("[ab]_[a-z_]+_horizontal_compact");
        Property<?> pos = state.getBlock().getStateDefinition().getProperty("pos");
        if (pos == null || pos.getValue("none").isEmpty() || pos.getValue("middle").isEmpty()) return false;
        return column && pos.getValue("top").isPresent() && pos.getValue("bottom").isPresent()
            || beam && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && pos.getValue("left").isPresent() && pos.getValue("right").isPresent();
    }

    public static Direction positiveDirection(BlockState state) {
        Property<?> pos = state.getBlock().getStateDefinition().getProperty("pos");
        if (pos.getValue("top").isPresent()) return Direction.UP;
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
    }

    public static String position(Direction facing, boolean negative, boolean positive) {
        if (negative && positive) return "middle";
        if (!negative && !positive) return "none";
        if (facing == null) return positive ? "bottom" : "top";
        boolean negativeIsLeft = facing == Direction.WEST || facing == Direction.SOUTH;
        return negative == negativeIsLeft ? "left" : "right";
    }

    public static BlockState resolve(BlockState state, boolean negative, boolean positive) {
        Direction facing = positiveDirection(state) == Direction.UP ? null : state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return withPosition(state, position(facing, negative, positive));
    }

    public static BlockState key(BlockState state) { return withPosition(state, "none"); }

    private static <T extends Comparable<T>> BlockState set(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }

    private static BlockState withPosition(BlockState state, String value) {
        return set(state, state.getBlock().getStateDefinition().getProperty("pos"), value);
    }
}
