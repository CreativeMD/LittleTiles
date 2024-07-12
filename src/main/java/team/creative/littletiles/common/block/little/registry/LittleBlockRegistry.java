package team.creative.littletiles.common.block.little.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.block.LittleBlock;
import team.creative.littletiles.common.convertion.OldLittleTilesDataParser;
import team.creative.littletiles.mixin.common.block.StateHolderAccessor;

public class LittleBlockRegistry {
    
    private static final HashMap<Block, LittleBlock> BLOCK_MAP = new HashMap<>();
    private static final HashMap<String, LittleBlock> NAME_MAP = new HashMap<>();
    private static final PairList<Filter<Block>, Function<Block, LittleBlock>> BLOCK_HANDLERS = new PairList<>();
    private static final Function<Block, LittleBlock> FALLBACK = x -> new LittleMCBlock(x);
    private static final List<Function<String, LittleBlock>> SPECIAL_HANDLERS = new ArrayList<>();
    private static final Object2BooleanMap<Block> HAS_HANDLER_CACHE = new Object2BooleanArrayMap<>();
    
    public static LittleBlock getMissing(String name) {
        LittleBlock little = NAME_MAP.get(name);
        if (little != null)
            return little;
        return create(name, null);
    }
    
    public static LittleBlock get(String name) {
        LittleBlock little = NAME_MAP.get(name);
        if (little != null)
            return little;
        return create(name, BuiltInRegistries.BLOCK.get(ResourceLocation.parse(name)));
    }
    
    public static LittleBlock get(Block block) {
        LittleBlock little = BLOCK_MAP.get(block);
        if (little != null)
            return little;
        return create(null, block);
    }
    
    public static LittleBlock get(BlockState block) {
        LittleBlock little = BLOCK_MAP.get(block.getBlock());
        if (little != null)
            return little;
        return create(null, block.getBlock());
    }
    
    public static String saveState(BlockState state) {
        if (state.getBlock() instanceof AirBlock)
            return null;
        
        StringBuilder name = new StringBuilder();
        name.append(state.getBlock().builtInRegistryHolder().key().location());
        if (!state.getValues().isEmpty())
            name.append('[').append(state.getValues().entrySet().stream().map(StateHolderAccessor.getPROPERTY_ENTRY_TO_STRING_FUNCTION()).collect(Collectors.joining(","))).append(
                ']');
        return name.toString();
    }
    
    public static BlockState loadState(String name) {
        return loadState(name, true);
    }
    
    public static BlockState loadState(String name, boolean checkOld) {
        String[] parts;
        if (name.contains("["))
            parts = name.split("\\[");
        else
            parts = new String[] { name };
        if (parts.length == 0)
            return LittleTilesRegistry.MISSING.value().defaultBlockState();
        ResourceLocation location;
        try {
            location = ResourceLocation.parse(parts[0]);
        } catch (ResourceLocationException e) {
            return LittleTilesRegistry.MISSING.value().defaultBlockState();
        }
        Block block = BuiltInRegistries.BLOCK.get(location);
        if (block == null || block instanceof AirBlock) {
            if (checkOld) {
                String converted = OldLittleTilesDataParser.BLOCK_MAP.get(name);
                if (converted != null)
                    return loadState(converted, false);
            }
            return LittleTilesRegistry.MISSING.value().defaultBlockState();
        }
        if (parts.length == 1)
            return block.defaultBlockState();
        if (parts.length > 2)
            throw new IllegalArgumentException(name);
        
        BlockState state = block.defaultBlockState();
        String[] properties = parts[1].substring(0, parts[1].length() - 1).split(",");
        for (int i = 0; i < properties.length; i++) {
            String[] data = properties[i].split("=");
            if (data.length != 2)
                throw new IllegalArgumentException(name);
            Property<? extends Comparable> property = block.getStateDefinition().getProperty(data[0]);
            Optional value = property.getValue(data[1]);
            if (value.isPresent())
                state = setValue(state, property, value.get());
            else
                throw new IllegalArgumentException("Invalid property " + properties[i] + " in " + name);
        }
        return state;
    }
    
    private static <T extends Comparable<T>, V extends T> BlockState setValue(BlockState state, Property prop, Object value) {
        return state.setValue((Property<T>) prop, (V) value);
    }
    
    public static boolean hasHandler(Block block) {
        synchronized (HAS_HANDLER_CACHE) {
            return HAS_HANDLER_CACHE.computeIfAbsent(block, x -> {
                if (block instanceof LittleBlock)
                    return true;
                for (Pair<Filter<Block>, Function<Block, LittleBlock>> pair : BLOCK_HANDLERS)
                    if (pair.key.is(block))
                        return true;
                return false;
            });
        }
    }
    
    private static LittleBlock create(String name, Block block) {
        if (block instanceof LittleBlock) {
            NAME_MAP.put(name, (LittleBlock) block);
            return (LittleBlock) block;
        }
        if (block == null) {
            for (Function<String, LittleBlock> special : SPECIAL_HANDLERS) {
                LittleBlock little = special.apply(name);
                if (little != null) {
                    NAME_MAP.put(name, little);
                    return little;
                }
            }
            LittleBlock little = new LittleMissingBlock(name);
            NAME_MAP.put(name, little);
            return little;
        }
        
        if (name == null)
            name = block.builtInRegistryHolder().key().location().toString();
        for (Pair<Filter<Block>, Function<Block, LittleBlock>> pair : BLOCK_HANDLERS)
            if (pair.key.is(block)) {
                LittleBlock little = pair.value.apply(block);
                BLOCK_MAP.put(block, little);
                NAME_MAP.put(name, little);
                return little;
            }
        LittleBlock little = FALLBACK.apply(block);
        NAME_MAP.put(name, little);
        BLOCK_MAP.put(block, little);
        return little;
    }
    
    public static void register(Filter<Block> filter, Function<Block, LittleBlock> function) {
        BLOCK_HANDLERS.add(filter, function);
    }
    
}
