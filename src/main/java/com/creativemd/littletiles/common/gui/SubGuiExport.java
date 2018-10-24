package com.creativemd.littletiles.common.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.gui.event.container.SlotChangeEvent;
import com.creativemd.littletiles.common.container.SubContainerExport;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.utils.converting.StructureStringUtils;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;

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
		
		controls.add(new GuiButton("Export model", 43, 52, 100) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				if (this.caption.equals("Export model")) {
					ItemStack stack = ((SubContainerExport) container).slot.getStackInSlot(0);
					if (stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemRecipe)) {
						textfield.text = StructureStringUtils.exportModel(stack);
						this.caption = "Export structure";
						this.customTooltip.clear();
						this.customTooltip.add("Export structure instead,");
						this.customTooltip.add("can be imported again!");
					} else
						textfield.text = "";
					
				} else {
					ItemStack stack = ((SubContainerExport) container).slot.getStackInSlot(0);
					if (stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemRecipe)) {
						textfield.text = StructureStringUtils.exportStructure(stack);
						this.caption = "Export model";
						this.customTooltip.clear();
						this.customTooltip.add("Export minecraft model instead,");
						this.customTooltip.add("cannot be imported again!");
					} else
						textfield.text = "";
				}
			}
		}.setCustomTooltip("Export minecraft model instead.", "CANNOT be imported again!"));
	}
	
	@CustomEventSubscribe
	public void onSlotChange(SlotChangeEvent event) {
		ItemStack stack = ((SubContainerExport) container).slot.getStackInSlot(0);
		if (stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemRecipe)) {
			textfield.text = StructureStringUtils.exportStructure(stack);
		} else
			textfield.text = "";
	}
	
}
