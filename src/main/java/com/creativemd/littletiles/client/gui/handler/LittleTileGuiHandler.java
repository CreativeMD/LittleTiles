package com.creativemd.littletiles.client.gui.handler;

import java.util.UUID;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.location.TileLocation;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.parent.ParentTileList;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleTileGuiHandler extends CustomGuiHandler {
	
	public static void openGui(String id, NBTTagCompound nbt, EntityPlayer player, ParentTileList parent, LittleTile tile) {
		nbt.setTag("location", new TileLocation(parent, tile).write());
		if (parent.getWorld() instanceof CreativeWorld)
			nbt.setString("uuid", ((CreativeWorld) parent.getWorld()).parent.getUniqueID().toString());
		GuiHandler.openGui(id, nbt, player);
	}
	
	public World getWorld(NBTTagCompound nbt, EntityPlayer player) {
		if (nbt.hasKey("uuid")) {
			EntityAnimation animation = WorldAnimationHandler.findAnimation(player.world.isRemote, UUID.fromString(nbt.getString("uuid")));
			if (animation != null)
				return animation.fakeWorld;
			throw new RuntimeException("Could not find world " + nbt.getString("uuid"));
		}
		return player.world;
	}
	
	public abstract SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, IParentTileList parent, LittleTile tile);
	
	@Override
	public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
		try {
			Pair<IParentTileList, LittleTile> pair = new TileLocation(nbt.getCompoundTag("location")).find(getWorld(nbt, player));
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
			Pair<IParentTileList, LittleTile> pair = new TileLocation(nbt.getCompoundTag("location")).find(getWorld(nbt, player));
			return getGui(player, nbt, pair.key, pair.value);
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
