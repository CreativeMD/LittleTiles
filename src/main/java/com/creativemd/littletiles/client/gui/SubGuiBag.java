package com.creativemd.littletiles.client.gui;

import com.creativemd.creativecore.common.gui.client.style.ColoredDisplayStyle;
import com.creativemd.creativecore.common.gui.client.style.DisplayStyle;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlClickEvent;
import com.creativemd.littletiles.client.gui.controls.GuiColorProgressBar;
import com.creativemd.littletiles.common.container.SubContainerBag;
import com.creativemd.littletiles.common.util.ingredient.ColorIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.item.ItemLittleBag;

public class SubGuiBag extends SubGui {
    
    public ItemStack stack;
    public LittleIngredients bag;
    
    public SubGuiBag(ItemStack stack) {
        super();
        this.stack = stack;
    }
    
    public static Style blackStyle = new Style("black", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(50, 50, 50));
    public static Style cyanStyle = new Style("cyan", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(0, 255, 255), new ColoredDisplayStyle(50, 50, 50));
    public static Style magentaStyle = new Style("magenta", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(255, 0, 255), new ColoredDisplayStyle(50, 50, 50));
    public static Style yellowStyle = new Style("yellow", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(255, 255, 0), new ColoredDisplayStyle(50, 50, 50));
    
    @Override
    public void createControls() {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        bag = ((ItemLittleBag) stack.getItem()).getInventory(stack);
        ColorIngredient unit = bag.get(ColorIngredient.class);
        
        controls.add(new GuiColorProgressBar("black", 120, 26, 45, 3, ItemLittleBag.colorUnitMaximum, unit.black).setStyle(blackStyle));
        controls.add(new GuiColorProgressBar("cyan", 120, 40, 45, 3, ItemLittleBag.colorUnitMaximum, unit.cyan).setStyle(cyanStyle));
        controls.add(new GuiColorProgressBar("magenta", 120, 54, 45, 3, ItemLittleBag.colorUnitMaximum, unit.magenta).setStyle(magentaStyle));
        controls.add(new GuiColorProgressBar("yellow", 120, 68, 45, 3, ItemLittleBag.colorUnitMaximum, unit.yellow).setStyle(yellowStyle));
    }
    
    @CustomEventSubscribe
    public void clicked(GuiControlClickEvent event) {
        if (event.source instanceof GuiColorProgressBar) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("color", true);
            nbt.setString("type", event.source.name);
            sendPacketToServer(nbt);
        }
    }
    
    @Override
    public void receiveContainerPacket(NBTTagCompound nbt) {
        if (nbt.getBoolean("reload")) {
            nbt.removeTag("reload");
            stack.setTagCompound(nbt);
            controls.clear();
            createControls();
            refreshControls();
            container.controls.clear();
            ((SubContainerBag) container).stack = stack;
            container.createControls();
            container.refreshControls();
            addContainerControls();
            refreshControls();
        }
    }
    
}
