package team.creative.littletiles.common.item;

import java.util.HashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.NBTUtils;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiModeSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadePreview;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittlePremadeType;

public class ItemPremadeStructure extends Item implements ILittlePlacer, IItemTooltip {
    
    private static HashMap<String, LittleGroup> cachedPreviews = new HashMap<>();
    
    public static ItemStack of(String structure) {
        ItemStack stack = new ItemStack(LittleTilesRegistry.PREMADE.value());
        CompoundTag nbt = new CompoundTag();
        CompoundTag structNbt = new CompoundTag();
        structNbt.putString("id", structure);
        nbt.put(LittleGroup.STRUCTURE_KEY, structNbt);
        ILittleTool.setData(stack, nbt);
        return stack;
    }
    
    public static String getPremadeId(ItemStack stack) {
        return ILittleTool.getData(stack).getCompound(LittleGroup.STRUCTURE_KEY).getString("id");
    }
    
    public static LittlePremadeType get(ItemStack stack) {
        return LittlePremadeRegistry.get(ILittleTool.getData(stack).getCompound(LittleGroup.STRUCTURE_KEY).getString("id"));
    }
    
    public static LittlePremadePreview getPremade(ItemStack stack) {
        return LittlePremadeRegistry.getPreview(ILittleTool.getData(stack).getCompound(LittleGroup.STRUCTURE_KEY).getString("id"));
    }
    
    public ItemPremadeStructure() {
        super(new Item.Properties());
    }
    
    @Override
    public String getDescriptionId(ItemStack stack) {
        return "structure." + getPremadeId(stack);
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return new GuiModeSelector(view, PlacementPlayerSetting.grid(player), PlacementPlayerSetting.placementMode(player)) {
            
            @Override
            public CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, PlacementMode mode) {
                LittleTilesClient.setPlace(grid, mode);
                return nbt;
            }
        };
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return LittlePremadeRegistry.getPreview(getPremadeId(stack)) != null;
    }
    
    public void removeUnnecessaryData(ItemStack stack) {
        var data = ILittleTool.getData(stack);
        if (!data.isEmpty()) {
            data.remove("tiles");
            data.remove("size");
            data.remove("min");
        }
        ILittleTool.setData(stack, data);
    }
    
    public static void clearCache() {
        cachedPreviews.clear();
    }
    
    private LittleGroup getPreviews(String id) {
        if (cachedPreviews.containsKey(id))
            return cachedPreviews.get(id).copy();
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
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client) {
        String id = getPremadeId(stack);
        LittleGroup previews = getPreviews(id);
        if (previews.isEmpty())
            return;
        previews.rotate(rotation, previews.getGrid().rotationCenter);
        saveTiles(stack, previews);
    }
    
    @Override
    public void mirror(Player player, ItemStack stack, Axis axis, boolean client) {
        String id = getPremadeId(stack);
        LittleGroup previews = getPreviews(id);
        if (previews.isEmpty())
            return;
        previews.mirror(axis, previews.getGrid().rotationCenter);
        saveTiles(stack, previews);
    }
    
    @Override
    public PlacementPreview getPlacement(Player player, Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        return PlacementPreview.relative(level, stack, position, allowLowResolution);
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup group) {
        cachedPreviews.put(getPremadeId(stack), group);
    }
    
    @Override
    public boolean sendTransformationUpdate() {
        return false;
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return true;
    }
    
    @Override
    public boolean shouldCache() {
        return false;
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
        return new Object[] { LittleTilesClient.configure.getTranslatedKeyMessage(), LittleTilesClient.arrowKeysTooltip(), LittleTilesClient.mirror.getTranslatedKeyMessage() };
    }
    
}
