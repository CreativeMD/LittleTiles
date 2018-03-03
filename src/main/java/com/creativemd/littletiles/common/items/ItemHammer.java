package com.creativemd.littletiles.common.items;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.gui.SubGuiGrabber;
import com.creativemd.littletiles.common.gui.SubGuiHammer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.geo.SelectShape;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemHammer extends Item implements ISpecialBlockSelector, IGuiCreator {
	
	public ItemHammer()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add("can be used to chisel blocks");
		SelectShape shape = getShape(stack);
		tooltip.add("mode: " + shape.key);
		shape.addExtraInformation(worldIn, stack.getTagCompound(), tooltip);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		if(hand == EnumHand.OFF_HAND)
			return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand)); 
		if(!world.isRemote)
			GuiHandler.openGuiItem(player, world);
        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
	
	@Override
	public List<LittleTileBox> getBox(World world, ItemStack stack, EntityPlayer player, RayTraceResult result,
			LittleTileVec absoluteHit) {
		SelectShape shape = getShape(stack);
		
		return shape.getHighlightBoxes(player, stack.getTagCompound(), result);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean onClickBlock(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleTileVec absoluteHit) {
		SelectShape shape = getShape(stack);
		if(shape.leftClick(player, stack.getTagCompound(), result))
			new LittleActionDestroyBoxes(shape.getBoxes(player, stack.getTagCompound(), result)).execute();
		return true;
	}
	
	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player)
    {
        return false;
    }
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
    {
        return 0F;
    }
	
	@Override
	public void onDeselect(World world, ItemStack stack, EntityPlayer player) {
		getShape(stack).deselect(player, stack.getTagCompound());
	}
	
	@Override
	public boolean hasCustomBox(World world, ItemStack stack, EntityPlayer player, IBlockState state, RayTraceResult result,
			LittleTileVec absoluteHit) {
		return LittleAction.isBlockValid(state.getBlock()) || world.getTileEntity(result.getBlockPos()) instanceof TileEntityLittleTiles;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiHammer(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos,
			IBlockState state) {
		return new SubContainerHammer(player, stack);
	}
	
	@Override
	public void rotateLittlePreview(ItemStack stack, Rotation rotation) {
		SelectShape shape = getShape(stack);
		if(shape != null)
			shape.rotate(rotation, stack.getTagCompound());
	}

	@Override
	public void flipLittlePreview(ItemStack stack, Axis axis) {
		SelectShape shape = getShape(stack);
		if(shape != null)
			shape.flip(axis, stack.getTagCompound());
	}
	
	public static SelectShape getShape(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		return SelectShape.getShape(stack.getTagCompound().getString("shape"));
	}
}
