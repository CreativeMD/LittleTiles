package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.InventoryUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleActionDestroy extends LittleActionInteract {
	
	public LittleActionDestroy(BlockPos blockPos, EntityPlayer player) {
		super(blockPos, player);
	}
	
	public LittleActionDestroy() {
		
	}
	
	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	public List<LittleTilePreview> destroyedTiles;
	public StructurePreview structurePreview;
	
	@Override
	public LittleAction revert() {
		if(structurePreview != null)
			return structurePreview.getPlaceAction();
		return new LittleActionPlaceAbsolute(destroyedTiles, false);
	}
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player,
			RayTraceResult moving, BlockPos pos) throws LittleActionException{
		
		
		if(tile.isStructureBlock)
		{
			boolean loaded = tile.isLoaded() && tile.structure.hasLoaded();
			if(loaded || player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench)
			{
				if(loaded)
				{
					structurePreview = new StructurePreview(tile.structure);
					ItemStack drop = tile.structure.getStructureDrop();
					if(needIngredients(player) && !InventoryUtils.addItemStackToInventory(player.inventory, drop))
						WorldUtils.dropItem(world, drop, pos);
					tile.destroy();
				}
				else
					tile.te.removeTile(tile);
				
				//if(!world.isRemote)
					//tile.structure.removeWorldProperties();
			}else			
				throw new LittleActionException.StructureNotLoadedException();
		}else{
			
			destroyedTiles = new ArrayList<>();
			
			if(BlockTile.selectEntireBlock(player))
			{
				List<LittleTile> remains = new ArrayList<>();
				for (LittleTile toDestory : te.getTiles()) {
					if(!toDestory.isStructureBlock)
					{
						LittleTilePreview preview = toDestory.getPreviewTile();
						preview.box.addOffset(toDestory.te.getPos());
						destroyedTiles.add(preview);
					}else
						remains.add(toDestory);
				}
				
				addPreviewToInventory(player, destroyedTiles);
				
				te.getTiles().clear();
				te.getTiles().addAll(remains);
				te.updateTiles();
			}else{
				LittleTilePreview preview = tile.getPreviewTile();
				preview.box.addOffset(tile.te.getPos());
				destroyedTiles.add(preview);
				
				addPreviewToInventory(player, destroyedTiles);
				
				tile.destroy();
			}
		}
		
		world.playSound((EntityPlayer)null, pos, tile.getSound().getBreakSound(), SoundCategory.BLOCKS, (tile.getSound().getVolume() + 1.0F) / 2.0F, tile.getSound().getPitch() * 0.8F);
		
		return true;
	}

	@Override
	protected boolean isRightClick() {
		return false;
	}
	
	public static class StructurePreview {
		
		public List<LittleTilePreview> previews;
		public LittleStructure structure;
		
		public StructurePreview(LittleStructure structure) {
			previews = new ArrayList<>();
			for (Iterator<LittleTile> iterator = structure.getTiles(); iterator.hasNext();) {
				LittleTile tile2 = iterator.next();
				LittleTilePreview preview = tile2.getPreviewTile();
				preview.box.addOffset(tile2.te.getPos());
				previews.add(preview);
			}
			this.structure = structure;
		}
		
		public LittleAction getPlaceAction()
		{
			return new LittleActionPlaceAbsolute(previews, structure, false, true, false);
		}
		
		@Override
		public int hashCode() {
			return structure.hashCode();
		}
		
		@Override
		public boolean equals(Object paramObject) {
			if(paramObject instanceof StructurePreview)
				return structure == ((StructurePreview) paramObject).structure;
			if(paramObject instanceof LittleStructure)
				return structure == paramObject;
			return false;
		}
	}

}
