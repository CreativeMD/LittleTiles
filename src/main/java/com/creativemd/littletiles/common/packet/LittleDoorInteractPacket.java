package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.littletiles.common.structure.LittleDoor;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleDoorInteractPacket extends CreativeCorePacket {
	
	
	public BlockPos blockPos;
	public Rotation direction;
	public boolean inverse;
	public Vec3d pos;
	public Vec3d look;
	
	public UUID uuid;
	
	public LittleDoorInteractPacket() {
		
	}
	
	public LittleDoorInteractPacket(BlockPos blockPos, EntityPlayer player, Rotation rotation, boolean inverse, UUID uuid)
	{
		this.blockPos = blockPos;
		this.pos = player.getPositionEyes(TickUtils.getPartialTickTime());
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(TickUtils.getPartialTickTime());
		this.look = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
		this.direction = rotation;
		this.inverse = inverse;
		this.uuid = uuid;
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, blockPos);
		writeVec3d(pos, buf);
		writeVec3d(look, buf);
		buf.writeInt(direction.ordinal());
		buf.writeBoolean(inverse);
		writeString(buf, uuid.toString());
	}

	@Override
	public void readBytes(ByteBuf buf) {
		blockPos = readPos(buf);
		pos = readVec3d(buf);
		look = readVec3d(buf);
		direction = Rotation.values()[buf.readInt()];
		inverse = buf.readBoolean();
		uuid = UUID.fromString(readString(buf));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		TileEntity tileEntity = player.world.getTileEntity(blockPos);
		World world = player.world;
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
			LittleTile tile = te.getFocusedTile(pos, look);
			if(tile != null && tile.isLoaded() && tile.structure instanceof LittleDoor)
			{
				((LittleDoor) tile.structure).interactWithDoor(world, player, direction, inverse, uuid);
				//System.out.println("Open Door");
			}//else
				//System.out.println("No door found!");
		}
	}
	
	
	
}
