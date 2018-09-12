package com.creativemd.littletiles.common.ingredients;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BlockIngredient {

	public Block block;
	public int meta;
	public double value;

	public BlockIngredient(Block block, int meta, double value) {
		this.block = block;
		this.meta = meta;
		this.value = value;
	}

	public ItemStack getItemStack() {
		return new ItemStack(block, 1, meta);
	}

	public ItemStack getTileItemStack() {
		ItemStack stack = new ItemStack(LittleTiles.blockTileNoTicking);
		NBTTagCompound nbt = new NBTTagCompound();
		new LittleTileSize(1, 1, 1).writeToNBT("size", nbt);

		LittleTile tile = new LittleTileBlock(block, meta);
		tile.saveTileExtra(nbt);
		nbt.setString("tID", "BlockTileBlock");
		stack.setTagCompound(nbt);

		int count = (int) (value / LittleGridContext.get().minimumTileSize);
		if (count == 0) {
			LittleGridContext.getMax().set(stack.getTagCompound());
			count = (int) (value / LittleGridContext.getMax().minimumTileSize);
		}
		stack.setCount(count);
		return stack;
	}

	@Override
	public int hashCode() {
		return block.hashCode() + meta;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof BlockIngredient && ((BlockIngredient) object).block == this.block && ((BlockIngredient) object).meta == this.meta;
	}

	public IBlockState getState() {
		return block.getStateFromMeta(meta);
	}

	public boolean is(ItemStack stack) {
		return Block.getBlockFromItem(stack.getItem()) == this.block && stack.getItemDamage() == this.meta;
	}

	public BlockIngredient copy() {
		return new BlockIngredient(block, meta, value);
	}

	public BlockIngredient copy(double value) {
		return new BlockIngredient(this.block, this.meta, value);
	}

	public static class BlockIngredients {
		private List<BlockIngredient> content;

		public BlockIngredients() {
			this.content = new ArrayList<>();
		}

		public BlockIngredients(List<BlockIngredient> ingredients) {
			this();
			addIngredients(ingredients);
		}

		public List<BlockIngredient> getIngredients() {
			return content;
		}

		public void addIngredients(BlockIngredients ingredients) {
			addIngredients(ingredients.content);
		}

		public void addIngredients(List<BlockIngredient> ingredients) {
			for (BlockIngredient ingredient : ingredients) {
				addIngredient(ingredient);
			}
		}

		public void addIngredient(BlockIngredient ingredient) {
			if (ingredient == null)
				return;
			int indexOf = content.indexOf(ingredient);
			if (indexOf != -1) {
				content.get(indexOf).value += ingredient.value;
			} else
				content.add(ingredient.copy());
		}

		/**
		 * 
		 * @param stack
		 *            to drain from. Will only take necessary count
		 * @return left overs
		 */
		public BlockIngredient drainItemStack(ItemStack stack) {
			BlockIngredient ingredient = LittleAction.getIngredientsOfStackSimple(stack);

			int indexOf = content.indexOf(ingredient);
			if (indexOf != -1) {
				BlockIngredient ownIngredient = content.get(indexOf);

				int amount = (int) Math.ceil(ownIngredient.value / ingredient.value);
				double volume = ingredient.value * amount;
				stack.shrink(amount);

				if (volume >= ownIngredient.value) {
					content.remove(indexOf);
					if (volume > ownIngredient.value)
						return new BlockIngredient(ingredient.block, ingredient.meta, volume - ownIngredient.value);
				} else
					ownIngredient.value -= volume;
			}
			return null;
		}

		public void drainIngredient(BlockIngredient ingredient) {
			int indexOf = content.indexOf(ingredient);
			if (indexOf != -1) {
				BlockIngredient ownIngredient = content.get(indexOf);
				ownIngredient.value -= ingredient.value;
				if (ownIngredient.value <= 0)
					content.remove(indexOf);
			}
		}

		public void drainIngredients(BlockIngredients ingredients) {
			for (BlockIngredient ingredient : ingredients.content) {
				drainIngredient(ingredient);
			}
		}

		public BlockIngredient getEqualIngredient(BlockIngredient ingredient) {
			int indexOf = content.indexOf(ingredient);
			if (indexOf != -1)
				return content.get(indexOf);
			return null;
		}

		public boolean isEmpty() {
			return content.isEmpty();
		}

		public BlockIngredients copy() {
			return new BlockIngredients(content);
		}

		public int size() {
			return content.size();
		}

	}
}
