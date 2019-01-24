package com.creativemd.littletiles.common.utils.ingredients;

import com.creativemd.creativecore.common.utils.sorting.BlockSelector;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class IngredientUtils {
	
	private static PairList<BlockSelector, BlockIngredientRule> rules = new PairList<>();
	
	public static void registerRule(BlockSelector selector, BlockIngredientRule rule) {
		rules.add(selector, rule);
	}
	
	public static BlockIngredient getBlockIngredient(Block block, int meta, double value) {
		for (Pair<BlockSelector, BlockIngredientRule> pair : rules) {
			if (pair.key.is(block, meta))
				return pair.value.getBlockIngredient(block, meta, value);
		}
		return new BlockIngredient(block, meta, value);
	}
	
	public static BlockIngredient getBlockIngredient(NBTTagCompound nbt) {
		Block block = Block.getBlockFromName(nbt.getString("block"));
		if (block == null || block instanceof BlockAir)
			return null;
		if (nbt.getDouble("volume") > 0)
			return new BlockIngredient(block, nbt.getInteger("meta"), nbt.getDouble("volume"));
		return null;
	}
	
	public static BlockIngredient getIngredientsOfStackSimple(ItemStack stack) {
		Block block = Block.getBlockFromItem(stack.getItem());
		
		if (block != null && !(block instanceof BlockAir) && LittleAction.isBlockValid(block))
			return getBlockIngredient(block, stack.getItemDamage(), 1);
		return null;
	}
	
	/** @return does not take care of stackSize */
	public static CombinedIngredients getIngredientsOfStack(ItemStack stack) {
		if (!stack.isEmpty()) {
			ILittleTile tile = PlacementHelper.getLittleInterface(stack);
			
			if (tile != null && tile.hasLittlePreview(stack) && tile.containsIngredients(stack)) {
				LittlePreviews tiles = tile.getLittlePreview(stack);
				if (tiles != null) {
					CombinedIngredients ingredients = new CombinedIngredients();
					for (int i = 0; i < tiles.size(); i++) {
						LittleTilePreview preview = tiles.get(i);
						if (preview.canBeConvertedToBlockEntry()) {
							ingredients.block.addIngredient(preview.getBlockIngredient(tiles.context));
							ingredients.color.addColorUnit(ColorUnit.getColors(tiles.context, preview));
						}
					}
					return ingredients;
				}
			}
			
			Block block = Block.getBlockFromItem(stack.getItem());
			
			if (block != null && !(block instanceof BlockAir)) {
				if (LittleAction.isBlockValid(block)) {
					CombinedIngredients ingredients = new CombinedIngredients();
					ingredients.block.addIngredient(getBlockIngredient(block, stack.getItemDamage(), 1));
					return ingredients;
				}
			}
		}
		return null;
	}
	
}
