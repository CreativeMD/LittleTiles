package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleActionColorBoxes extends LittleActionBoxes {
	
	public int color;
	
	public LittleActionColorBoxes(List<LittleTileBox> boxes, int color) {
		super(boxes);
		this.color = color;
	}
	
	public LittleActionColorBoxes() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
		buf.writeInt(color);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
		color = buf.readInt();
	}
	
	public ColorUnit action(TileEntityLittleTiles te, List<LittleTileBox> boxes, ColorUnit gained, boolean simulate)
	{
		double colorVolume = 0;
		
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
					if(simulate)
					{
						double volume = 0;
						for (LittleTileBox box : tile.boundingBoxes) {
							List<LittleTileBox> cutout = new ArrayList<>();
							box.cutOut(boxes, cutout);
							
							for (LittleTileBox box2 : cutout) {
								colorVolume += box2.getPercentVolume();
								volume += box2.getPercentVolume();
							}
						}
						
						gained.addColorUnit(ColorUnit.getRequiredColors(tile.getPreviewTile(), volume));
					}else{
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
					
					}
				}else{
					LittleTileBox box = tile.boundingBoxes.get(0);
					
					if(simulate)
					{
						double volume = 0;
						List<LittleTileBox> cutout = new ArrayList<>();
						box.cutOut(boxes, cutout);
						for (LittleTileBox box2 : cutout) {
							colorVolume += box2.getPercentVolume();
							volume += box2.getPercentVolume();
						}
						
						gained.addColorUnit(ColorUnit.getRequiredColors(tile.getPreviewTile(), volume));
						
					}else{
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
				}
			}else{
				if(simulate)
				{
					colorVolume += tile.getPercentVolume();
					gained.addColorUnit(ColorUnit.getRequiredColors(tile.getPreviewTile(), tile.getPercentVolume()));
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
		}
		ColorUnit toDrain = ColorUnit.getRequiredColors(color);
		toDrain.scale(colorVolume);
		
		gained.drain(toDrain);
		
		return toDrain;
	}

	@Override
	public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes) throws LittleActionException {
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
			
			List<BlockIngredient> entries = new ArrayList<>();
			
			te.preventUpdate = true;
			
			ColorUnit gained = new ColorUnit();
			
			ColorUnit toDrain = action(te, boxes, gained, true);
			
			if(addIngredients(player, null, gained, true))
			{
				drainIngredients(player, null, toDrain);
				addIngredients(player, null, gained);
				
				action(te, boxes, gained, false);
			}
			
			te.preventUpdate = false;
			
			te.combineTiles();
		}
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
