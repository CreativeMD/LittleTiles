package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;
import java.util.Iterator;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.core.CreativeCoreClient;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.blocks.BlockTile.TEResult;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.TileList;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleBlockPacket extends CreativeCorePacket{
	
	public BlockPos blockPos;
	public Vec3d pos;
	public Vec3d look;
	public int action;
	public NBTTagCompound nbt;
	
	public LittleBlockPacket()
	{
		
	}
	
	public LittleBlockPacket(BlockPos blockPos, EntityPlayer player, int action)
	{
		this(blockPos, player, action, new NBTTagCompound());
	}
	
	public LittleBlockPacket(BlockPos blockPos, EntityPlayer player, int action, NBTTagCompound nbt)
	{
		this.blockPos = blockPos;
		this.action = action;
		this.pos = player.getPositionEyes(TickUtils.getPartialTickTime());
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(TickUtils.getPartialTickTime());
		this.look = pos.addVector(look.xCoord * d0, look.yCoord * d0, look.zCoord * d0);
		this.nbt = nbt;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, blockPos);
		writeVec3(pos, buf);
		writeVec3(look, buf);
		buf.writeInt(action);
		writeNBT(buf, nbt);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		blockPos = readPos(buf);
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
		TileEntity tileEntity = player.worldObj.getTileEntity(blockPos);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
			LittleTile tile = te.getFocusedTile(pos, look);
			if(tile != null)
			{
				switch(action)
				{
				case 0: //Activated
					RayTraceResult moving = te.getMoving(pos, look);
					if(tile.onBlockActivated(player.worldObj, blockPos, player.worldObj.getBlockState(blockPos), player, EnumHand.MAIN_HAND, player.getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord))
						BlockTile.cancelNext = true;
					break;
				case 1: //Destory tile
					LittleTileBox box = null;
    				
    				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
    				if(stack != null && stack.getItem() instanceof ISpecialBlockSelector)
    				{
    					box = ((ISpecialBlockSelector) stack.getItem()).getBox(te, tile, te.getPos(), player);
    					if(box != null)
    					{
    						te.removeBoxFromTile(tile, box);
    						if(!player.capabilities.isCreativeMode)
    						{
    							tile.boundingBoxes.add(new LittleTileBox(0,0,0,1,1,1));
    							WorldUtils.dropItem(player, tile.getDrops());
    						}
    					}
    				}
    				
    				if(box == null)
    				{
						tile.destroy();
						if(!player.capabilities.isCreativeMode)
							WorldUtils.dropItem(player.worldObj, tile.getDrops(), blockPos);
					}
    				
    				for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
    					LittleTile tileNeighbor = (LittleTile) iterator.next();
						tileNeighbor.onNeighborChangeInside();
					}
    				
    				te.updateBlock();
					break;
				case 2: //Saw
					int side = nbt.getInteger("side");
					EnumFacing direction = EnumFacing.getFront(side);
					if(tile.canSawResizeTile(direction, player))
					{
						box = null;
						if(player.isSneaking())
							box = tile.boundingBoxes.get(0).shrink(direction);
						else
							box = tile.boundingBoxes.get(0).expand(direction);
						
						if(box.isBoxInsideBlock() && box.isValidBox() && te.isSpaceForLittleTile(box.getBox(), tile))
						{
							float ammount = tile.boundingBoxes.get(0).getSize().getPercentVolume()-box.getSize().getPercentVolume();
							boolean success = false;
							if(player.isSneaking())
							{
								if(ItemTileContainer.addBlock(player, ((LittleTileBlock)tile).block, ((LittleTileBlock)tile).meta, ammount))
									success = true;
							}else{
								if(ItemTileContainer.drainBlock(player, ((LittleTileBlock)tile).block, ((LittleTileBlock)tile).meta, -ammount))
									success = true;
							}
							
							if(player.capabilities.isCreativeMode || success)
							{
								tile.boundingBoxes.set(0, box);
								tile.updateCorner();
								te.updateBlock();
							}
						}
					}
					break;
				case 3: //COLOR TUBE set Color
					if(tile.getClass() == LittleTileBlock.class || tile instanceof LittleTileBlockColored)
					{
						int color = nbt.getInteger("color");
						
						int index = te.getTiles().indexOf(tile);
						if(player.isSneaking())
						{
							color = ColorUtils.WHITE;
							if(tile instanceof LittleTileBlockColored)
								color = ((LittleTileBlockColored) tile).color;
							ItemColorTube.setColor(player.getHeldItemMainhand(), color);
						}else{
							
							LittleTile newTile = LittleTileBlockColored.setColor((LittleTileBlock) tile, color);
							
							if(newTile != null)
								te.getTiles().set(index, newTile);
							te.updateBlock();
						}
					}
					break;
				case 4: //RUBBER MALLET
					ArrayList<LittleTile> newTiles = new ArrayList<>();
					if((tile.getClass() == LittleTileBlock.class || tile instanceof LittleTileBlockColored)  && tile.structure == null)
					{
						for (int j = 0; j < tile.boundingBoxes.size(); j++) {
							box = tile.boundingBoxes.get(j);
							for (int littleX = box.minX; littleX < box.maxX; littleX++) {
								for (int littleY = box.minY; littleY < box.maxY; littleY++) {
									for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
										tile = tile.copy();
										tile.boundingBoxes.clear();
										tile.boundingBoxes.add(new LittleTileBox(littleX, littleY, littleZ, littleX+1, littleY+1, littleZ+1));
										tile.updateCorner();
										tile.te = te;
										newTiles.add(tile);
									}
								}
							}
						}
						
						if(LittleTiles.maxNewTiles >= newTiles.size() - 1)
						{
							te.removeTile(tile);
							te.addTiles(newTiles);
							te.updateBlock();
						}else{
							player.addChatComponentMessage(new TextComponentTranslation("Too much new tiles! Limit=" + LittleTiles.maxNewTiles));
						}
					}
					break;
				}
			}
		}
	}
	
}
