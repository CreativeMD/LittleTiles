package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.container.SubContainerColorTube;
import com.creativemd.littletiles.common.gui.SubGuiColorTube;
import com.creativemd.littletiles.common.items.geo.SelectShape;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleSelectShapePacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleSelectShapePacket.LittleSelectShapeAction;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IItemColor;
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
	public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        return 0F;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("click: dyes a tile");
		list.add("shift+click: copies tile's color");
		SelectShape shape = getShape(stack);
		list.add("shape: " + (shape == null ? "tile" : shape.key));
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
			return null;
		return SelectShape.getShape(shape);
	}

	@Override
	public void onDeselect(World world, ItemStack stack, EntityPlayer player) {
		SelectShape shape = getShape(stack);
		if(shape != null)
			shape.deselect(player, stack.getTagCompound());
	}

	@Override
	public boolean hasCustomBox(World world, ItemStack stack, EntityPlayer player, IBlockState state,
			RayTraceResult result, LittleTileVec absoluteHit) {
		return getShape(stack) != null;
	}

	@Override
	public List<LittleTileBox> getBox(World world, ItemStack stack, EntityPlayer player, RayTraceResult result,
			LittleTileVec absoluteHit) {
		SelectShape shape = getShape(stack);
		
		return shape.getHighlightBoxes(player, stack.getTagCompound(), result);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean onClickBlock(World world, ItemStack stack, EntityPlayer player, RayTraceResult result,
			LittleTileVec absoluteHit) {
		SelectShape shape = getShape(stack);
		if(shape == null || player.isSneaking())
		{
			TileEntity tileEntity = world.getTileEntity(result.getBlockPos());
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				if(world.isRemote)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setInteger("color", getColor(stack));
					PacketHandler.sendPacketToServer(new LittleBlockPacket(result.getBlockPos(), player, BlockPacketAction.COLOR_TUBE, nbt));
				}
				return true;
			}
		}else if(shape.leftClick(player, stack.getTagCompound(), result)){
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("color", getColor(stack));
			PacketHandler.sendPacketToServer(new LittleSelectShapePacket(shape.getBoxes(player, stack.getTagCompound(), result), LittleSelectShapeAction.COLOR_TUBE, nbt));
		}
		return true;
	}
}
