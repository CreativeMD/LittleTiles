package com.creativemd.littletiles.client.gui.handler;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.location.TileLocation;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleTileGuiHandler extends CustomGuiHandler {
	
	public static void openGui(String id, NBTTagCompound nbt, EntityPlayer player, IParentTileList parent, LittleTile tile) {
		nbt.setTag("location", new TileLocation(parent, tile).write());
		GuiHandler.openGui(id, nbt, player);
	}
	
	public abstract SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, IParentTileList parent, LittleTile tile);
	
	@Override
	public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
		try {
			Pair<IParentTileList, LittleTile> pair = new TileLocation(nbt.getCompoundTag("location")).find(player.world);
			return getContainer(player, nbt, pair.key, pair.value);
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public abstract SubGui getGui(EntityPlayer player, NBTTagCompound nbt, IParentTileList parent, LittleTile tile);
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
		try {
			Pair<IParentTileList, LittleTile> pair = new TileLocation(nbt.getCompoundTag("location")).find(player.world);
			return getGui(player, nbt, pair.key, pair.value);
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
