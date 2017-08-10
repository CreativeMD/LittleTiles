package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleActionDestroyBoxes extends LittleActionBoxes {
	
	public LittleActionDestroyBoxes(List<LittleTileBox> boxes) {
		super(boxes);
	}
	
	public LittleActionDestroyBoxes() {
		
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
			
			BlockIngredients ingredients = new BlockIngredients();
			
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
					BlockIngredient ingredient = tile.getIngredient();
					ingredient.value = 0;
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
									ingredient.value += cutout.get(l).getSize().getPercentVolume();
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
								ingredient.value += cutout.get(l).getSize().getPercentVolume();
							}
							
							tile.destroy();
						}
					}
					
					if(ingredient.value > 0)
						ingredients.addIngredient(ingredient);
				}else{
					tile.destroy();
				}
			}
			
			addIngredients(player, ingredients, null);
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
