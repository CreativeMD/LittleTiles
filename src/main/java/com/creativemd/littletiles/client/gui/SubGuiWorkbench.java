package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiItemListBox;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.container.SubContainerWorkbench;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.items.ItemRecipeAdvanced;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredientEntry;
import com.creativemd.littletiles.common.utils.ingredients.ColorIngredient;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;
import com.creativemd.littletiles.common.utils.ingredients.LittleInventory;
import com.creativemd.littletiles.common.utils.ingredients.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.utils.ingredients.StackIngredient;
import com.creativemd.littletiles.common.utils.ingredients.StackIngredientEntry;
import com.creativemd.littletiles.common.utils.tooltip.TooltipUtils;

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
						LittleInventory inventory = new LittleInventory(player);
						LittleIngredients ingredients = LittleAction.getIngredients(previews);
						
						try {
							if (LittleAction.checkAndTake(player, inventory, ingredients)) {
								sendPacketToServer(new NBTTagCompound());
							}
						} catch (NotEnoughIngredientsException e2) {
							LittleIngredients missing = e2.getIngredients();
							
							BlockIngredient blocks = missing.get(BlockIngredient.class);
							if (blocks != null)
								for (BlockIngredientEntry ingredient : blocks) {
									listBox.add(TooltipUtils.printVolume(ingredient.value, true), ingredient.getItemStack());
								}
							
							ColorIngredient color = missing.get(ColorIngredient.class);
							if (color != null) {
								if (color.black > 0)
									listBox.add(color.getBlackDescription(), ItemStack.EMPTY);
								if (color.cyan > 0)
									listBox.add(color.getCyanDescription(), ItemStack.EMPTY);
								if (color.magenta > 0)
									listBox.add(color.getMagentaDescription(), ItemStack.EMPTY);
								if (color.yellow > 0)
									listBox.add(color.getYellowDescription(), ItemStack.EMPTY);
							}
							
							StackIngredient stacks = missing.get(StackIngredient.class);
							if (stacks != null)
								for (StackIngredientEntry stack : stacks)
									listBox.add(stack.count + "", stack.stack);
								
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
