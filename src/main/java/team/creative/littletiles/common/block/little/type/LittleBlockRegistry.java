package team.creative.littletiles.common.block.little.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.creativecore.common.util.type.PairList;
import team.creative.littletiles.common.api.block.LittleBlock;

public class LittleBlockRegistry {
    
    private static final HashMap<Block, LittleBlock> blockMap = new HashMap<>();
    private static final HashMap<String, LittleBlock> nameMap = new HashMap<>();
    private static final PairList<Filter<Block>, Function<Block, LittleBlock>> blockHandlers = new PairList<>();
    private static final Function<Block, LittleBlock> fallBack = x -> new LittleMCBlock(x);
    private static final List<Function<String, LittleBlock>> specialHandlers = new ArrayList<>();
    
    public static LittleBlock get(String name) {
        LittleBlock little = nameMap.get(name);
        if (little != null)
            return little;
        return create(name, ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name)));
    }
    
    public static LittleBlock get(Block block) {
        LittleBlock little = blockMap.get(block);
        if (little != null)
            return little;
        return create(null, block);
    }
    
    private static LittleBlock create(String name, Block block) {
        if (block instanceof LittleBlock) {
            nameMap.put(name, (LittleBlock) block);
            return (LittleBlock) block;
        }
        if (block == null) {
            for (Function<String, LittleBlock> special : specialHandlers) {
                LittleBlock little = special.apply(name);
                if (little != null) {
                    nameMap.put(name, little);
                    return little;
                }
            }
            LittleBlock little = new LittleMissingBlock(name);
            nameMap.put(name, little);
            return little;
        }
        
        for (Pair<Filter<Block>, Function<Block, LittleBlock>> pair : blockHandlers)
            if (pair.key.is(block)) {
                LittleBlock little = pair.value.apply(block);
                blockMap.put(block, little);
                return little;
            }
        LittleBlock little = fallBack.apply(block);
        blockMap.put(block, little);
        return little;
    }
    
    public static void register(Filter<Block> filter, Function<Block, LittleBlock> function) {
        blockHandlers.add(filter, function);
    }
    
}
