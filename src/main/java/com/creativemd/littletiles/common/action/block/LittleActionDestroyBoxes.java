package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.InventoryUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionDestroy.StructurePreview;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.ingredients.CombinedIngredients;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleActionDestroyBoxes extends LittleActionBoxes {
	
	public LittleActionDestroyBoxes(List<LittleTileBox> boxes) {
		super(boxes);
	}
	
	public LittleActionDestroyBoxes() {
		
	}
	
	public List<StructurePreview> destroyedStructures;
	public List<LittleTilePreview> previews;
	
	private boolean containsStructure(LittleStructure structure)
	{
		for (StructurePreview structurePreview : destroyedStructures) {
			if(structurePreview.structure == structure)
				return true;
		}
		return false;
	}
	
	public CombinedIngredients action(EntityPlayer player, TileEntityLittleTiles te, List<LittleTileBox> boxes, boolean simulate)
	{
		LittleTileVec offset = new LittleTileVec(te.getPos());
		CombinedIngredients ingredients = new CombinedIngredients();
		
		for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			
			boolean intersects = false;
			for (int j = 0; j < boxes.size(); j++) {
				if(tile.intersectsWith(boxes.get(j)))
				{
					intersects = true;
					break;
				}
			}
			
			if(!intersects)
				continue;
			
			if(!tile.isStructureBlock && tile.canBeSplitted())
			{
				double volume = 0;
				LittleTilePreview preview = tile.getPreviewTile();
				
				List<LittleTileBox> cutout = new ArrayList<>();
				List<LittleTileBox> newBoxes = tile.cutOut(boxes, cutout);
				
				if(newBoxes != null)
				{
					if(!simulate)
					{						
						for (int i = 0; i < newBoxes.size(); i++) {
							LittleTile newTile = tile.copy();
							newTile.box = newBoxes.get(i);
							newTile.place();
						}
						
						tile.destroy();
					}
					
					for (int l = 0; l < cutout.size(); l++) {
						volume += cutout.get(l).getPercentVolume();
						if(!simulate)
						{
							LittleTilePreview preview2 = preview.copy();
							preview2.box = cutout.get(l).copy();
							preview2.box.addOffset(offset);
							previews.add(preview2);
						}
					}		
				}
			
				if(volume > 0)
					ingredients.addPreview(preview, volume);
			}else{
				ingredients.addPreview(tile.getPreviewTile());
				
				if(!simulate)
				{
					if(!containsStructure(tile.structure) && tile.isLoaded() && tile.structure.hasLoaded())
					{
						destroyedStructures.add(new StructurePreview(tile.structure));
						ItemStack drop = tile.structure.getStructureDrop();
						if(needIngredients(player) && !InventoryUtils.addItemStackToInventory(player.inventory, drop))
							WorldUtils.dropItem(player.world, drop, tile.te.getPos());
						
						tile.destroy();
						
						//tile.structure.removeWorldProperties();
					}else{
						LittleTilePreview preview = tile.getPreviewTile();
						preview.box.addOffset(offset);
						previews.add(preview);
						tile.destroy();
					}
				}
				
			}
		}
		
		return ingredients;
	}

	@Override
	public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes) throws LittleActionException {
		TileEntity tileEntity = loadTe(world, pos, true);
		
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			if(addIngredients(player, action(player, (TileEntityLittleTiles) tileEntity, boxes, true)))
				action(player, (TileEntityLittleTiles) tileEntity, boxes, false);
		}
	}
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		previews = new ArrayList<>();
		destroyedStructures = new ArrayList<>();
		return super.action(player);
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() {
		boolean additionalPreviews = previews.size() > 0;
		LittleAction[] actions = new LittleAction[additionalPreviews ? 1 : 0 + destroyedStructures.size()];
		if(additionalPreviews)
			actions[0] = new LittleActionPlaceAbsolute(previews, null, false, true, true);
		for (int i = 0; i < destroyedStructures.size(); i++) {
			actions[additionalPreviews ? 1 : 0 + i] = destroyedStructures.get(i).getPlaceAction();
		}
		return new LittleActionCombined(actions);
	}
	
}
