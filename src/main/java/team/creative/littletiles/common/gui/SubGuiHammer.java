package team.creative.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBoxTranslated;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.api.ILittleEditor;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.gui.configure.SubGuiConfigure;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class SubGuiHammer extends SubGuiConfigure {
    
    public SubGuiHammer(ItemStack stack) {
        super(140, 150, stack);
    }
    
    public LittleGridContext getContext() {
        return ((ILittleEditor) stack.getItem()).getPositionContext(stack);
    }
    
    @Override
    public void saveConfiguration() {
        GuiComboBox box = (GuiComboBox) get("shape");
        GuiScrollBox scroll = (GuiScrollBox) get("settings");
        LittleShape shape = ShapeRegistry.getShape(box.getCaption());
        
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        nbt.setString("shape", shape.getKey());
        shape.saveCustomSettings(scroll, nbt, getContext());
    }
    
    @Override
    public void createControls() {
        GuiComboBox box = new GuiComboBoxTranslated("shape", 0, 0, 134, "shape.", new ArrayList<>(ShapeRegistry.notTileShapeNames()));
        box.select(ItemLittleHammer.getShape(stack).getKey());
        GuiScrollBox scroll = new GuiScrollBox("settings", 0, 23, 134, 120);
        controls.add(box);
        controls.add(scroll);
        onChange();
    }
    
    @CustomEventSubscribe
    public void onComboBoxChange(GuiControlChangedEvent event) {
        if (event.source.is("shape"))
            onChange();
    }
    
    public void onChange() {
        GuiComboBox box = (GuiComboBox) get("shape");
        GuiScrollBox scroll = (GuiScrollBox) get("settings");
        
        LittleShape shape = ShapeRegistry.getShape(box.getCaption());
        scroll.controls.clear();
        scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound(), getContext()));
        scroll.refreshControls();
    }
    
}
