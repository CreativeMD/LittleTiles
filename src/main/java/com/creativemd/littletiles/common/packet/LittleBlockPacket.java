package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;

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
	public NBTTagCompound nbt;
	
	public LittleBlockPacket()
	{
		
	}
	
	public LittleBlockPacket(int x, int y, int z, EntityPlayer player, int action)
	{
		this(x, y, z, player, action, new NBTTagCompound());
	}
	
	public LittleBlockPacket(int x, int y, int z, EntityPlayer player, int action, NBTTagCompound nbt)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.action = action;
		this.pos = player.getPosition(1);
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3 look = player.getLook(1.0F);
		this.look = pos.addVector(look.xCoord * d0, look.yCoord * d0, look.zCoord * d0);
		this.nbt = nbt;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		writeVec3(pos, buf);
		writeVec3(look, buf);
		buf.writeInt(action);
		writeNBT(buf, nbt);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		pos = readVec3(buf);
		look = readVec3(buf);		
		action = buf.readInt();
		nbt = readNBT(buf);
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
			TileEntityLittleTiles littleEntity = (TileEntityLittleTiles) tileEntity;
			MovingObjectPosition moving = littleEntity.getMoving(pos, look, true);
			LittleTile tile = littleEntity.loadedTile;
			if(tile != null)
			{
				switch(action)
				{
				case 0: //Activated
					if(tile.onBlockActivated(player.worldObj, x, y, z, player, moving.sideHit, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord))
						BlockTile.cancelNext = true;
					break;
				case 1: //Destory tile
					tile.destroy();
					//littleEntity.removeTile(tile);
					if(!player.capabilities.isCreativeMode)
						WorldUtils.dropItem(player.worldObj, tile.getDrops(), x, y, z);
					for (int i = 0; i < littleEntity.tiles.size(); i++) {
						littleEntity.tiles.get(i).onNeighborChangeInside();
					}
					littleEntity.update();
					break;
				case 2:
					try{
						int side = nbt.getInteger("side");
						ForgeDirection direction = ForgeDirection.getOrientation(side);
						TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
						if(te.updateLoadedTileServer(pos, look) && te.loadedTile.boundingBoxes.size() == 1 && !te.loadedTile.isStructureBlock && !(te.loadedTile instanceof LittleTileTileEntity) && te.loadedTile instanceof LittleTileBlock)
						{
							LittleTileBox box = null;
							if(player.isSneaking())
								box = te.loadedTile.boundingBoxes.get(0).shrink(direction);
							else
								box = te.loadedTile.boundingBoxes.get(0).expand(direction);
							
							if(box.isBoxInsideBlock() && box.isValidBox() && te.isSpaceForLittleTile(box.getBox(), te.loadedTile))
							{
								float ammount = te.loadedTile.boundingBoxes.get(0).getSize().getPercentVolume()-box.getSize().getPercentVolume();
								boolean success = false;
								if(player.isSneaking())
								{
									if(ItemTileContainer.addBlock(player, ((LittleTileBlock)te.loadedTile).block, ((LittleTileBlock)te.loadedTile).meta, ammount))
										success = true;
								}else{
									if(ItemTileContainer.drainBlock(player, ((LittleTileBlock)te.loadedTile).block, ((LittleTileBlock)te.loadedTile).meta, -ammount))
										success = true;
								}
								
								if(player.capabilities.isCreativeMode || success)
								{
									te.loadedTile.boundingBoxes.set(0, box);
									te.loadedTile.updateCorner();
									te.update();
								}
							}
						}
					
					}catch(Exception e){
						System.out.println("Failed to use saw!");
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}
	
}
