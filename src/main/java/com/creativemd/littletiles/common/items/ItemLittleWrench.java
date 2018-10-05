package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.BlockTile.TEResult;
import com.creativemd.littletiles.common.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleWrench extends Item {
	
	public ItemLittleWrench() {
		setCreativeTab(LittleTiles.littleTab);
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityLittleTiles) {
			
			if (world.isRemote) {
				TEResult result = BlockTile.loadTeAndTile(world, pos, player);
				
				if (result.isComplete() && result.tile.isChildOfStructure())
					LittleGuiHandler.openGui("structureoverview", new NBTTagCompound(), player, result.tile);
				else {
					player.sendStatusMessage(new TextComponentString("grid:" + ((TileEntityLittleTiles) tileEntity).getContext()), true);
					((TileEntityLittleTiles) tileEntity).combineTiles();
					((TileEntityLittleTiles) tileEntity).convertBlockToVanilla();
				}
				
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}
}
