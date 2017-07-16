package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.container.SubContainerUtilityKnife;
import com.creativemd.littletiles.common.gui.SubGuiUtilityKnife;
import com.creativemd.littletiles.common.items.geo.SelectShape;
import com.creativemd.littletiles.common.packet.LittleSelectShapePacket;
import com.creativemd.littletiles.common.packet.LittleSelectShapePacket.LittleSelectShapeAction;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUtilityKnife extends Item implements ISpecialBlockSelector, IGuiCreator {
	
	public ItemUtilityKnife()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("can be used to chisel blocks");
		SelectShape shape = getShape(stack);
		list.add("mode: " + shape.key);
		shape.addExtraInformation(player, stack.getTagCompound(), list);
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
			PacketHandler.sendPacketToServer(new LittleSelectShapePacket(shape.getBoxes(player, stack.getTagCompound(), result), LittleSelectShapeAction.UTILITY_KNIFE, new NBTTagCompound()));
		return true;
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
	public void onDeselect(World world, ItemStack stack, EntityPlayer player) {
		getShape(stack).deselect(player, stack.getTagCompound());
	}
	
	@Override
	public boolean hasCustomBox(World world, ItemStack stack, EntityPlayer player, IBlockState state, RayTraceResult result,
			LittleTileVec absoluteHit) {
		return SubContainerHammer.isBlockValid(state.getBlock()) || world.getTileEntity(result.getBlockPos()) instanceof TileEntityLittleTiles;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiUtilityKnife(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos,
			IBlockState state) {
		return new SubContainerUtilityKnife(player, stack);
	}
	
	public static SelectShape getShape(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		return SelectShape.getShape(stack.getTagCompound().getString("shape"));
	}
}
