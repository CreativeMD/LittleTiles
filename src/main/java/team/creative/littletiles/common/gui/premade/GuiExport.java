package team.creative.littletiles.common.gui.premade;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.client.export.LittleExportType;
import team.creative.littletiles.common.placement.PlacementHelper;

public class GuiExport extends GuiLayer {
    
    public GuiTextfield textfield;
    public Container exportSlot = new SimpleContainer(1);
    
    public GuiExport() {
        super("export");
        this.flow = GuiFlow.STACK_Y;
        this.align = Align.STRETCH;
    }
    
    @Override
    public void create() {
        add(new GuiInventoryGrid("export", exportSlot).addListener(x -> {
            ItemStack stack = exportSlot.getItem(0);
            if (!stack.isEmpty() && PlacementHelper.isLittleBlock(stack))
                textfield.setText(((GuiComboBoxMapped<LittleExportType>) get("type")).getSelected().export(stack));
            else
                textfield.setText("");
        }));
        
        add(new GuiComboBoxMapped<LittleExportType>("type", new TextMapBuilder<LittleExportType>().addEntrySet(LittleExportType.REGISTRY.entrySet(), x -> Component.translatable(
            "gui.export." + x.getKey()))));
        
        GuiParent row = new GuiParent(GuiFlow.STACK_X);
        add(row);
        textfield = new GuiTextfield("export");
        textfield.setMaxStringLength(Integer.MAX_VALUE);
        row.add(textfield);
        
        if (isClient())
            row.add(new GuiButton("Copy", x -> Minecraft.getInstance().keyboardHandler.setClipboard(textfield.getText())).setTranslate("gui.copy"));
        
        add(new GuiPlayerInventoryGrid(getPlayer()).setUnexpandableX());
        
    }
    
    @Override
    public void closed() {
        super.closed();
        PlayerUtils.addOrDrop(getPlayer(), exportSlot);
    }
}
