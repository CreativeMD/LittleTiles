package team.creative.littletiles.common.config;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;

public class LittleBuildingConfig {
    
    static {
        ConfigTypeConveration.registerTypeCreator(LittleBuildingConfig.class, () -> new LittleBuildingConfig());
    }
    
    public LittleBuildingConfig() {
        limitAffectedBlocks = true;
        editUnbreakable = false;
        minimumTransparency = 255;
        limitEditBlocks = true;
        limitPlaceBlocks = true;
        harvestLevelBlock = HarvestLevel.WOOD;
        maxAffectedBlocks = 0;
        maxEditBlocks = 0;
        maxPlaceBlocks = 0;
    }
    
    public LittleBuildingConfig(boolean survival) {
        limitAffectedBlocks = survival;
        editUnbreakable = !survival;
        minimumTransparency = survival ? 255 : 0;
        limitEditBlocks = survival;
        limitPlaceBlocks = survival;
        harvestLevelBlock = survival ? HarvestLevel.STONE : HarvestLevel.DIAMOND;
    }
    
    @CreativeConfig
    public boolean limitAffectedBlocks;
    
    @CreativeConfig
    public int maxAffectedBlocks = 2;
    
    @CreativeConfig
    public HarvestLevel harvestLevelBlock;
    
    @CreativeConfig
    public boolean editUnbreakable;
    
    @CreativeConfig
    @CreativeConfig.IntRange(min = 0, max = 255)
    public int minimumTransparency;
    
    @CreativeConfig
    public boolean limitEditBlocks;
    @CreativeConfig
    public int maxEditBlocks = 10;
    
    @CreativeConfig
    public boolean limitPlaceBlocks;
    @CreativeConfig
    public int maxPlaceBlocks = 10;
    
    @CreativeConfig
    public boolean limitRecipeSize = false;
    
    @CreativeConfig
    public int recipeBlocksLimit = 16;
    
    public static enum HarvestLevel {
        
        DIAMOND {
            @Override
            public boolean is(Block block) {
                return true;
            }
        },
        IRON {
            @Override
            public boolean is(Block block) {
                return block.builtInRegistryHolder().is(BlockTags.NEEDS_IRON_TOOL) || !block.builtInRegistryHolder().is(BlockTags.NEEDS_DIAMOND_TOOL);
            }
        },
        STONE {
            @Override
            public boolean is(Block block) {
                return block.builtInRegistryHolder().is(BlockTags.NEEDS_STONE_TOOL) || (!block.builtInRegistryHolder().is(BlockTags.NEEDS_IRON_TOOL) && !block
                        .builtInRegistryHolder().is(BlockTags.NEEDS_DIAMOND_TOOL));
            }
        },
        WOOD {
            @Override
            public boolean is(Block block) {
                return !block.builtInRegistryHolder().is(BlockTags.NEEDS_STONE_TOOL) && !block.builtInRegistryHolder().is(BlockTags.NEEDS_IRON_TOOL) && !block
                        .builtInRegistryHolder().is(BlockTags.NEEDS_DIAMOND_TOOL);
            }
        };
        
        public abstract boolean is(Block block);
    }
    
}
