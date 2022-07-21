package team.creative.littletiles.common.gui.premade;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.common.grid.LittleGrid;

public class GuiImport extends GuiLayer {
    
    public GuiTextfield textfield;
    
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
            StringSelection stringSelection = new StringSelection(textfield.getText());
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
                    sendPacketToServer(nbt);
                } catch (RuntimeException e) {
                    openButtonDialogDialog("Invalid grid size " + nbt.getInt("grid"), "Ok");
                }
            } catch (CommandSyntaxException e) {}
        }));
    }
    
}
