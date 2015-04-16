package com.creativemd.littletiles.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LittleBlockPacket extends CreativeCorePacket{
	
	public int x;
	public int y;
	public int z;
	public Vec3 pos;
	public Vec3 look;
	public int action;
	
	public LittleBlockPacket()
	{
		
	}
	
	public LittleBlockPacket(int x, int y, int z, EntityPlayer player, int action)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.action = action;
		this.pos = player.getPosition(1);
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3 look = player.getLook(1.0F);
		this.look = pos.addVector(look.xCoord * d0, look.yCoord * d0, look.zCoord * d0);
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		writeVec3(pos, buf);
		writeVec3(look, buf);
		buf.writeInt(action);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		pos = readVec3(buf);
		look = readVec3(buf);		
		action = buf.readInt();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			MovingObjectPosition moving = ((TileEntityLittleTiles) tileEntity).getMoving(pos, look, true);
			LittleTile tile = ((TileEntityLittleTiles) tileEntity).loadedTile;
			if(tile != null)
			{
				switch(action)
				{
				case 0: //Activated
					tile.block.onBlockActivated(player.worldObj, x, y, z, player, moving.sideHit, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord);
					break;
				}
			}
		}
	}
	
}
