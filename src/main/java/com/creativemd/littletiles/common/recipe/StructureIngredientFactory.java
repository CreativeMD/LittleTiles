package com.creativemd.littletiles.common.recipe;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructureTypePremade;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class StructureIngredientFactory implements IIngredientFactory {
	
	@Override
	public Ingredient parse(JsonContext context, JsonObject json) {
		if (json.has("structure")) {
			
			ItemStack stack = new ItemStack(LittleTiles.premade);
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagCompound structureNBT = new NBTTagCompound();
			String id = json.get("structure").getAsString();
			
			if (!(LittleStructureRegistry.getStructureType(id) instanceof LittleStructureTypePremade))
				throw new JsonSyntaxException("Unkown structure type '" + json.get("structure").getAsString() + "'!");
			
			structureNBT.setString("id", id);
			nbt.setTag("structure", structureNBT);
			stack.setTagCompound(nbt);
			
			return Ingredient.fromStacks(stack.copy());
		}
		throw new JsonSyntaxException("Missing 'structure' type!");
	}
	
}
