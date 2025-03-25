package team.creative.littletiles.common.gui.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.control.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiShowItem;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.element.LittleElement.NotBlockException;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.control.GuiGridConfig;
import team.creative.littletiles.common.gui.control.GuiShapeConfiguration;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public class GuiChisel extends GuiConfigureTool {
    
    public GuiChisel(ContainerSlotView view) {
        super("chisel", 230, 200, view);
        registerEventChanged(x -> {
            if (x.control.is("picker", "preview"))
                updateLabel();
        });
        flow = GuiFlow.STACK_Y;
        valign = VAlign.STRETCH;
        
        registerEventChanged(x -> {
            if (x.control.is("mode")) {
                GuiComboBox<PlacementMode> modeBox = (GuiComboBox<PlacementMode>) x.control;
                TextBuilder builder = new TextBuilder();
                if (modeBox.selected().canPlaceStructures())
                    builder.text("" + ChatFormatting.BOLD).translate("placement.mode.placestructure").text("" + ChatFormatting.WHITE).newLine();
                builder.translate(modeBox.selected().translatableKey() + ".tooltip");
                ((GuiLabel) get("text")).setTitle(builder.build());
                LittleTilesClient.placementMode(modeBox.selected());
            }
        });
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        
        GuiParent upper = new GuiParent();
        add(upper);
        
        GuiParent left = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        upper.add(left);
        
        LittleElement element = LittleElement.getOrDefault(tool.get());
        Color color = new Color(element.color);
        left.add(new GuiColorPicker("picker", color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        
        GuiParent parent = new GuiParent(GuiFlow.STACK_X).setVAlign(VAlign.CENTER);
        left.add(parent);
        parent.add(new GuiShowItem("item").setDim(60, 60));
        parent.add(new GuiGridConfig("grid", getPlayer(), PlacementPlayerSetting.grid(getPlayer()), LittleTilesClient::grid));
        
        GuiStackSelector selector = new GuiStackSelector("preview", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
        selector.setSelectedForce(element.getBlock().getStack());
        left.add(selector);
        
        GuiParent right = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        upper.add(right);
        
        GuiComboBox<PlacementMode> modeBox = new GuiComboBox<>("mode", PlacementMode.map());
        modeBox.select(PlacementPlayerSetting.placementMode(getPlayer()));
        right.add(modeBox);
        right.add(new GuiLabel("text"));
        raiseEvent(new GuiControlChangedEvent(modeBox));
        
        add(new GuiShapeConfiguration(getPlayer(), "shape", tool));
        updateLabel();
    }
    
    public void updateLabel() {
        GuiStackSelector selector = (GuiStackSelector) get("preview");
        ItemStack selected = selector.getSelected();
        GuiColorPicker picker = get("picker");
        
        LittleElement element;
        try {
            element = LittleElement.of(selected, picker.color.toInt());
        } catch (NotBlockException e) {
            element = new LittleElement(LittleElement.getOrDefault(tool.get()), picker.color.toInt());
        }
        
        get("item", GuiShowItem.class).stack = ItemMultiTiles.of(element);
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        data.set(LittleTilesRegistry.SHAPE.get(), get("shape", GuiShapeConfiguration.class).save());
        
        GuiColorPicker picker = get("picker");
        
        ItemStack selected = get("preview", GuiStackSelector.class).getSelected();
        LittleElement element;
        try {
            element = LittleElement.of(selected, picker.color.toInt());
        } catch (NotBlockException e) {
            element = new LittleElement(LittleElement.getOrDefault(tool.get()), picker.color.toInt());
        }
        data.set(LittleTilesRegistry.ELEMENT.get(), element);
        
        return true;
    }
}
