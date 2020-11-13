package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor.StillInMotionException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleActivateDoorPacket extends CreativeCorePacket {
	
	public StructureLocation location;
	public UUID uuid;
	
	public LittleActivateDoorPacket(StructureLocation location, UUID uuid) {
		this.location = location;
		this.uuid = uuid;
	}
	
	public LittleActivateDoorPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeStructureLocation(location, buf);
		writeString(buf, uuid.toString());
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		location = LittleAction.readStructureLocation(buf);
		uuid = UUID.fromString(readString(buf));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) { // Note it does not take care of synchronization, just sends an activation to the client, only used for already animated doors		
		try {
			LittleStructure structure = location.find(player.world);
			if (structure instanceof LittleDoor) {
				LittleDoor door = (LittleDoor) structure;
				door.activate(null, uuid, false);
			}
			
		} catch (StillInMotionException e) {
			PacketHandler.sendPacketToServer(new LittleEntityFixControllerPacket(uuid, new NBTTagCompound()));
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		LittleTile tile;
		EntityAnimation animation = null;
		
		try {
			LittleStructure structure = location.find(player.world);
			if (structure instanceof LittleDoor) {
				LittleDoor door = (LittleDoor) structure;
				World world = door.getWorld();
				if (world instanceof SubWorld)
					animation = (EntityAnimation) ((SubWorld) world).parent;
				
				door.activate(player, uuid, true);
			}
		} catch (LittleActionException e) {
			if (animation != null)
				PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), false), (EntityPlayerMP) player);
			else
				PacketHandler.sendPacketToPlayer(new LittleResetAnimationPacket(uuid), (EntityPlayerMP) player);
			
		}
	}
	
}
