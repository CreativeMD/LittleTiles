package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiListBox;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.item.ItemStack;

public class SubGuiRecipeAdvanced extends SubGuiConfigure {
	
	public static List<String> getScaleRatios()
	{
		List<String> scales = new ArrayList<>();
		double scale = LittleTile.gridSize;
		while(scale % 1 == 0)
		{
			scales.add(LittleTile.gridSize + ":" + ((int) scale));
			scale /= 2D;
		}
		return scales;
	}

	public SubGuiRecipeAdvanced(ItemStack stack) {
		super(140, 120, stack);
	}

	@Override
	public void saveConfiguration() {
		GuiComboBox box = (GuiComboBox) get("scale");
		stack.getTagCompound().setInteger("scale", Integer.parseInt(box.caption.replace(LittleTile.gridSize + ":", "")));
	}

	@Override
	public void createControls() {
		controls.add(new GuiComboBox("scale", 0, 0, 130, getScaleRatios()));
		controls.add(new GuiButton("save", 105, 95) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				closeGui();
			}
		});		
	}

}
