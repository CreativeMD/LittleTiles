package com.creativemd.littletiles.client.gui.handler;

import java.util.UUID;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleGuiHandler extends CustomGuiHandler {
	
	public static void openGui(String id, NBTTagCompound nbt, EntityPlayer player, LittleTile tile) {
		new LittleTileIdentifierAbsolute(tile).writeToNBT(nbt);
		if (tile.te.getWorld() instanceof CreativeWorld)
			nbt.setString("uuid", ((CreativeWorld) tile.te.getWorld()).parent.getUniqueID().toString());
		GuiHandler.openGui(id, nbt, player);
	}
	
	public World getWorld(NBTTagCompound nbt, EntityPlayer player) {
		if (nbt.hasKey("uuid")) {
			EntityAnimation animation = LittleDoorHandler.getHandler(player.world.isRemote).findDoor(UUID.fromString(nbt.getString("uuid")));
			if (animation != null)
				return animation.fakeWorld;
			throw new RuntimeException("Could not find world " + nbt.getString("uuid"));
		}
		return player.world;
	}
	
	public abstract SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleTile tile);
	
	@Override
	public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
		try {
			return getContainer(player, nbt, LittleAction.getTile(getWorld(nbt, player), new LittleTileIdentifierAbsolute(nbt)));
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
			return getGui(player, nbt, LittleAction.getTile(getWorld(nbt, player), new LittleTileIdentifierAbsolute(nbt)));
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
