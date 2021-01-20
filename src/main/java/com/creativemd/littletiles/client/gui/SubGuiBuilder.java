package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCounter;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructureBuilder;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiBuilder extends SubGui {
    
    public LittleStructureBuilder builder;
    
    public SubGuiBuilder(LittleStructureBuilder builder) {
        this.builder = builder;
    }
    
    @Override
    public void createControls() {
        controls.add(new GuiLabel(translate("gui.frame_builder.width"), 0, 1));
        controls.add(new GuiLabel(translate("gui.frame_builder.height"), 90, 1));
        controls.add(new GuiLabel(translate("gui.frame_builder.thickness"), 0, 21));
        controls.add(new GuiCounter("width", 33, 0, 50, builder.lastSizeX, 1, Integer.MAX_VALUE));
        controls.add(new GuiCounter("height", 120, 0, 50, builder.lastSizeY, 1, Integer.MAX_VALUE));
        controls.add(new GuiCounter("thickness", 53, 20, 30, builder.lastThickness, 1, Integer.MAX_VALUE));
        
        List<String> names = new ArrayList<>(LittleStructureBuilder.getNames());
        List<String> translatedNames = new ArrayList<>();
        for (String id : names)
            translatedNames.add(translate("structure." + id + ".name"));
        
        GuiComboBox box = new GuiComboBox("type", 0, 60, 100, translatedNames);
        box.select(translate("structure." + builder.lastStructureType + ".name"));
        if (box.index == -1)
            box.select(0);
        controls.add(box);
        controls.add(new GuiStateButton("grid", LittleGridContext.getNames().indexOf(builder.lastGrid + ""), 145, 38, 20, 12, LittleGridContext.getNames()
            .toArray(new String[0])));
        GuiStackSelectorAll selector = new GuiStackSelectorAll("preview", 0, 38, 112, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
        selector.setSelectedForce(new ItemStack(builder.lastBlockState.getBlock(), 1, builder.lastBlockState.getBlock().getMetaFromState(builder.lastBlockState)));
        controls.add(selector);
        controls.add(new GuiLabel("failed", translate("gui.frame_builder.failed"), 90, 20, ColorUtils.RED).setVisible(false));
        controls.add(new GuiButton(translate("gui.frame_builder.craft"), 110, 60) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                if ((getPlayer().isCreative() && builder.inventory.getStackInSlot(0).isEmpty()) || ItemLittleRecipeAdvanced.isRecipe(builder.inventory.getStackInSlot(0).getItem())) {
                    get("failed").visible = false;
                    NBTTagCompound nbt = new NBTTagCompound();
                    GuiCounter width = (GuiCounter) get("width");
                    nbt.setInteger("width", width.getValue());
                    GuiCounter height = (GuiCounter) get("height");
                    nbt.setInteger("height", height.getValue());
                    GuiCounter thickness = (GuiCounter) get("thickness");
                    nbt.setInteger("thickness", thickness.getValue());
                    GuiComboBox box = (GuiComboBox) get("type");
                    nbt.setString("type", names.get(box.index));
                    GuiStateButton grid = (GuiStateButton) get("grid");
                    LittleGridContext context;
                    try {
                        context = LittleGridContext.get(Integer.parseInt(grid.getCaption()));
                    } catch (NumberFormatException e) {
                        context = LittleGridContext.get();
                    }
                    nbt.setInteger("grid", context.size);
                    ItemStack stack = selector.getSelected();
                    IBlockState state = BlockUtils.getState(stack);
                    Block block = state.getBlock();
                    int meta = block.getMetaFromState(state);
                    nbt.setString("block", block.getRegistryName().toString() + (meta != 0 ? ":" + meta : ""));
                    sendPacketToServer(nbt);
                } else
                    get("failed").visible = true;
            }
        });
    }
    
}
