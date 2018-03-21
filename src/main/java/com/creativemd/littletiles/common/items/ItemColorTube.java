package com.creativemd.littletiles.common.items;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.LittleActionColorBoxes;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.container.SubContainerColorTube;
import com.creativemd.littletiles.common.gui.SubGuiColorTube;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket.VanillaBlockAction;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.geo.SelectShape;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemColorTube extends Item implements IGuiCreator, ISpecialBlockSelector{

	public ItemColorTube()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	public static int getColor(ItemStack stack)
	{
		if(stack == null)
			return ColorUtils.WHITE;
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		if(!stack.getTagCompound().hasKey("color"))
			setColor(stack, ColorUtils.WHITE);
		return stack.getTagCompound().getInteger("color");
	}
	
	public static void setColor(ItemStack stack, int color)
	{
		if(stack == null)
			return ;
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("color", color);
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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add("click: dyes a tile");
		tooltip.add("shift+click: copies tile's color");
		SelectShape shape = getShape(stack);
		tooltip.add("shape: " + (shape == null ? "tile" : shape.key));
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
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiColorTube(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerColorTube(player, stack);
	}
	
	public static SelectShape getShape(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		String shape = stack.getTagCompound().getString("shape");
		if(shape.equals("tile") || shape.equals(""))
			return SelectShape.tileShape;
		return SelectShape.getShape(shape);
	}

	@Override
	public void onDeselect(World world, ItemStack stack, EntityPlayer player) {
		SelectShape shape = getShape(stack);
		if(shape != null)
			shape.deselect(player, stack.getTagCompound(), getContext(stack));
	}

	@Override
	public boolean hasCustomBox(World world, ItemStack stack, EntityPlayer player, IBlockState state,
			RayTraceResult result, LittleTilePos absoluteHit) {
		return getShape(stack) != null;
	}

	@Override
	public LittleBoxes getBox(World world, ItemStack stack, EntityPlayer player, RayTraceResult result,
			LittleTilePos absoluteHit) {
		SelectShape shape = getShape(stack);
		
		return shape.getHighlightBoxes(player, stack.getTagCompound(), result, getContext(stack));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean onClickBlock(World world, ItemStack stack, EntityPlayer player, RayTraceResult result,
			LittleTilePos absoluteHit) {
		SelectShape shape = getShape(stack);
		if(LittleAction.isUsingSecondMode(player))
		{
			if(!world.isRemote)
				return true;
			TileEntity tileEntity = world.getTileEntity(result.getBlockPos());
			if(tileEntity instanceof TileEntityLittleTiles)
				PacketHandler.sendPacketToServer(new LittleBlockPacket(result.getBlockPos(), player, BlockPacketAction.COLOR_TUBE, new NBTTagCompound()));
			else
				PacketHandler.sendPacketToServer(new LittleVanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.COLOR_TUBE));
		}else if(shape.leftClick(player, stack.getTagCompound(), result, getContext(stack))){
			new LittleActionColorBoxes(shape.getBoxes(player, stack.getTagCompound(), result, getContext(stack)), getColor(stack), false).execute();
		}
		return true;
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
	
	@Override
	public LittleGridContext getContext(ItemStack stack) {
		return ItemMultiTiles.currentContext;
	}
}
