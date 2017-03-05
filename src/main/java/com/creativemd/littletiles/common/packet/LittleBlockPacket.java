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
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemRubberMallet;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileCoord;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.TileList;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
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
		writeVec3d(pos, buf);
		writeVec3d(look, buf);
		buf.writeInt(action);
		writeNBT(buf, nbt);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		blockPos = readPos(buf);
		pos = readVec3d(buf);
		look = readVec3d(buf);		
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
		World world = player.worldObj;
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
			LittleTile tile = te.getFocusedTile(pos, look);
			if(tile != null)
			{
				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
				switch(action)
				{
				case 0: //Activated
					RayTraceResult moving = te.getMoving(pos, look);
					if(tile.onBlockActivated(player.worldObj, blockPos, player.worldObj.getBlockState(blockPos), player, EnumHand.MAIN_HAND, player.getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord))
						BlockTile.cancelNext = true;
					break;
				case 1: //Destory tile
					LittleTileBox box = null;
					moving = te.getMoving(pos, look);
    				if(stack != null && stack.getItem() instanceof ISpecialBlockSelector)
    				{
    					box = ((ISpecialBlockSelector) stack.getItem()).getBox(te, tile, te.getPos(), player, moving);
    					if(box != null)
    					{
    						te.removeBoxFromTiles(box);
    						if(!player.capabilities.isCreativeMode)
    						{
    							tile.boundingBoxes.clear();
    							tile.boundingBoxes.add(box.copy());
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
    				
    				world.playSound((EntityPlayer)null, blockPos, tile.getSound().getBreakSound(), SoundCategory.BLOCKS, (tile.getSound().getVolume() + 1.0F) / 2.0F, tile.getSound().getPitch() * 0.8F);
    				
    				/*for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
    					LittleTile tileNeighbor = (LittleTile) iterator.next();
						tileNeighbor.onNeighborChangeInside();
					}*/
    				
	    			//te.updateBlock();
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
						
						if(box.isValidBox())
						{
							double ammount = tile.boundingBoxes.get(0).getSize().getPercentVolume()-box.getSize().getPercentVolume();
							boolean success = false;
							if(player.isSneaking())
							{
								if(ItemTileContainer.addBlock(player, ((LittleTileBlock)tile).getBlock(), ((LittleTileBlock)tile).getMeta(), ammount))
									success = true;
							}else{
								if(ItemTileContainer.drainBlock(player, ((LittleTileBlock)tile).getBlock(), ((LittleTileBlock)tile).getMeta(), -ammount))
									success = true;
							}
							
							if(player.capabilities.isCreativeMode || success)
							{
								if(box.isBoxInsideBlock() && te.isSpaceForLittleTile(box.getBox(), tile))
								{
									tile.boundingBoxes.set(0, box);
									tile.updateCorner();
									te.updateBlock();
								}else if(!box.isBoxInsideBlock()){
									box = box.createOutsideBlockBox(direction);
									BlockPos newPos = blockPos.offset(direction);
									IBlockState state = world.getBlockState(newPos);
									TileEntityLittleTiles littleTe = null;
									TileEntity newTE = world.getTileEntity(newPos);
									if(newTE instanceof TileEntityLittleTiles)
										littleTe = (TileEntityLittleTiles) newTE;
									if(state.getMaterial().isReplaceable())
									{
										//new TileEntityLittleTiles();
										world.setBlockState(newPos, LittleTiles.blockTile.getDefaultState());
										littleTe = (TileEntityLittleTiles) world.getTileEntity(newPos);
									}
									if(littleTe != null)
									{
										LittleTile newTile = tile.copy();
										newTile.boundingBoxes.clear();
										newTile.boundingBoxes.add(box);
										newTile.te = littleTe;
										
										if(littleTe.isSpaceForLittleTile(box))
										{
											newTile.place();
											//littleTe.addTile(newTile);
											littleTe.updateBlock();
										}
									}
								}
							}
						}
					}
					break;
				case 3: //COLOR TUBE set Color
					if((tile.getClass() == LittleTileBlock.class || tile instanceof LittleTileBlockColored))
					{
						int color = nbt.getInteger("color");
						
						if(player.isSneaking())
						{
							color = ColorUtils.WHITE;
							if(tile instanceof LittleTileBlockColored)
								color = ((LittleTileBlockColored) tile).color;
							ItemColorTube.setColor(player.getHeldItemMainhand(), color);
						}else{
							
							LittleTile newTile = LittleTileBlockColored.setColor((LittleTileBlock) tile, color);
							if(newTile != null)
							{
								tile.te.removeTile(tile);
								tile.te.addTile(newTile);
							}
							if(tile.isStructureBlock)
							{
								newTile.isStructureBlock = true;
								newTile.structure.getTiles().remove(tile);
								newTile.structure.getTiles().add(newTile);
								if(tile.isMainBlock)
									newTile.structure.setMainTile(newTile);
								newTile.structure.getMainTile().te.updateBlock();
							}
							te.updateBlock();
						}
					}
					break;
				case 4: //RUBBER MALLET
					side = nbt.getInteger("side");
					direction = EnumFacing.getFront(side).getOpposite();
					if(player.isSneaking())
						direction = direction.getOpposite();
					if(tile.canBeMoved(direction))
					{
						if(tile.isStructureBlock)
						{
							if(tile.checkForStructure())
							{
								LittleStructure structure = tile.structure;
								if(structure.hasLoaded())
								{
									ArrayList<LittleTile> tiles = new ArrayList(structure.getTiles());
									for (int i = 0; i < tiles.size(); i++) {
										if(!ItemRubberMallet.moveTile(tiles.get(i).te, direction, tiles.get(i), true))
											return ;
									}
									for (int i = 0; i < tiles.size(); i++)
										ItemRubberMallet.moveTile(tiles.get(i).te, direction, tiles.get(i), false);
											//tiles.get(i).te.updateTiles();
									
									structure.combineTiles();
									structure.selectMainTile();
									structure.moveStructure(direction);
								}else
									player.addChatMessage(new TextComponentString("Cannot move structure (not all tiles are loaded)."));
							}
						}else
							if(ItemRubberMallet.moveTile(te, direction, tile, false))
								te.updateTiles();												
					}
					break;
				case 5: //Glowing
					if(stack != null && stack.getItem() == Items.GLOWSTONE_DUST && player.isSneaking())
					{
						if(!player.isCreative())
						{
							if(tile.glowing){
								if(!player.inventory.addItemStackToInventory(new ItemStack(Items.GLOWSTONE_DUST)))
									player.dropItem(new ItemStack(Items.GLOWSTONE_DUST), true);
							}else{
								stack.stackSize--;
								if(stack.stackSize <= 0)
									player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
							}
						}
						if(tile.glowing)
							player.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
						else
							player.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
						tile.glowing = !tile.glowing;
						te.updateLighting();
					}
					break;
				}
			}
		}
	}
	
}
