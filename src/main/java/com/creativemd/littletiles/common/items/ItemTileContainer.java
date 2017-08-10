package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fusesource.jansi.Ansi.Color;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.container.SubContainerTileContainer;
import com.creativemd.littletiles.common.gui.SubGuiTileContainer;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTileContainer extends Item implements IGuiCreator{
	
	public static int colorUnitMaximum = 1000000;
	public static int inventoryWidth = 6;
	public static int inventoryHeight = 4;
	public static int inventorySize = inventoryWidth*inventoryHeight;
	public static int maxStackSize = 64;
	public static int maxStackSizeOfTiles = maxStackSize*LittleTile.maxTilesPerBlock;
	
	public ItemTileContainer()
	{
		setCreativeTab(LittleTiles.littleTab);
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		
	}
	
	public static void saveInventory(ItemStack stack, List<BlockIngredient> inventory)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		NBTTagList list = new NBTTagList();
		int i = 0;
		for (BlockIngredient ingredient : inventory) {
			if(ingredient.block instanceof BlockAir)
				continue;
			if(i >= inventorySize)
				break;
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("block", Block.REGISTRY.getNameForObject(ingredient.block).toString());
			nbt.setInteger("meta", ingredient.meta);
			nbt.setDouble("volume", ingredient.value);
			list.appendTag(nbt);
			i++;
		}
		
		stack.getTagCompound().setTag("inv", list);
	}
	
	public static List<BlockIngredient> loadInventory(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		List<BlockIngredient> inventory = new ArrayList<>();
		
		NBTTagList list = stack.getTagCompound().getTagList("inv", 10);
		int size = Math.min(inventorySize, list.tagCount());
		for (int i = 0; i < size; i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			Block block = Block.getBlockFromName(nbt.getString("block"));
			if(block instanceof BlockAir)
				continue;
			inventory.add(new BlockIngredient(block, nbt.getInteger("meta"), nbt.getDouble("volume")));
		}
		return inventory;
	}
	
	public static BlockIngredients drainBlocks(ItemStack stack, BlockIngredients ingredients, boolean simulate)
	{
		List<BlockIngredient> inventory = loadInventory(stack);
		for (Iterator<BlockIngredient> iterator = ingredients.getIngredients().iterator(); iterator.hasNext();) {
			BlockIngredient ingredient = iterator.next();
			
			for (Iterator iterator2 = inventory.iterator(); iterator2.hasNext();) {
				BlockIngredient invIngredient = (BlockIngredient) iterator2.next();
				if(invIngredient.equals(ingredient))
				{
					double amount = Math.min(invIngredient.value, ingredient.value);
					ingredient.value -= amount;
					invIngredient.value -= amount;
					if(ingredient.value <= 0)
					{
						iterator.remove();
						break;
					}
					if(invIngredient.value <= 0)
						iterator2.remove();
				}
			}
		}
		
		if(!simulate)
			saveInventory(stack, inventory);
		
		if(ingredients.isEmpty())
			return null;
		return ingredients;		
	}
	
	public static BlockIngredients storeBlocks(ItemStack stack, BlockIngredients ingredients, boolean simulate)
	{
		ingredients = ItemTileContainer.storeBlocks(stack, ingredients, true, simulate);
		if(ingredients != null)
			ingredients = ItemTileContainer.storeBlocks(stack, ingredients, false, simulate);
		return ingredients;
	}
	
	public static BlockIngredients storeBlocks(ItemStack stack, BlockIngredients ingredients, boolean stackOnly, boolean simulate)
	{
		List<BlockIngredient> inventory = loadInventory(stack);
		
		if(stackOnly)
		{
			for (Iterator<BlockIngredient> iterator = ingredients.getIngredients().iterator(); iterator.hasNext();) {
				BlockIngredient ingredient = iterator.next();
				for (BlockIngredient equal : inventory) {
					if(equal.equals(ingredient))
					{
						double amount = Math.min(equal.value+ingredient.value, maxStackSize);
						ingredient.value -= amount-equal.value;
						equal.value = amount;
						if(ingredient.value <= 0)
						{
							iterator.remove();
							break;
						}
					}
				}
			}
		}else{
			for (Iterator<BlockIngredient> iterator = ingredients.getIngredients().iterator(); iterator.hasNext();) {
				BlockIngredient ingredient = iterator.next();
				if(inventory.size() >= inventorySize)
					break;
				
				double amount = Math.min(ingredient.value, maxStackSize);
				ingredient.value -= amount;
				
				if(ingredient.value <= 0)
					iterator.remove();
				
				inventory.add(ingredient.copy(amount));
			}
		}
		
		if(!simulate)
			saveInventory(stack, inventory);
		
		if(ingredients.isEmpty())
			return null;
		return ingredients;
	}
	
	public static ColorUnit loadColorUnit(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		return new ColorUnit(stack.getTagCompound().getInteger("black"), stack.getTagCompound().getInteger("red"), stack.getTagCompound().getInteger("green"), stack.getTagCompound().getInteger("blue"));
	}
	
	public static void saveColorUnit(ItemStack stack, ColorUnit unit)
	{
		stack.getTagCompound().setInteger("black", unit.BLACK);
		stack.getTagCompound().setInteger("red", unit.RED);
		stack.getTagCompound().setInteger("green", unit.GREEN);
		stack.getTagCompound().setInteger("blue", unit.BLUE);
	}
	
	public static ColorUnit storeColor(ItemStack stack, ColorUnit unit, boolean simulate)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		ColorUnit result = unit.copy();
		
		int maxBlack = Math.min(stack.getTagCompound().getInteger("black") + unit.BLACK, colorUnitMaximum);
		if(stack.getTagCompound().getInteger("black") != maxBlack)
			result.BLACK -= maxBlack - stack.getTagCompound().getInteger("black");
		
		int maxRed = Math.min(stack.getTagCompound().getInteger("red") + unit.RED, colorUnitMaximum);
		if(stack.getTagCompound().getInteger("red") != maxRed)
			result.RED -= maxRed - stack.getTagCompound().getInteger("red");
		
		int maxGreen = Math.min(stack.getTagCompound().getInteger("green") + unit.GREEN, colorUnitMaximum);
		if(stack.getTagCompound().getInteger("green") != maxGreen)
			result.GREEN -= maxGreen - stack.getTagCompound().getInteger("green");
		
		int maxBlue = Math.min(stack.getTagCompound().getInteger("blue") + unit.BLUE, colorUnitMaximum);
		if(stack.getTagCompound().getInteger("blue") != maxBlue)
			result.BLUE -= maxBlue - stack.getTagCompound().getInteger("blue");
		
		if(!simulate)
		{
			stack.getTagCompound().setInteger("black", maxBlack);
			stack.getTagCompound().setInteger("red", maxRed);
			stack.getTagCompound().setInteger("green", maxGreen);
			stack.getTagCompound().setInteger("blue", maxBlue);
		}
		
		if(result.BLACK == 0 && result.RED == 0 && result.GREEN == 0 && result.BLUE == 0)
			return null;
		
		return result;
	}
	
	public static ColorUnit drainColor(ItemStack stack, ColorUnit unit, boolean simulate)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		ColorUnit result = unit.copy();
		
		int drainBlack = Math.min(unit.BLACK, stack.getTagCompound().getInteger("black"));
		result.BLACK -= drainBlack;
		
		int drainRed = Math.min(unit.RED, stack.getTagCompound().getInteger("red"));
		result.RED -= drainRed;
		
		int drainGreen = Math.min(unit.GREEN, stack.getTagCompound().getInteger("green"));
		result.GREEN -= drainGreen;
		
		int drainBlue = Math.min(unit.BLUE, stack.getTagCompound().getInteger("blue"));
		result.BLUE -= drainBlue;
		
		if(!simulate)
		{
			stack.getTagCompound().setInteger("black", stack.getTagCompound().getInteger("black") - drainBlack);
			stack.getTagCompound().setInteger("red", stack.getTagCompound().getInteger("red") - drainRed);
			stack.getTagCompound().setInteger("green", stack.getTagCompound().getInteger("green") - drainGreen);
			stack.getTagCompound().setInteger("blue", stack.getTagCompound().getInteger("blue") - drainBlue);
		}
		
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiTileContainer(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerTileContainer(player, stack, player.inventory.currentItem);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		if(!world.isRemote)
			GuiHandler.openGuiItem(player, world);
		return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

}
