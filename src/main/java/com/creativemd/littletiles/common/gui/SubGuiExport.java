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

		controls.add(new GuiButton("Export model", 43, 52) {

			@Override
			public void onClicked(int x, int y, int button) {
				ItemStack stack = ((SubContainerExport) container).slot.getStackInSlot(0);
				if (stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemRecipe)) {
					textfield.text = StructureStringUtils.exportModel(stack);
				} else
					textfield.text = "";
			}
		});
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
