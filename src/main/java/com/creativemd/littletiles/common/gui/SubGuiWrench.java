package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiItemListBox;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiWrench extends SubGui {

	@Override
	public void createControls() {
		controls.add(new GuiButton("Craft", 70, 3, 40){

			@Override
			public void onClicked(int x, int y, int button) {
				ItemStack stack1 = ((SubContainerWrench)container).basic.getStackInSlot(0);
				ItemStack stack2 = ((SubContainerWrench)container).basic.getStackInSlot(1);
				
				GuiItemListBox listBox = (GuiItemListBox) get("missing");
				listBox.clear();
				
				if(stack1 != null)
				{
					if(stack1.getItem() instanceof ItemRecipe)
					{
						ArrayList<LittleTilePreview> tiles = ItemRecipe.getPreview(stack1);
						ArrayList<BlockEntry> required = SubContainerWrench.getRequiredIngredients(tiles);
						ArrayList<BlockEntry> remaining = new ArrayList<>();
						
						boolean success = true;
						if(!container.player.isCreative()){
							success = SubContainerWrench.drainIngridients(required, stack2, false, remaining, true) ||SubContainerWrench.drainIngridients(required, getPlayer().inventory, false, remaining, false);
						}else
							required.clear();
						
						if(remaining.size() > 0 && !ItemTileContainer.canStoreRemains(getPlayer()))
							success = false;
						
						for (int i = 0; i < required.size(); i++) {
							listBox.add(SubGuiTileContainer.getStringOfValue(required.get(i).value), required.get(i).getItemStack());
						}
						
						if(!success)
							return ;
					}
				}
				sendPacketToServer(new NBTTagCompound());
			}
			
		});
		controls.add(new GuiItemListBox("missing", 5, 25, 160, 50, new ArrayList<ItemStack>(), new ArrayList<String>()));
	}

}
