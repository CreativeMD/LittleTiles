package team.creative.littletiles.common.gui.premade;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.littletiles.common.util.converation.StructureStringUtils;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.util.text.TextListBuilder;
import team.creative.littletiles.common.gui.SubContainerExport;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.placement.PlacementHelper;

public class GuiExport extends GuiLayer {
    
    public GuiTextfield textfield;
    
    @Override
    public void create() {
        textfield = new GuiTextfield("export");
        textfield.setMaxStringLength(Integer.MAX_VALUE);
        add(textfield);
        
        add(new GuiButton("Copy", x -> {
            StringSelection stringSelection = new StringSelection(textfield.getText());
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        }));
        
        add(new GuiComboBox("type", new TextListBuilder().addTranslated("gui.export.", "structure", "model")));
        
        registerEventChanged(x -> updateTextfield());
    }
    
    public void updateTextfield() {
        ItemStack stack = ((SubContainerExport) container).slot.getStackInSlot(0);
        if (stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemLittleBlueprint)) {
            GuiComboBox box = (GuiComboBox) get("type");
            if (box.getIndex() == 0)
                textfield.setText(StructureStringUtils.exportStructure(stack));
            else
                textfield.setText(StructureStringUtils.exportModel(stack));
        } else
            textfield.setText("");
    }
    
    @CustomEventSubscribe
    public void onSlotChange(SlotChangeEvent event) {
        updateTextfield();
    }
    
}
