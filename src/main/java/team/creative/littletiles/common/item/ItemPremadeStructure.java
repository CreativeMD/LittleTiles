package team.creative.littletiles.common.item;

import java.util.HashMap;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.NBTUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.GuiModeSelector;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructurePremadeEntry;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructureTypePremade;

public class ItemPremadeStructure extends Item implements ILittlePlacer {
    
    public ItemPremadeStructure() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB));
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "." + getPremadeId(stack);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public GuiConfigure getConfigureAdvanced(Player player, ContainerSlotView view) {
        return new GuiModeSelector(view, ItemMultiTiles.currentContext, ItemLittleChisel.currentMode) {
            
            @Override
            public CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, PlacementMode mode) {
                ItemLittleChisel.currentMode = mode;
                ItemMultiTiles.currentContext = grid;
                return nbt;
            }
        };
    }
    
    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (allowedIn(tab))
            for (LittleStructureTypePremade entry : LittleStructurePremade.getPremadeStructureTypes())
                if (entry.showInCreativeTab)
                    list.add(entry.createItemStack());
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return LittleStructurePremade.getStructurePremadeEntry(getPremadeId(stack)) != null;
    }
    
    public void removeUnnecessaryData(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove("tiles");
            stack.getTag().remove("size");
            stack.getTag().remove("min");
        }
    }
    
    private static HashMap<String, LittleGroup> cachedPreviews = new HashMap<>();
    
    public static void clearCache() {
        cachedPreviews.clear();
    }
    
    private LittleGroup getPreviews(String id) {
        if (cachedPreviews.containsKey(id))
            return cachedPreviews.get(id).copy();
        LittleGroup previews = LittleStructurePremade.getPreviews(id);
        if (previews != null)
            return previews.copy();
        return null;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        String id = getPremadeId(stack);
        LittleGroup previews = getPreviews(id);
        if (previews != null && previews.getStructureTag() != null && stack.getOrCreateTag().contains("structure"))
            NBTUtils.mergeNotOverwrite(previews.getStructureTag(), stack.getTag().getCompound("structure"));
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
    public PlacementPreview getPlacement(Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
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
    public PlacementMode getPlacementMode(ItemStack stack) {
        if (!ItemMultiTiles.currentMode.canPlaceStructures())
            return PlacementMode.getStructureDefault();
        return ItemMultiTiles.currentMode;
    }
    
    @Override
    public boolean shouldCache() {
        return false;
    }
    
    @Override
    public boolean snapToGridByDefault(ItemStack stack) {
        LittleStructureType type = LittleStructureRegistry.getStructureType(getPremadeId(stack));
        if (type instanceof LittleStructureTypePremade)
            return ((LittleStructureTypePremade) type).snapToGrid;
        return false;
    }
    
    public static String getPremadeId(ItemStack stack) {
        return stack.getOrCreateTagElement("structure").getString("id");
    }
    
    public static LittleStructurePremadeEntry getPremade(ItemStack stack) {
        return LittleStructurePremade.getStructurePremadeEntry(stack.getOrCreateTagElement("structure").getString("id"));
    }
    
}
