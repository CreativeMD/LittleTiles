package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.LittleToolPlacer;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.convertion.OldLittleTilesDataParser;
import team.creative.littletiles.common.convertion.OldLittleTilesDataParser.LittleMissingGridException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiModeSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public class ItemMultiTiles extends Item implements ILittlePlacer, IItemTooltip {
    
    private static final List<ItemStack> EXAMPLES = new ArrayList<>();
    
    public static ItemStack of(LittleElement element) {
        return of(element, LittleGrid.MIN, LittleGrid.MIN.box());
    }
    
    public static ItemStack of(LittleGroup group) {
        ItemStack stack = new ItemStack(LittleTilesRegistry.ITEM_TILES.value());
        ILittleTool.setData(stack, LittleGroup.save(group));
        return stack;
    }
    
    public static ItemStack of(LittleElement element, LittleGrid grid, LittleBox box) {
        ItemStack stack = new ItemStack(LittleTilesRegistry.ITEM_TILES.value());
        LittleGroup group = new LittleGroup();
        group.add(grid, element, box);
        ILittleTool.setData(stack, LittleGroup.save(group));
        return stack;
    }
    
    public static void reloadExampleStructures(ResourceManager manager) {
        EXAMPLES.clear();
        Map<ResourceLocation, Resource> files = manager.listResources("example", x -> x.getNamespace().equals(LittleTiles.MODID) && x.getPath().endsWith(".struct"));
        for (Entry<ResourceLocation, Resource> file : files.entrySet()) {
            try {
                ItemStack stack = new ItemStack(LittleTilesRegistry.ITEM_TILES.value());
                var in = file.getValue().open();
                CompoundTag nbt = TagParser.parseTag(IOUtils.toString(in, Charsets.UTF_8));
                if (OldLittleTilesDataParser.isOld(nbt))
                    nbt = OldLittleTilesDataParser.convert(nbt);
                ILittleTool.setData(stack, nbt);
                EXAMPLES.add(stack);
                in.close();
            } catch (LittleMissingGridException e) {
                
            } catch (Exception e) {
                LittleTiles.LOGGER.error("Could not load '" + file.getKey() + "' example structure!", e);
            }
        }
    }
    
    public static void collectExamples(CreativeModeTab.Output output) {
        for (ItemStack stack : EXAMPLES)
            output.accept(stack);
    }
    
    public ItemMultiTiles() {
        super(new Item.Properties());
    }
    
    public static String getStructure(ItemStack stack) {
        var data = ILittleTool.getData(stack);
        if (data.contains(LittleGroup.STRUCTURE_KEY))
            return data.getCompound(LittleGroup.STRUCTURE_KEY).getString("id");
        return "";
    }
    
    @Override
    public Component getName(ItemStack stack) {
        var data = ILittleTool.getData(stack);
        if (data.contains(LittleGroup.STRUCTURE_KEY) && data.getCompound(LittleGroup.STRUCTURE_KEY).contains("n"))
            return Component.literal(data.getCompound(LittleGroup.STRUCTURE_KEY).getString("n"));
        return super.getName(stack);
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return LittleGroup.load(ILittleTool.getData(stack));
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return LittleGroup.loadLow(ILittleTool.getData(stack));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String id = "none";
        var data = ILittleTool.getData(stack);
        if (data.contains(LittleGroup.STRUCTURE_KEY))
            id = data.getCompound(LittleGroup.STRUCTURE_KEY).getString("id");
        tooltip.add(Component.translatable("gui.structure").append(": ").append(Component.translatable("structure." + id)));
        tooltip.add(LittleGroup.printTooltip(data));
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view, boolean secondary) {
        return new GuiModeSelector(view, PlacementPlayerSetting.grid(player), PlacementPlayerSetting.placementMode(player)) {
            
            @Override
            public boolean saveConfiguration(DataComponentMap data, LittleGrid grid, PlacementMode mode) {
                LittleTilesClient.setPlace(grid, mode);
                return false;
            }
            
        };
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleVecGrid getCachedSize(ItemStack stack) {
        return LittleGroup.getSize(ILittleTool.getData(stack));
    }
    
    @Override
    public LittleVecGrid getCachedMin(ItemStack stack) {
        return LittleGroup.getMin(ILittleTool.getData(stack));
    }
    
    @Override
    public String tooltipTranslateKey(ItemStack stack, String defaultKey) {
        return "littletiles.tiles.tooltip";
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { LittleTilesClient.KEY_CONFIGURE.getTranslatedKeyMessage(), LittleTilesClient.KEY_BUILDING_MODE.getTranslatedKeyMessage(), LittleTilesClient
                .arrowKeysTooltip(), LittleTilesClient.KEY_MIRROR.getTranslatedKeyMessage() };
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleTool tool(ItemStack stack) {
        return new LittleToolPlacer(stack);
    }
    
}
