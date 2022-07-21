package team.creative.littletiles.common.gui.premade;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.client.export.LittleExportType;
import team.creative.littletiles.common.placement.PlacementHelper;

public class GuiExport extends GuiLayer {
    
    public GuiTextfield textfield;
    public Container exportSlot = new SimpleContainer(1);
    
    public GuiExport() {
        super("gui.little.export");
    }
    
    @Override
    public void create() {
        add(new GuiInventoryGrid("export", exportSlot));
        
        GuiParent row = new GuiParent(GuiFlow.STACK_X);
        row.setExpandableX();
        add(row);
        textfield = new GuiTextfield("export");
        textfield.setMaxStringLength(Integer.MAX_VALUE);
        row.add(textfield);
        
        row.add(new GuiButton("Copy", x -> {
            StringSelection stringSelection = new StringSelection(textfield.getText());
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        }));
        
        add(new GuiComboBoxMapped<LittleExportType>("type", new TextMapBuilder<LittleExportType>()
                .addEntrySet(LittleExportType.REGISTRY.entrySet(), x -> new TranslatableComponent("gui.export." + x.getKey()))));
        
        add(new GuiPlayerInventoryGrid(getPlayer()));
        
        registerEventChanged(x -> {
            ItemStack stack = exportSlot.getItem(0);
            if (!stack.isEmpty() && PlacementHelper.isLittleBlock(stack))
                textfield.setText(((GuiComboBoxMapped<LittleExportType>) get("type")).getSelected().export(stack));
            else
                textfield.setText("");
        });
    }
}
