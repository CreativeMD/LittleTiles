package com.creativemd.littletiles.common.gui.handler;

import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.littletiles.common.gui.SubGuiStorage;
import com.creativemd.littletiles.common.structure.LittleStorage;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleGuiHandler extends CustomGuiHandler {
	
	public static void openGui(String id, NBTTagCompound nbt, EntityPlayer player, LittleTile tile)
	{
		nbt.setInteger("x", tile.te.getPos().getX());
		nbt.setInteger("y", tile.te.getPos().getY());
		nbt.setInteger("z", tile.te.getPos().getZ());
		
		nbt.setInteger("tX", tile.cornerVec.x);
		nbt.setInteger("tY", tile.cornerVec.y);
		nbt.setInteger("tZ", tile.cornerVec.z);
		
		GuiHandler.openGui(id, nbt, player);
	}
	
	public abstract SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleTile tile);

	@Override
	public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
		BlockPos pos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
		TileEntity te = player.getEntityWorld().getTileEntity(pos);
		if(te instanceof TileEntityLittleTiles)
		{
			LittleTile tile = ((TileEntityLittleTiles) te).getTile(nbt.getInteger("tX"), nbt.getInteger("tY"), nbt.getInteger("tZ"));
			if(tile != null)
				return getContainer(player, nbt, tile);
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public abstract SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleTile tile);

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
		BlockPos pos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
		TileEntity te = player.getEntityWorld().getTileEntity(pos);
		if(te instanceof TileEntityLittleTiles)
		{
			LittleTile tile = ((TileEntityLittleTiles) te).getTile(nbt.getInteger("tX"), nbt.getInteger("tY"), nbt.getInteger("tZ"));
			if(tile != null)
				return getGui(player, nbt, tile);
		}
		return null;
	}

}
