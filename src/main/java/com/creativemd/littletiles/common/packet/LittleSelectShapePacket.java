package com.creativemd.littletiles.common.packet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleSelectShapePacket extends CreativeCorePacket {
	
	public static enum LittleSelectShapeAction 
	{
		UTILITY_KNIFE {
			@Override
			public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes, NBTTagCompound nbt) {
				
				TileEntity tileEntity = world.getTileEntity(pos);
				if(SubContainerHammer.isBlockValid(state.getBlock()))
				{
					world.setBlockState(pos, LittleTiles.blockTile.getDefaultState());
					tileEntity = world.getTileEntity(pos);
					
					
					LittleTileBox box = new LittleTileBox(0,0,0,LittleTile.maxPos,LittleTile.maxPos,LittleTile.maxPos);
					
					LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
					tile.te = (TileEntityLittleTiles) tileEntity;
					tile.boundingBoxes.add(box);
					tile.place();
				}
				
				if(tileEntity instanceof TileEntityLittleTiles)
				{
					TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
					
					List<BlockEntry> entries = new ArrayList<>();
					
					for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
						LittleTile tile = iterator.next();
						
						boolean intersects = false;
						for (int i = 0; i < tile.boundingBoxes.size(); i++) {
							for (int j = 0; j < boxes.size(); j++) {
								if(tile.boundingBoxes.get(i).intersectsWith(boxes.get(j)))
								{
									intersects = true;
									break;
								}
							}
						}
						
						if(!intersects)
							continue;
						
						if(!tile.isStructureBlock && tile.canBeSplitted())
						{
							BlockEntry entry = tile.getBlockEntry();
							entry.value = 0;
							if(tile.canHaveMultipleBoundingBoxes())
							{
								int i = 0;
								int max = tile.boundingBoxes.size();
								while (i < max) {
									LittleTileBox box = tile.boundingBoxes.get(i);
									
									List<LittleTileBox> cutout = new ArrayList<>();
									List<LittleTileBox> newBoxes = box.cutOut(boxes, cutout);
									
									if(newBoxes != null)
									{
										tile.boundingBoxes.remove(i);
										tile.boundingBoxes.addAll(newBoxes);
										
										for (int l = 0; l < cutout.size(); l++) {
											entry.value += cutout.get(l).getSize().getPercentVolume();
										}
										
										max--;
									}else
										i++;
								}
								
								if(tile.boundingBoxes.isEmpty())
									tile.destroy();
								else
									LittleTileBox.combineBoxes(tile.boundingBoxes);
								
							}else{
								LittleTileBox box = tile.boundingBoxes.get(0);
								
								List<LittleTileBox> cutout = new ArrayList<>();
								List<LittleTileBox> newBoxes = box.cutOut(boxes, cutout);
								
								if(newBoxes != null)
								{
									tile.boundingBoxes.clear();
									
									for (int i = 0; i < newBoxes.size(); i++) {
										LittleTile newTile = tile.copy();
										newTile.boundingBoxes.add(newBoxes.get(i));
										newTile.place();
									}
									
									for (int l = 0; l < cutout.size(); l++) {
										entry.value += cutout.get(l).getSize().getPercentVolume();
									}
									
									tile.destroy();
								}
							}
							
							if(entry.value > 0)
								entries.add(entry);
						}else{
							tile.destroy();
						}
					}
					
					if(!entries.isEmpty() && !player.capabilities.isCreativeMode)
					{
						double value = 0;
						for (int i = 0; i < entries.size(); i++) {
							BlockEntry entry = entries.get(i);
							entry.value = value;
							boolean successful = false;
							if(entry != null)
								successful = ItemTileContainer.addBlock(player, entry.block, entry.meta, entry.value);
							if(!successful)
								WorldUtils.dropItem(player, entry.getTileItemStack());
						}						
					}
				}
				
				
			}
		},
		COLOR_TUBE {
			
			@Override
			public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes, NBTTagCompound nbt) {
				
				TileEntity tileEntity = world.getTileEntity(pos);
				if(SubContainerHammer.isBlockValid(state.getBlock()))
				{
					world.setBlockState(pos, LittleTiles.blockTile.getDefaultState());
					tileEntity = world.getTileEntity(pos);
					
					
					LittleTileBox box = new LittleTileBox(0,0,0,LittleTile.maxPos,LittleTile.maxPos,LittleTile.maxPos);
					
					LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
					tile.te = (TileEntityLittleTiles) tileEntity;
					tile.boundingBoxes.add(box);
					tile.place();
				}
				
				if(tileEntity instanceof TileEntityLittleTiles)
				{
					int color = nbt.getInteger("color");
					
					TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
					
					List<BlockEntry> entries = new ArrayList<>();
					
					te.preventUpdate = true;
					
					for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
						LittleTile tile = iterator.next();
						
						boolean intersects = false;
						for (int i = 0; i < tile.boundingBoxes.size(); i++) {
							for (int j = 0; j < boxes.size(); j++) {
								if(tile.boundingBoxes.get(i).intersectsWith(boxes.get(j)))
								{
									intersects = true;
									break;
								}
							}
						}
						
						if(!intersects || !(tile.getClass() == LittleTileBlock.class || tile instanceof LittleTileBlockColored) || (tile.isStructureBlock && (!tile.isLoaded() || !tile.structure.hasLoaded())))
							continue;
						
						if(tile.canBeSplitted())
						{
							if(tile.canHaveMultipleBoundingBoxes())
							{
								int i = 0;
								int max = tile.boundingBoxes.size();
								
								LittleTile tempTile = tile.copy();
								LittleTile changedTile = LittleTileBlockColored.setColor((LittleTileBlock) tempTile, color);
								if(changedTile == null)
									changedTile = tempTile;
								
								changedTile.boundingBoxes.clear();
								
								while (i < max) {
									LittleTileBox box = tile.boundingBoxes.get(i);
									
									List<LittleTileBox> cutout = new ArrayList<>();
									List<LittleTileBox> newBoxes = box.cutOut(boxes, cutout);
									
									if(newBoxes != null)
									{
										tile.boundingBoxes.remove(i);
										tile.boundingBoxes.addAll(newBoxes);
										
										changedTile.boundingBoxes.addAll(cutout);
										
										max--;
									}else
										i++;
								}
								
								LittleTileBox.combineBoxes(tile.boundingBoxes);
								LittleTileBox.combineBoxes(changedTile.boundingBoxes);
								
								changedTile.place();
								
								if(tile.isStructureBlock)
									changedTile.structure.addTile(changedTile);
								
								if(tile.isMainBlock)
									tile.structure.setMainTile(tile);
								
								if(tile.isStructureBlock)
									tile.structure.updateStructure();
								
								if(tile.boundingBoxes.isEmpty())
								{
									tile.isStructureBlock = false;
									tile.destroy();
								}
								
							}else{
								LittleTileBox box = tile.boundingBoxes.get(0);
								
								List<LittleTileBox> cutout = new ArrayList<>();
								List<LittleTileBox> newBoxes = box.cutOut(boxes, cutout);
								
								if(newBoxes != null)
								{
									tile.boundingBoxes.clear();
									
									LittleTile tempTile = tile.copy();
									LittleTile changedTile = LittleTileBlockColored.setColor((LittleTileBlock) tempTile, color);
									if(changedTile == null)
										changedTile = tempTile;
									
									if(tile.isStructureBlock)
										tile.structure.removeTile(tile);
									
									for (int i = 0; i < newBoxes.size(); i++) {
										LittleTile newTile = tile.copy();
										newTile.boundingBoxes.add(newBoxes.get(i));
										newTile.place();
										if(tile.isStructureBlock)
											tile.structure.addTile(newTile);
									}
									
									for (int i = 0; i < cutout.size(); i++) {
										LittleTile newTile = changedTile.copy();
										newTile.boundingBoxes.add(cutout.get(i));
										newTile.place();
										if(tile.isStructureBlock)
											tile.structure.addTile(newTile);
									}
									
									if(tile.isMainBlock)
										tile.structure.selectMainTile();
									
									if(tile.isStructureBlock)
										tile.structure.updateStructure();
									
									tile.isStructureBlock = false;
									tile.destroy();
								}
							}
						}else{
							LittleTile changedTile = LittleTileBlockColored.setColor((LittleTileBlock) tile, color);
							if(changedTile != null)
							{
								
								changedTile.place();
								
								if(tile.isStructureBlock)
								{
									changedTile.isStructureBlock = true;
									changedTile.structure.removeTile(tile);
									changedTile.structure.addTile(changedTile);
									
									if(tile.isStructureBlock)
										tile.structure.updateStructure();
									
									tile.isStructureBlock = false;
									tile.destroy();
									
									if(tile.isMainBlock)
										changedTile.structure.setMainTile(changedTile);
									
								}
							}
						}
					}
					te.preventUpdate = false;
					
					te.combineTiles();
				}
			}
		};
		
		public abstract void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes, NBTTagCompound nbt);
		
	}
	
	public List<LittleTileBox> boxes;
	public LittleSelectShapeAction action;
	public NBTTagCompound nbt;
	
	public LittleSelectShapePacket() {
		
	}
	
	public LittleSelectShapePacket(List<LittleTileBox> boxes, LittleSelectShapeAction action, NBTTagCompound nbt) {
		this.boxes = boxes;
		this.action = action;
		this.nbt = nbt;
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(boxes.size());
		
		for (int i = 0; i < boxes.size(); i++) {
			LittleTileBox box = boxes.get(i);
			buf.writeInt(box.minX);
			buf.writeInt(box.minY);
			buf.writeInt(box.minZ);
			buf.writeInt(box.maxX);
			buf.writeInt(box.maxY);
			buf.writeInt(box.maxZ);
		}
		
		buf.writeInt(action.ordinal());
		
		writeNBT(buf, nbt);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		int size = buf.readInt();
		boxes = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			boxes.add(new LittleTileBox(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt()));
		}
		action = LittleSelectShapeAction.values()[buf.readInt()];
		
		nbt = readNBT(buf);
	}

	@Override
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		World world = player.world;
		HashMapList<BlockPos, LittleTileBox> boxesMap = new HashMapList<>();
		
		for (int i = 0; i < boxes.size(); i++) {
			boxes.get(i).split(boxesMap);
		}
		
		for (Iterator<Entry<BlockPos, ArrayList<LittleTileBox>>> iterator = boxesMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<BlockPos, ArrayList<LittleTileBox>> entry = iterator.next();
			BlockPos pos = entry.getKey();
			IBlockState state = world.getBlockState(pos);
			if(!LittleBlockPacket.isAllowedToInteract(player, pos, false, EnumFacing.EAST))
			{
				world.notifyBlockUpdate(pos, state, state, 3);
				continue ;
			}
			
			action.action(world, player, pos, state, entry.getValue(), nbt);
		}
		
		world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1, 1);
	}

}
