package team.creative.littletiles.common.gui.controls.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiListBoxBase;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonContext;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;

public class GuiElementFilterGroup extends GuiElementFilter {
    
    protected final GuiListBoxBase<GuiElementFilter> list;
    protected GuiStateButtonMapped<GuiElementFilterOperator> operator;
    
    public GuiElementFilterGroup(Player player, GuiElementFilterOperator operator, BiFilter<IParentCollection, LittleTile>... filters) {
        add(list = new GuiListBoxBase<>("list", true, new ArrayList<>(Stream.of(filters).map(x -> GuiElementFilter.of(player, x)).toList())));
        list.setExpandable();
        list.setDim(new GuiSizeRules().minHeight(40));
        var map = new TextMapBuilder<Consumer<Integer>>();
        map.addComponent(x -> list.addItem(GuiElementFilter.of(getPlayer(), TileFilters.block(Blocks.STONE))), Component.translatable("gui.filter.block"));
        map.addComponent(x -> list.addItem(GuiElementFilter.of(getPlayer(), TileFilters.tag(BlockTags.LOGS))), Component.translatable("gui.filter.block_tag"));
        map.addComponent(x -> list.addItem(GuiElementFilter.of(getPlayer(), TileFilters.color(ColorUtils.WHITE))), Component.translatable("gui.filter.color"));
        map.addComponent(x -> list.addItem(GuiElementFilter.of(getPlayer(), TileFilters.or())), Component.translatable("gui.filter.group"));
        GuiParent lower = new GuiParent();
        add(lower);
        lower.add(this.operator = new GuiStateButtonMapped<>("operator", new TextMapBuilder<GuiElementFilterOperator>().addComponent(GuiElementFilterOperator.values(),
            x -> Component.translatable("gui.filter." + x.name().toLowerCase()))));
        this.operator.select(operator);
        lower.add(new GuiButtonContext("add", map).setTranslate("gui.plus").setDim(new GuiSizeRules().maxWidth(6).maxHeight(8)));
    }
    
    protected BiFilter<IParentCollection, LittleTile>[] getSubFilters() {
        List<BiFilter<IParentCollection, LittleTile>> filters = new ArrayList<>();
        for (GuiElementFilter element : list.items()) {
            var filter = element.get();
            if (filter != null)
                filters.add(filter);
        }
        return filters.toArray(new BiFilter[filters.size()]);
    }
    
    @Override
    public BiFilter<IParentCollection, LittleTile> get() {
        return operator.getSelected(GuiElementFilterOperator.OR).create(getSubFilters());
    }
    
    public static enum GuiElementFilterOperator {
        
        OR {
            @Override
            public BiFilter<IParentCollection, LittleTile> create(BiFilter<IParentCollection, LittleTile>[] subFilters) {
                if (subFilters.length == 1)
                    return subFilters[0];
                return TileFilters.or(subFilters);
            }
        },
        AND {
            @Override
            public BiFilter<IParentCollection, LittleTile> create(BiFilter<IParentCollection, LittleTile>[] subFilters) {
                if (subFilters.length == 1)
                    return subFilters[0];
                return TileFilters.and(subFilters);
            }
        },
        NOT_OR {
            @Override
            public BiFilter<IParentCollection, LittleTile> create(BiFilter<IParentCollection, LittleTile>[] subFilters) {
                if (subFilters.length == 1)
                    return TileFilters.not(subFilters[0]);
                return TileFilters.not(TileFilters.or(subFilters));
            }
        },
        NOT_AND {
            @Override
            public BiFilter<IParentCollection, LittleTile> create(BiFilter<IParentCollection, LittleTile>[] subFilters) {
                if (subFilters.length == 1)
                    return TileFilters.not(subFilters[0]);
                return TileFilters.not(TileFilters.and(subFilters));
            }
        };
        
        public abstract BiFilter<IParentCollection, LittleTile> create(BiFilter<IParentCollection, LittleTile>[] subFilters);
    }
    
}