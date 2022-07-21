package team.creative.littletiles.common.gui.premade;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleBlueprint;

public class GuiImport extends GuiLayer {
    
    public GuiTextfield textfield;
    public Container importSlot = new SimpleContainer(1);
    
    public GuiSyncLocal<CompoundTag> IMPORT_DATA = getSyncHolder().register("import_data", (nbt) -> {
        ItemStack stack = importSlot.getItem(0);
        if (stack.getItem() instanceof ItemLittleBlueprint || (getPlayer().isCreative() && stack.isEmpty())) {
            // TODO Import
            //importSlot.setItem(0, newStack);
        }
    });
    
    public GuiImport() {
        super("little.import");
        flow = GuiFlow.STACK_Y;
    }
    
    @Override
    public void create() {
        add(textfield = new GuiTextfield("import"));
        GuiParent secondRow = new GuiParent();
        secondRow.align = Align.RIGHT;
        secondRow.add(new GuiButton("Paste", x -> {
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable t = clpbrd.getContents(this);
            if (t == null)
                return;
            try {
                textfield.setText((String) t.getTransferData(DataFlavor.stringFlavor));
            } catch (Exception e) {}
            
        }));
        
        secondRow.add(new GuiButton("Import", x -> {
            
            try {
                CompoundTag nbt = TagParser.parseTag(textfield.getText());
                try {
                    LittleGrid.get(nbt);
                    IMPORT_DATA.send(nbt);
                } catch (RuntimeException e) {
                    GuiDialogHandler.openDialog(this, "invalid_grid", Component.translatable("invalid_grid", nbt.getString("grid")), (y, z) -> {}, DialogButton.OK);
                }
            } catch (CommandSyntaxException e) {}
        }));
        
        add(new GuiInventoryGrid("import", importSlot));
        
        add(new GuiPlayerInventoryGrid(getPlayer()));
    }
}
