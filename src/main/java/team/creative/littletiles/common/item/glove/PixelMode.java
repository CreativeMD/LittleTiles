package team.creative.littletiles.common.item.glove;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.controls.simple.GuiShowItem;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.controls.GuiGridConfig;
import team.creative.littletiles.common.gui.tool.GuiGlove;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public class PixelMode extends ElementGloveMode {
    
    public static LittleBox getBox(ItemStack stack) {
        if (stack.getOrCreateTag().contains("box"))
            return LittleBox.create(stack.getTag().getIntArray("box"));
        LittleBox box = new LittleBox(0, 0, 0, 1, 1, 1);
        setBox(stack.getOrCreateTag(), box);
        return box;
    }
    
    public static void setBox(CompoundTag nbt, LittleBox box) {
        nbt.putIntArray("box", box.getArray());
    }
    
    @Override
    public void loadGui(GuiGlove gui) {
        Player player = gui.getPlayer();
        ItemStack stack = gui.tool.get();
        ILittleTool tool = (ILittleTool) stack.getItem();
        LittleElement element = getElement(stack);
        LittleBox box = PixelMode.getBox(stack);
        LittleGrid oldContext = LittleGrid.get(stack.getTag());
        LittleGrid grid = tool.getPositionGrid(stack);
        
        if (oldContext != grid)
            box.convertTo(oldContext, grid);
        if (box.minX == box.maxX)
            box.maxX++;
        if (box.minY == box.maxY)
            box.maxY++;
        if (box.minZ == box.maxZ)
            box.maxZ++;
        LittleVec size = box.getSize();
        
        gui.registerEventChanged(x -> updateLabel(gui));
        
        GuiParent parent = new GuiParent();
        gui.add(parent.setExpandableX());
        GuiParent left = new GuiParent();
        parent.add(left);
        left.add(new GuiShowItem("item", ItemStack.EMPTY).setDim(32, 32));
        
        GuiParent right = new GuiParent(GuiFlow.STACK_Y);
        parent.add(right.setAlign(Align.RIGHT).setExpandableX());
        right.add(new GuiSteppedSlider("sizeX", size.x, 1, grid.count));
        right.add(new GuiSteppedSlider("sizeY", size.y, 1, grid.count));
        right.add(new GuiSteppedSlider("sizeZ", size.z, 1, grid.count));
        right.add(new GuiGridConfig("grid", grid, x -> {
            GuiSteppedSlider sizeX = gui.get("sizeX");
            sizeX.maxValue = x.count;
            sizeX.value = Mth.clamp(sizeX.value, sizeX.minValue, sizeX.maxValue);
            
            GuiSteppedSlider sizeY = gui.get("sizeY");
            sizeY.maxValue = x.count;
            sizeY.value = Mth.clamp(sizeY.value, sizeY.minValue, sizeY.maxValue);
            
            GuiSteppedSlider sizeZ = gui.get("sizeZ");
            sizeZ.maxValue = x.count;
            sizeZ.value = Mth.clamp(sizeZ.value, sizeZ.minValue, sizeZ.maxValue);
        }));
        
        gui.add(new GuiColorPicker("picker", new Color(element.color), LittleTiles.CONFIG.isTransparencyEnabled(player), LittleTiles.CONFIG.getMinimumTransparency(player)));
        
        GuiStackSelector selector = new GuiStackSelector("preview", player, LittleGuiUtils.getCollector(player), true);
        selector.setSelectedForce(element.getBlock().getStack());
        gui.add(selector.setExpandableX());
        
        updateLabel(gui);
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
    
    protected LittleBox getBox(GuiGlove gui) {
        int sizeX = gui.get("sizeX", GuiSteppedSlider.class).getValue();
        int sizeY = gui.get("sizeY", GuiSteppedSlider.class).getValue();
        int sizeZ = gui.get("sizeZ", GuiSteppedSlider.class).getValue();
        return new LittleBox(0, 0, 0, sizeX, sizeY, sizeZ);
    }
    
    protected LittleGrid getGrid(GuiGlove gui) {
        return gui.get("grid", GuiGridConfig.class).get();
    }
    
    protected void updateLabel(GuiGlove gui) {
        gui.get("item", GuiShowItem.class).stack = ItemMultiTiles.of(getElement(gui), getGrid(gui), getBox(gui));
    }
    
    @Override
    public void saveGui(GuiGlove gui, CompoundTag nbt) {
        setElement(nbt, getElement(gui));
        setBox(nbt, getBox(gui));
        getGrid(gui).set(nbt);
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> tooltip) {
        super.addExtraInformation(nbt, tooltip);
        tooltip.add(Component.literal(TooltipUtils.printColor(getElement(nbt).color)));
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        LittleGroup group = new LittleGroup();
        group.addTile(LittleGrid.get(stack.getTag()), new LittleTile(getElement(stack), getBox(stack)));
        return group;
    }
    
    @Override
    public void setTiles(LittleGroup previews, ItemStack stack) {}
    
}