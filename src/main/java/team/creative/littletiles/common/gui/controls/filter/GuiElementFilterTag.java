package team.creative.littletiles.common.gui.controls.filter;

import java.util.Optional;

import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;

public class GuiElementFilterTag extends GuiElementFilter {
    
    protected GuiComboBoxMapped<TagKey<Block>> box;
    
    public GuiElementFilterTag(TagKey<Block> selected) {
        add(box = new GuiComboBoxMapped<>("tag", new TextMapBuilder<TagKey<Block>>().addComponents(BuiltInRegistries.BLOCK.getTagNames().toList(), x -> {
            TextBuilder builder = new TextBuilder();
            Optional<Named<Block>> tag = BuiltInRegistries.BLOCK.getTag(x);
            if (tag.isPresent() && tag.get().size() > 0)
                builder.stack(new ItemStack(tag.get().get(0).value()));
            return builder.text(x.location().toString()).build();
        })));
        box.setSearchbar(true);
        box.select(selected);
    }
    
    @Override
    public BiFilter<IParentCollection, LittleTile> get() {
        TagKey<Block> selected = box.getSelected();
        if (selected != null)
            return TileFilters.tag(selected);
        return null;
    }
    
}
