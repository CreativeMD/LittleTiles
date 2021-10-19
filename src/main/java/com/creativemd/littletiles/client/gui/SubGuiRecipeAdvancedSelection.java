package com.creativemd.littletiles.client.gui;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.common.gui.controls.gui.GuiArraySlider;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.selection.mode.SelectionMode;
import com.creativemd.littletiles.common.util.selection.mode.SelectionMode.SelectionResult;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiRecipeAdvancedSelection extends SubGuiConfigure {
    
    public SubGuiRecipeAdvancedSelection(ItemStack stack) {
        super(200, 200, stack);
    }
    
    @Override
    public void saveConfiguration() {
        
    }
    
    public SelectionResult result;
    
    @Override
    public void createControls() {
        SelectionMode mode = ItemLittleRecipeAdvanced.getSelectionMode(stack);
        GuiComboBox box = new GuiComboBox("selection_mode", 0, 0, 100, translate(SelectionMode.names()));
        box.select(mode.name);
        controls.add(box);
        
        result = mode.generateResult(getPlayer().world, stack);
        
        GuiCheckBox vanilla = new GuiCheckBox("includeVanilla", translate("selection.include.vanilla"), 0, 23, false);
        if (result != null && result.blocks > 0)
            vanilla.setCustomTooltip(result.blocks + " block(s)");
        else
            vanilla.enabled = false;
        controls.add(vanilla);
        
        GuiCheckBox cb = new GuiCheckBox("includeCB", translate("selection.include.cb"), 0, 43, true);
        if (result != null && result.cbBlocks > 0)
            cb.setCustomTooltip(result.cbBlocks + " block(s)", result.cbTiles + " tile(s)", result.minCBContext.size + " grid");
        else
            cb.enabled = false;
        controls.add(cb);
        
        GuiCheckBox lt = new GuiCheckBox("includeLT", translate("selection.include.lt"), 0, 63, true);
        if (result != null && result.ltBlocks > 0)
            lt.setCustomTooltip(result.ltBlocks + " block(s)", result.ltTiles + " tile(s)", result.minLtContext.size + " grid");
        else
            lt.enabled = false;
        controls.add(lt);
        
        controls.add(new GuiCheckBox("remember_structure", translate("selection.include.structure"), 0, 83, true));
        // accurate
        GuiLabel label = new GuiLabel("label_scale", translate("selection.scale") + ":", 0, 102);
        controls.add(label);
        controls.add(new GuiArraySlider("scale", label.width + 5, 100, 100, 14, "", ""));
        updateSlider();
        
        controls.add(new GuiButton("save", translate("selection.save"), 114, 180, 80) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                boolean rememberStructure = ((GuiCheckBox) get("remember_structure")).value;
                boolean includeVanilla = ((GuiCheckBox) get("includeVanilla")).value;
                boolean includeCB = ((GuiCheckBox) get("includeCB")).value;
                boolean includeLT = ((GuiCheckBox) get("includeLT")).value;
                
                try {
                    if (rememberStructure && mode.getPreviews(getPlayer().world, getPlayer(), stack, includeVanilla, includeCB, includeLT, rememberStructure).isEmpty()) {
                        openButtonDialogDialog("Parent structure has to have at least one tile!\nDisable remember structure or adjust your selection.", "ok");
                        return;
                    }
                } catch (LittleActionException e) {
                    openButtonDialogDialog(e.getLocalizedMessage(), translate("gui.ok"));
                    return;
                }
                
                mode.saveSelection(stack);
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setBoolean("save_selection", true);
                nbt.setBoolean("includeVanilla", includeVanilla);
                nbt.setBoolean("includeCB", includeCB);
                nbt.setBoolean("includeLT", includeLT);
                nbt.setBoolean("remember_structure", rememberStructure);
                
                LittleGridContext minRequired = LittleGridContext.getMin();
                if (nbt.getBoolean("includeCB") && result.minCBContext != null)
                    minRequired = LittleGridContext.max(minRequired, result.minCBContext);
                if (nbt.getBoolean("includeLT") && result.minLtContext != null)
                    minRequired = LittleGridContext.max(minRequired, result.minLtContext);
                LittleGridContext selected = LittleGridContext.context[(int) (LittleGridContext.context.length - 1 - ((GuiArraySlider) get("scale")).value)];
                if (minRequired != selected) {
                    nbt.setInteger("grid", minRequired.size);
                    nbt.setInteger("aimedGrid", selected.size);
                }
                
                sendPacketToServer(nbt);
            }
        }.setEnabled(result != null));
    }
    
    @Override
    public void receiveContainerPacket(NBTTagCompound nbt) {
        stack.setTagCompound(nbt);
    }
    
    @CustomEventSubscribe
    public void onChanged(GuiControlChangedEvent event) {
        if (!event.source.is("scale"))
            updateSlider();
    }
    
    public void updateSlider() {
        GuiArraySlider slider = (GuiArraySlider) get("scale");
        boolean includeVanilla = ((GuiCheckBox) get("includeVanilla")).enabled && ((GuiCheckBox) get("includeVanilla")).value;
        boolean includeCB = ((GuiCheckBox) get("includeCB")).enabled && ((GuiCheckBox) get("includeCB")).value;
        boolean includeLT = ((GuiCheckBox) get("includeLT")).enabled && ((GuiCheckBox) get("includeLT")).value;
        
        if (result == null || (!includeVanilla && !includeCB && !includeLT))
            slider.setEnabled(false);
        else {
            LittleGridContext minRequired = LittleGridContext.getMin();
            if (includeCB && result.minCBContext != null)
                minRequired = LittleGridContext.max(minRequired, result.minCBContext);
            if (includeLT && result.minLtContext != null)
                minRequired = LittleGridContext.max(minRequired, result.minLtContext);
            
            String value = slider.getValue();
            
            String[] values = new String[LittleGridContext.context.length];
            for (LittleGridContext context : LittleGridContext.context) {
                values[values.length - 1 - context.index] = minRequired.size + ":" + context.size + " x" + (context.pixelSize / minRequired.pixelSize) + "";
            }
            slider.setValues(values);
            if (ArrayUtils.contains(values, value))
                slider.select(value);
            else
                slider.select(values[values.length - 1 - minRequired.index]);
            slider.setEnabled(true);
        }
    }
}
