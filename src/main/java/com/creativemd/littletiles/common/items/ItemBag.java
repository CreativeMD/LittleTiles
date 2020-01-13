package com.creativemd.littletiles.common.items;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.SubGuiBag;
import com.creativemd.littletiles.common.api.ILittleInventory;
import com.creativemd.littletiles.common.container.SubContainerBag;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredientEntry;
import com.creativemd.littletiles.common.utils.ingredients.ColorIngredient;
import com.creativemd.littletiles.common.utils.ingredients.IngredientUtils;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBag extends Item implements IGuiCreator, ILittleInventory {
	
	public static int colorUnitMaximum = 10000000;
	public static int inventoryWidth = 6;
	public static int inventoryHeight = 4;
	public static int inventorySize = inventoryWidth * inventoryHeight;
	public static int maxStackSize = 64;
	public static int maxStackSizeOfTiles = maxStackSize * LittleGridContext.get().maxTilesPerBlock;
	
	public ItemBag() {
		setCreativeTab(LittleTiles.littleTab);
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiBag(stack);
	}
	
	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerBag(player, stack, player.inventory.currentItem);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote)
			GuiHandler.openGuiItem(player, world);
		return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}
	
	@Override
	public LittleIngredients getInventory(ItemStack stack) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		LittleIngredients ingredients = new LittleIngredients();
		BlockIngredient blocks = new BlockIngredient().setLimits(inventorySize, maxStackSize);
		
		NBTTagList list = stack.getTagCompound().getTagList("inv", 10);
		int size = Math.min(inventorySize, list.tagCount());
		for (int i = 0; i < size; i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			BlockIngredientEntry ingredient = IngredientUtils.loadBlockIngredient(nbt);
			if (ingredient != null && ingredient.value >= LittleGridContext.getMax().pixelVolume)
				blocks.add(ingredient);
		}
		ingredients.set(blocks.getClass(), blocks);
		
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		ColorIngredient color = new ColorIngredient(stack.getTagCompound().getInteger("black"), stack.getTagCompound().getInteger("cyan"), stack.getTagCompound().getInteger("magenta"), stack.getTagCompound().getInteger("yellow"));
		color.setLimit(colorUnitMaximum);
		ingredients.set(color.getClass(), color);
		return ingredients;
	}
	
	@Override
	public void setInventory(ItemStack stack, LittleIngredients ingredients) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		NBTTagList list = new NBTTagList();
		int i = 0;
		for (BlockIngredientEntry ingredient : ingredients.get(BlockIngredient.class).getContent()) {
			if (ingredient.block instanceof BlockAir && ingredient.value < LittleGridContext.getMax().pixelVolume)
				continue;
			if (i >= inventorySize)
				break;
			list.appendTag(ingredient.writeToNBT(new NBTTagCompound()));
			i++;
		}
		
		stack.getTagCompound().setTag("inv", list);
		
		ColorIngredient color = ingredients.get(ColorIngredient.class);
		stack.getTagCompound().setInteger("black", color.black);
		stack.getTagCompound().setInteger("cyan", color.cyan);
		stack.getTagCompound().setInteger("magenta", color.magenta);
		stack.getTagCompound().setInteger("yellow", color.yellow);
	}
	
}
