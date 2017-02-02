package com.creativemd.littletiles.common.jei;

import com.creativemd.littletiles.LittleTiles;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class LittleJeiPlugin implements IModPlugin {

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {
		
	}

	@Override
	public void register(IModRegistry registry) {
		IItemBlacklist itemBlacklist = registry.getJeiHelpers().getItemBlacklist();
		itemBlacklist.addItemToBlacklist(new ItemStack(LittleTiles.blockTile));
		
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		
	}

}
