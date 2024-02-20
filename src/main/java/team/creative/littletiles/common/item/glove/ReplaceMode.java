package team.creative.littletiles.common.item.glove;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.filter.TileFilters;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.tool.GuiGlove;
import team.creative.littletiles.common.level.LittleLevelScanner;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class ReplaceMode extends ElementGloveMode {
    
    @Override
    public void loadGui(GuiGlove gui) {
        Player player = gui.getPlayer();
        ItemStack stack = gui.tool.get();
        LittleElement element = getElement(stack);
        
        gui.add(new GuiColorPicker("picker", new Color(element.color), LittleTiles.CONFIG.isTransparencyEnabled(player), LittleTiles.CONFIG.getMinimumTransparency(player)));
        
        GuiStackSelector selector = new GuiStackSelector("preview", player, LittleGuiUtils.getCollector(player), true);
        selector.setSelectedForce(element.getBlock().getStack());
        gui.add(selector.setExpandableX());
    }
    
    protected LittleElement getElement(GuiGlove gui) {
        GuiStackSelector selector = gui.get("preview");
        GuiColorPicker picker = gui.get("picker");
        ItemStack selected = selector.getSelected();
        
        if (!selected.isEmpty() && selected.getItem() instanceof BlockItem item)
            return new LittleElement(item.getBlock().defaultBlockState(), picker.color.toInt());
        else
            return getElement(gui.tool.get());
    }
    
    @Override
    public void saveGui(GuiGlove gui, CompoundTag nbt) {
        setElement(nbt, getElement(gui));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean rightClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
        if (PlacementHelper.canBlockBeUsed(level, result.getBlockPos())) {
            LittleTileContext context = LittleTileContext.selectFocused(level, result.getBlockPos(), player);
            if (context.isComplete()) {
                LittleBoxes boxes;
                if (LittleActionHandlerClient.isUsingSecondMode())
                    boxes = LittleLevelScanner.scan(level, result.getBlockPos(), TileFilters.of(context.tile));
                else {
                    boxes = new LittleBoxesSimple(result.getBlockPos(), context.parent.getGrid());
                    boxes.add(context.box.copy());
                }
                return LittleTilesClient.ACTION_HANDLER.execute(new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.absolute(level, PlacementMode.REPLACE,
                    new LittleGroupAbsolute(boxes, getElement(stack)), Facing.get(result.getDirection()))));
            }
        }
        return false;
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return false;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return null;
    }
    
    @Override
    public void setTiles(LittleGroup previews, ItemStack stack) {}
    
}