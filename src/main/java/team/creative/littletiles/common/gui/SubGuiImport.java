package team.creative.littletiles.common.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiImport extends SubGui {
    
    public GuiTextfield textfield;
    
    @Override
    public void createControls() {
        textfield = new GuiTextfield("import", "", 10, 30, 150, 14);
        controls.add(textfield);
        controls.add(new GuiButton("Paste", 10, 52) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                StringSelection stringSelection = new StringSelection(textfield.text);
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable t = clpbrd.getContents(this);
                if (t == null)
                    return;
                try {
                    textfield.text = (String) t.getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                
                }
            }
        });
                
        controls.add(new GuiButton("Import", 128, 52) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                try {
                    NBTTagCompound nbt = JsonToNBT.getTagFromJson(textfield.text);
                    try {
                        LittleGridContext.get(nbt);
                        sendPacketToServer(nbt);
                    } catch (RuntimeException e) {
                        openButtonDialogDialog("Invalid grid size " + nbt.getInteger("grid"), "Ok");
                    }
                } catch (NBTException e) {
                
                }
            }
        });
    }
    
}
