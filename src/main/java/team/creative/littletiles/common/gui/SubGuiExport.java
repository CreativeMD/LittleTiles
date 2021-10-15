package team.creative.littletiles.common.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.util.converation.StructureStringUtils;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import team.creative.littletiles.common.item.ItemLittleRecipe;
import team.creative.littletiles.common.placement.PlacementHelper;

public class SubGuiExport extends SubGui {
    
    public GuiTextfield textfield;
    
    @Override
    public void createControls() {
        textfield = new GuiTextfield("export", "", 10, 30, 150, 14);
        textfield.maxLength = Integer.MAX_VALUE;
        controls.add(textfield);
        
        controls.add(new GuiButton("Copy", 10, 52) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                StringSelection stringSelection = new StringSelection(textfield.text);
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });
        
        List<String> lines = new ArrayList<>();
        lines.add("structure");
        lines.add("model");
        controls.add(new GuiComboBox("type", 43, 52, 100, lines));
    }
    
    public void updateTextfield() {
        ItemStack stack = ((SubContainerExport) container).slot.getStackInSlot(0);
        if (stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemLittleRecipe)) {
            GuiComboBox box = (GuiComboBox) get("type");
            if (box.index == 0)
                textfield.text = StructureStringUtils.exportStructure(stack);
            else
                textfield.text = StructureStringUtils.exportModel(stack);;
        } else
            textfield.text = "";
    }
    
    @CustomEventSubscribe
    public void onSelectionChanged(GuiControlChangedEvent event) {
        updateTextfield();
    }
    
    @CustomEventSubscribe
    public void onSlotChange(SlotChangeEvent event) {
        updateTextfield();
    }
    
}
