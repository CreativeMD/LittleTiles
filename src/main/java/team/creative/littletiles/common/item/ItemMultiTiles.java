package team.creative.littletiles.common.item;

import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.convertion.OldLittleTilesDataParser;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiModeSelector;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class ItemMultiTiles extends Item implements ILittlePlacer {
    
    public static ItemStack of(LittleElement element) {
        return of(element, LittleGrid.MIN, LittleGrid.MIN.box());
    }
    
    public static ItemStack of(LittleElement element, LittleGrid grid, LittleBox box) {
        ItemStack stack = new ItemStack(LittleTilesRegistry.ITEM_TILES.get());
        LittleGroup group = new LittleGroup();
        group.add(grid, element, box);
        stack.setTag(LittleGroup.save(group));
        return stack;
    }
    
    public ItemMultiTiles() {
        super(new Item.Properties());
    }
    
    public static String getStructure(ItemStack stack) {
        if (stack.getOrCreateTag().contains(LittleGroup.STRUCTURE_KEY))
            return stack.getTag().getCompound(LittleGroup.STRUCTURE_KEY).getString("id");
        return "";
    }
    
    @Override
    public Component getName(ItemStack stack) {
        if (stack.getOrCreateTag().contains(LittleGroup.STRUCTURE_KEY) && stack.getOrCreateTagElement(LittleGroup.STRUCTURE_KEY).contains("name"))
            return Component.literal(stack.getOrCreateTagElement(LittleGroup.STRUCTURE_KEY).getString("name"));
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
        if (stack.getOrCreateTag().contains(LittleGroup.STRUCTURE_KEY))
            id = stack.getOrCreateTagElement(LittleGroup.STRUCTURE_KEY).getString("id");
        tooltip.add(Component.translatable("gui.structure").append(": ").append(Component.translatable("structure." + id)));
        tooltip.add(LittleGroup.printTooltip(stack.getOrCreateTag()));
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return new GuiModeSelector(view, LittleTilesClient.grid(), LittleTilesClient.placementMode()) {
            
            @Override
            public CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, PlacementMode mode) {
                LittleTilesClient.setPlace(grid, mode);
                return null;
            }
            
        };
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return true;
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
                example.stack = new ItemStack(LittleTilesRegistry.ITEM_TILES.get());
                CompoundTag nbt = TagParser.parseTag(IOUtils.toString(LittleStructurePremade.class.getClassLoader().getResourceAsStream(example.getFileName()), Charsets.UTF_8));
                if (OldLittleTilesDataParser.isOld(nbt))
                    nbt = OldLittleTilesDataParser.convert(nbt);
                example.stack.setTag(nbt);
            } catch (Exception e) {
                e.printStackTrace();
                LittleTiles.LOGGER.error("Could not load '{}' example structure!", example.name());
            }
        }
    }
    
    public static enum ExampleStructures {
        
        BASIC_LEVER,
        DOUBLE_DOOR,
        LIGHT_SWITCH,
        SIMPLE_LIGHT,
        STONE_PLATE,
        WOODEN_PLATE;
        
        public ItemStack stack;
        
        public String getFileName() {
            return "data/" + LittleTiles.MODID + "/example/" + name().toLowerCase() + ".struct";
        }
        
    }
    
}
