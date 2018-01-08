package com.creativemd.littletiles.common.gui.handler;

import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.gui.SubGuiStorage;
import com.creativemd.littletiles.common.structure.LittleStorage;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileAbsoluteCoord;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleGuiHandler extends CustomGuiHandler {
	
	public static void openGui(String id, NBTTagCompound nbt, EntityPlayer player, LittleTile tile)
	{
		new LittleTileAbsoluteCoord(tile).writeToNBT(nbt);
		
		GuiHandler.openGui(id, nbt, player);
	}
	
	public abstract SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleTile tile);

	@Override
	public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
		try {
			return getContainer(player, nbt, LittleAction.getTile(player.world, new LittleTileAbsoluteCoord(nbt)));
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public abstract SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleTile tile);

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
		try {
			return getGui(player, nbt, LittleAction.getTile(player.world, new LittleTileAbsoluteCoord(nbt)));
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		return null;
	}

}
