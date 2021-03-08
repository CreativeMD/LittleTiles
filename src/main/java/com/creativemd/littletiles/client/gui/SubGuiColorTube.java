package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.api.IBoxSelector;
import com.creativemd.littletiles.common.item.ItemLittlePaintBrush;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.select.SelectShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiColorTube extends SubGuiConfigure {
    
    public SubGuiColorTube(ItemStack stack) {
        super(140, 173, stack);
    }
    
    public LittleGridContext getContext() {
        return ((IBoxSelector) stack.getItem()).getContext(stack);
    }
    
    @Override
    public void createControls() {
        Color color = ColorUtils.IntToRGBA(ItemLittlePaintBrush.getColor(stack));
        // color.setAlpha(255);
        controls.add(new GuiColorPicker("picker", 2, 2, color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        
        ArrayList<String> shapes = new ArrayList<>(SelectShape.keys());
        shapes.add(0, "tile");
        GuiComboBox box = new GuiComboBox("shape", 0, 50, 134, shapes);
        SelectShape shape = ItemLittlePaintBrush.getShape(stack);
        box.select(shape == null ? "tile" : shape.key);
        GuiScrollBox scroll = new GuiScrollBox("settings", 0, 73, 134, 90);
        controls.add(box);
        controls.add(scroll);
        onChange();
    }
    
    @Override
    public void saveConfiguration() {
        GuiComboBox box = (GuiComboBox) get("shape");
        GuiScrollBox scroll = (GuiScrollBox) get("settings");
        SelectShape shape = box.getCaption().equals("tile") || box.getCaption().equals("") ? null : SelectShape.getShape(box.getCaption());
        
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        nbt.setString("shape", shape == null ? "tile" : shape.key);
        GuiColorPicker picker = (GuiColorPicker) get("picker");
        nbt.setInteger("color", ColorUtils.RGBAToInt(picker.color));
        if (shape != null)
            shape.saveCustomSettings(scroll, nbt, getContext());
    }
    
    @CustomEventSubscribe
    public void onComboBoxChange(GuiControlChangedEvent event) {
        if (event.source.is("shape"))
            onChange();
    }
    
    public void onChange() {
        GuiComboBox box = (GuiComboBox) get("shape");
        GuiScrollBox scroll = (GuiScrollBox) get("settings");
        
        scroll.controls.clear();
        SelectShape shape = box.getCaption().equals("tile") || box.getCaption().equals("") ? null : SelectShape.getShape(box.getCaption());
        if (shape != null) {
            scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound(), getContext()));
            scroll.refreshControls();
        }
    }
    
}
