package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.gui.GuiRenderHelper;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiItemListBox;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException.NotEnoughVolumeExcepion;
import com.creativemd.littletiles.common.container.SubContainerWorkbench;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.items.ItemRecipeAdvanced;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiWorkbench extends SubGui {
	
	public SubGuiWorkbench() {
		super(200, 200);
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiLabel("->", 25, 6));
		controls.add(new GuiButton("Craft", 70, 3, 40) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				ItemStack stack1 = ((SubContainerWorkbench) container).basic.getStackInSlot(0);
				ItemStack stack2 = ((SubContainerWorkbench) container).basic.getStackInSlot(1);
				
				GuiItemListBox listBox = (GuiItemListBox) get("missing");
				GuiLabel label = (GuiLabel) get("label");
				label.caption = "";
				listBox.clear();
				
				if (!stack1.isEmpty()) {
					if (stack1.getItem() instanceof ItemRecipe || stack1.getItem() instanceof ItemRecipeAdvanced) {
						LittlePreviews previews = LittleTilePreview.getPreview(stack1);
						
						EntityPlayer player = getPlayer();
						
						ColorUnit color = new ColorUnit();
						BlockIngredients ingredients = new BlockIngredients();
						for (LittleTilePreview preview : previews) {
							if (preview.canBeConvertedToBlockEntry()) {
								ingredients.addIngredient(preview.getBlockIngredient(previews.context));
								color.addColorUnit(ColorUnit.getColors(previews.context, preview));
							}
						}
						try {
							if (LittleAction.drainIngredients(player, ingredients, color)) {
								sendPacketToServer(new NBTTagCompound());
							}
						} catch (NotEnoughIngredientsException e) {
							if (e instanceof NotEnoughVolumeExcepion) {
								for (BlockIngredient ingredient : ingredients.getIngredients()) {
									listBox.add(ingredient.value > 1 ? ingredient.value + " blocks" : (int) (ingredient.value * previews.context.maxTilesPerBlock) + " pixels", ingredient.getItemStack());
								}
							} else {
								label.caption = e.getLocalizedMessage();
								label.width = GuiRenderHelper.instance.getStringWidth(label.caption) + label.getContentOffset() * 2;
							}
						}
						
					} else {
						sendPacketToServer(new NBTTagCompound());
					}
				}
				
			}
			
		});
		controls.add(new GuiItemListBox("missing", 5, 25, 180, 70, new ArrayList<ItemStack>(), new ArrayList<String>()));
		controls.add(new GuiLabel("label", "", 5, 102, ColorUtils.RGBAToInt(255, 50, 50, 255)));
	}
	
}
