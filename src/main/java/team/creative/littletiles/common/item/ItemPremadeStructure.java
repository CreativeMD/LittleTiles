package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.mc.NBTUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.LittleToolPlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiModeSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittlePremadeType;

public class ItemPremadeStructure extends Item implements ILittlePlacer, IItemTooltip {
    
    public static ItemStack of(ItemStack stack, String structure) {
        CompoundTag nbt = new CompoundTag();
        CompoundTag structNbt = new CompoundTag();
        structNbt.putString("id", structure);
        nbt.put(LittleGroup.STRUCTURE_KEY, structNbt);
        ILittleTool.setData(stack, nbt);
        return stack;
    }
    
    public static ItemStack of(String structure) {
        return of(new ItemStack(LittleTilesRegistry.PREMADE), structure);
    }
    
    public static String getPremadeId(ItemStack stack) {
        return ILittleTool.getData(stack).getCompound(LittleGroup.STRUCTURE_KEY).getString("id");
    }
    
    public static LittlePremadeType get(ItemStack stack) {
        return LittlePremadeRegistry.get(ILittleTool.getData(stack).getCompound(LittleGroup.STRUCTURE_KEY).getString("id"));
    }
    
    public ItemPremadeStructure() {
        super(new Item.Properties());
    }
    
    @Override
    public String getDescriptionId(ItemStack stack) {
        return "structure." + getPremadeId(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        if (LanguageUtils.can("structure.description." + getPremadeId(stack)))
            list.addAll(new TextBuilder().translate("structure.description." + getPremadeId(stack)).build());
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
    public boolean hasTiles(ItemStack stack) {
        return LittlePremadeRegistry.getPreview(getPremadeId(stack)) != null;
    }
    
    private LittleGroup getPreviews(String id) {
        LittleGroup previews = LittlePremadeRegistry.getLittleGroup(id);
        if (previews != null)
            return previews.copy();
        return null;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        String id = getPremadeId(stack);
        LittleGroup previews = getPreviews(id);
        var data = ILittleTool.getData(stack);
        if (previews != null && previews.getStructureTag() != null && data.contains(LittleGroup.STRUCTURE_KEY))
            NBTUtils.mergeNotOverwrite(previews.getStructureTag(), data.getCompound(LittleGroup.STRUCTURE_KEY));
        return previews;
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return getTiles(stack);
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return true;
    }
    
    @Override
    public boolean canSnapToGrid(ItemStack stack) {
        LittleStructureType type = LittlePremadeRegistry.get(getPremadeId(stack));
        if (type instanceof LittlePremadeType premade)
            return premade.canSnapToGrid();
        return false;
    }
    
    @Override
    public boolean snapToGridByDefault(ItemStack stack) {
        LittleStructureType type = LittlePremadeRegistry.get(getPremadeId(stack));
        if (type instanceof LittlePremadeType premade)
            return premade.snapToGrid;
        return false;
    }
    
    @Override
    public String tooltipTranslateKey(ItemStack stack, String defaultKey) {
        return "littletiles.tiles.tooltip";
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { LittleTilesClient.KEY_CONFIGURE.getTranslatedKeyMessage(), LittleTilesClient.arrowKeysTooltip(), LittleTilesClient.KEY_MIRROR
                .getTranslatedKeyMessage() };
    }
    
    @Override
    public LittleTool tool(ItemStack stack) {
        return new LittleToolPlacer(stack);
    }
}
