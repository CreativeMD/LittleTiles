package com.creativemd.littletiles.common.structure.type;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.type.UUIDSupplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface ILittleDoor {
	
	public void openDoor(World world, @Nullable EntityPlayer player, UUIDSupplier uuid);
	
}
