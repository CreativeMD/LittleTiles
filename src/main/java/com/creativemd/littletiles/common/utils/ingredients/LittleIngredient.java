package com.creativemd.littletiles.common.utils.ingredients;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.ingredients.NotEnoughIngredientsException.NotEnoughSpaceException;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.DyeUtils;

public abstract class LittleIngredient<T extends LittleIngredient> extends LittleIngredientBase<T> {
	
	private static HashMap<Class<? extends LittleIngredient>, Integer> types = new HashMap<>();
	private static HashMap<Integer, Class<? extends LittleIngredient>> typesInv = new HashMap<>();
	private static HashMap<Class<? extends LittleIngredient>, IngredientOverflowHandler> typesOverflowHandler = new HashMap<>();
	private static List<IngredientConvertionHandler> typesConverationHandler = new ArrayList<>();
	private static int index = 0;
	
	public static int indexOf(LittleIngredient ingredient) {
		return indexOf(ingredient.getClass());
	}
	
	public static int indexOf(Class<? extends LittleIngredient> type) {
		return types.get(type);
	}
	
	public static List<ItemStack> handleOverflow(LittleIngredient ingredient) throws NotEnoughSpaceException {
		return typesOverflowHandler.get(ingredient.getClass()).handleOverflow(ingredient);
	}
	
	static void extract(LittleIngredients ingredients, LittlePreviews previews) {
		for (IngredientConvertionHandler handler : typesConverationHandler)
			ingredients.add(handler.extract(previews));
		
		if (previews.hasStructure())
			previews.getStructure().addIngredients(ingredients);
		
		if (previews.hasChildren())
			for (LittlePreviews child : previews.getChildren())
				extract(ingredients, child);
	}
	
	public static LittleIngredients extractWithoutCount(ItemStack stack, boolean useLTStructures) {
		LittleIngredients ingredients = new LittleIngredients();
		ILittleTile tile = PlacementHelper.getLittleInterface(stack);
		
		if (tile != null) {
			if (useLTStructures && tile.hasLittlePreview(stack) && tile.containsIngredients(stack))
				extract(ingredients, tile.getLittlePreview(stack));
		} else {
			for (IngredientConvertionHandler handler : typesConverationHandler)
				ingredients.add(handler.extract(stack));
			
		}
		
		if (ingredients.isEmpty())
			return null;
		return ingredients;
	}
	
	public static int getSize() {
		return index + 1;
	}
	
	public static void registerConvationHandler(IngredientConvertionHandler converationHandler) {
		typesConverationHandler.add(converationHandler);
	}
	
	public static <T extends LittleIngredient> void registerType(Class<T> type, IngredientOverflowHandler<T> overflowHandler, IngredientConvertionHandler<T> converationHandler) {
		if (types.containsKey(type))
			throw new RuntimeException("Duplicate found! " + types);
		
		types.put(type, index);
		typesInv.put(index, type);
		typesOverflowHandler.put(type, overflowHandler);
		if (converationHandler != null)
			registerConvationHandler(converationHandler);
		index++;
	}
	
	static {
		registerType(BlockIngredient.class, new IngredientOverflowHandler<BlockIngredient>() {
			
			@Override
			public List<ItemStack> handleOverflow(BlockIngredient overflow) throws NotEnoughSpaceException {
				throw new NotEnoughSpaceException(overflow);
			}
		}, new IngredientConvertionHandler<BlockIngredient>() {
			
			@Override
			public BlockIngredient extract(ItemStack stack) {
				Block block = Block.getBlockFromItem(stack.getItem());
				
				if (block != null && !(block instanceof BlockAir) && LittleAction.isBlockValid(block)) {
					BlockIngredient ingredient = new BlockIngredient();
					ingredient.add(IngredientUtils.getBlockIngredient(block, stack.getItemDamage(), 1));
					return ingredient;
				}
				return null;
			}
			
			@Override
			public BlockIngredient extract(LittlePreviews previews) {
				BlockIngredient ingredient = new BlockIngredient();
				for (LittleTilePreview preview : previews)
					if (preview.canBeConvertedToBlockEntry())
						ingredient.add(preview.getBlockIngredient(previews.context));
					
				if (ingredient.isEmpty())
					return null;
				return ingredient;
			}
			
		});
		registerType(ColorIngredient.class, new IngredientOverflowHandler<ColorIngredient>() {
			
			@Override
			public List<ItemStack> handleOverflow(ColorIngredient overflow) throws NotEnoughSpaceException {
				throw new NotEnoughSpaceException(overflow);
			}
		}, new IngredientConvertionHandler<ColorIngredient>() {
			
			Field dyeColor = ReflectionHelper.findField(EnumDyeColor.class, "colorValue", "field_193351_w");
			
			@Override
			public ColorIngredient extract(ItemStack stack) {
				if (DyeUtils.isDye(stack)) {
					Optional<EnumDyeColor> optional = DyeUtils.colorFromStack(stack);
					if (!optional.isPresent())
						return null;
					
					try {
						ColorIngredient color = ColorIngredient.getColors(dyeColor.getInt(optional.get()));
						color.scale(SpecialServerConfig.dyeVolume);
						return color;
					} catch (IllegalArgumentException | IllegalAccessException e) {
						
					}
				}
				return null;
			}
			
			@Override
			public ColorIngredient extract(LittlePreviews previews) {
				ColorIngredient ingredient = new ColorIngredient();
				for (LittleTilePreview preview : previews)
					if (preview.canBeConvertedToBlockEntry())
						ingredient.add(ColorIngredient.getColors(previews.context, preview));
					
				if (ingredient.isEmpty())
					return null;
				return ingredient;
			}
			
		});
		registerType(StackIngredient.class, new IngredientOverflowHandler<StackIngredient>() {
			
			@Override
			public List<ItemStack> handleOverflow(StackIngredient overflow) throws NotEnoughSpaceException {
				List<ItemStack> stacks = new ArrayList<ItemStack>();
				for (StackIngredientEntry entry : overflow) {
					ItemStack stack = entry.stack.copy();
					stack.setCount(entry.count);
					stacks.add(stack);
				}
				return stacks;
			}
		}, null);
	}
	
	@Override
	public abstract T copy();
	
	public abstract void print(List<String> lines, List<ItemStack> stacks);
	
	@Override
	public abstract T add(T ingredient);
	
	@Override
	public abstract T sub(T ingredient);
	
	public abstract void scale(int count);
	
	public abstract int getMinimumCount(T other, int availableCount);
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.getClass() == obj.getClass();
	}
	
	public static abstract class IngredientOverflowHandler<T extends LittleIngredient> {
		
		public abstract List<ItemStack> handleOverflow(T overflow) throws NotEnoughSpaceException;
		
	}
	
	public static abstract class IngredientConvertionHandler<T extends LittleIngredient> {
		
		public abstract T extract(ItemStack stack);
		
		public abstract T extract(LittlePreviews previews);
		
	}
	
}
