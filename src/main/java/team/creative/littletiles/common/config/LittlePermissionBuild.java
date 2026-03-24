package team.creative.littletiles.common.config;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.premade.ToggleableConfig;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittlePermissionBuild {
    
    static {
        ConfigTypeConveration.registerTypeCreator(LittlePermissionBuild.class, () -> new LittlePermissionBuild());
    }
    
    public LittlePermissionBuild() {}
    
    public LittlePermissionBuild(boolean survival) {
        affectedBlockLimit.setEnabled(survival);
        editBlockLimit.setEnabled(survival);
        placeBlockLimit.setEnabled(survival);
        
        editUnbreakable = !survival;
        minimumTransparency = 0;
        harvestLevelBlock = survival ? HarvestLevel.STONE : HarvestLevel.DIAMOND;
    }
    
    @CreativeConfig
    public HarvestLevel harvestLevelBlock = HarvestLevel.WOOD;
    
    @CreativeConfig
    public boolean editUnbreakable = false;
    
    @CreativeConfig
    @CreativeConfig.IntRange(min = 0, max = 255)
    public int minimumTransparency = 0;
    
    @CreativeConfig
    public ToggleableConfig<Integer> affectedBlockLimit = new ToggleableConfig<Integer>(2, true);
    
    @CreativeConfig
    public ToggleableConfig<Integer> editBlockLimit = new ToggleableConfig<Integer>(10, true);
    
    @CreativeConfig
    public ToggleableConfig<Integer> placeBlockLimit = new ToggleableConfig<Integer>(10, true);
    
    @CreativeConfig
    public ToggleableConfig<Integer> blueprintSizeLimit = new ToggleableConfig<Integer>(16, false);
    
    @CreativeConfig
    public ToggleableConfig<Integer> gridLimit = new ToggleableConfig<Integer>(32, false);
    
    @CreativeConfig
    public int defaultGrid = 16;
    
    @CreativeConfig
    public boolean placeTransformableBoxes = true;
    
    public int maxGrid() {
        if (!gridLimit.isEnabled() || gridLimit.value >= LittleGrid.getMax().count)
            return LittleGrid.getMax().getIndex();
        int index = 0;
        for (LittleGrid grid : LittleGrid.grids()) {
            if (grid.count > gridLimit.value)
                break;
            index++;
        }
        return index;
    }
    
    public TextMapBuilder<LittleGrid> gridBuilder() {
        if (!gridLimit.isEnabled() || gridLimit.value >= LittleGrid.getMax().count)
            return LittleGrid.mapBuilder();
        TextMapBuilder<LittleGrid> map = new TextMapBuilder<LittleGrid>();
        for (LittleGrid grid : LittleGrid.grids()) {
            if (grid.count > gridLimit.value)
                break;
            map.addComponent(grid, Component.literal("" + grid.count));
        }
        return map;
    }
    
    public LittleGrid getOrDefault(int grid) {
        LittleGrid littleGrid = LittleGrid.tryGet(grid);
        if (littleGrid != null)
            return littleGrid;
        return defaultGrid();
    }
    
    public LittleGrid defaultGrid() {
        LittleGrid grid = LittleGrid.tryGet(defaultGrid);
        if (grid != null)
            return grid;
        return LittleGrid.overallDefault();
    }
    
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
