package team.creative.littletiles.common.item;

import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.GuiModeSelector;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class ItemMultiTiles extends Item implements ILittlePlacer {
    
    public static ItemStack of(LittleElement element) {
        ItemStack stack = new ItemStack(LittleTiles.ITEM_TILES);
        LittleGroup group = new LittleGroup();
        group.add(LittleGrid.min(), element, LittleGrid.min().box());
        stack.setTag(LittleGroup.save(group));
        return stack;
    }
    
    public static PlacementMode currentMode = PlacementMode.getDefault();
    public static LittleGrid currentContext;
    
    public ItemMultiTiles() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB));
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        if (stack.getOrCreateTag().contains("structure") && stack.getOrCreateTagElement("structure").contains("name"))
            return new TextComponent(stack.getOrCreateTagElement("structure").getString("name"));
        return super.getName(stack);
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return LittleGroup.load(stack.getOrCreateTag());
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return LittleGroup.loadLow(stack.getOrCreateTag());
    }
    
    @Override
    public PlacementPreview getPlacement(Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        return PlacementPreview.relative(level, stack, position, allowLowResolution);
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup group) {
        stack.setTag(LittleGroup.save(group));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String id = "none";
        if (stack.getOrCreateTag().contains("structure"))
            id = stack.getOrCreateTagElement("structure").getString("id");
        tooltip.add(new TranslatableComponent("gui.structure").append(": ").append(new TranslatableComponent("structure." + id)));
        tooltip.add(new TextComponent("" + stack.getOrCreateTag().getInt("count")).append(new TranslatableComponent("gui.tile.count")));
    }
    
    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (allowdedIn(tab))
            for (ExampleStructures example : ExampleStructures.values())
                if (example.stack != null)
                    list.add(example.stack);
    }
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        return currentMode;
    }
    
    @Override
    public GuiConfigure getConfigureAdvanced(Player player, ItemStack stack) {
        return new GuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
            
            @Override
            public CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, PlacementMode mode) {
                ItemMultiTiles.currentContext = grid;
                ItemMultiTiles.currentMode = mode;
                return nbt;
            }
            
        };
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return currentContext;
    }
    
    @Override
    public LittleVec getCachedSize(ItemStack stack) {
        return LittleGroup.getSize(stack.getOrCreateTag());
    }
    
    @Override
    public LittleVec getCachedMin(ItemStack stack) {
        return LittleGroup.getMin(stack.getOrCreateTag());
    }
    
    public static void reloadExampleStructures() {
        for (ExampleStructures example : ExampleStructures.values()) {
            try {
                example.stack = new ItemStack(LittleTiles.ITEM_TILES);
                example.stack
                        .setTag(TagParser.parseTag(IOUtils.toString(LittleStructurePremade.class.getClassLoader().getResourceAsStream(example.getFileName()), Charsets.UTF_8)));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not load '" + example.name() + " example structure!");
            }
        }
    }
    
    private static enum ExampleStructures {
        
        BASIC_LEVER,
        DOUBLE_DOOR,
        LIGHT_SWITCH,
        SIMPLE_LIGHT,
        STONE_PLATE,
        WOODEN_PLATE;
        
        public ItemStack stack;
        
        public String getFileName() {
            return "assets/" + LittleTiles.MODID + "/example/" + name().toLowerCase() + ".struct";
        }
        
    }
    
}
