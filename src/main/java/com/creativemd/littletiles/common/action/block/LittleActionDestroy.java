package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
	
	public LittleAbsolutePreviews destroyedTiles;
	public StructurePreview structurePreview;
	
	@Override
	public LittleAction revert() {
		if(structurePreview != null)
			return structurePreview.getPlaceAction();
		destroyedTiles.convertToSmallest();
		return new LittleActionPlaceAbsolute(destroyedTiles, PlacementMode.normal);
	}
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player,
			RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException{
		
		if(tile.isStructureBlock)
		{
			boolean loaded = tile.isLoaded() && tile.structure.hasLoaded();
			if(loaded || player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench)
			{
				if(loaded)
				{
					structurePreview = new StructurePreview(tile.structure);
					if(needIngredients(player) && !player.world.isRemote)
						WorldUtils.dropItem(world, tile.structure.getStructureDrop(), pos);
					tile.destroy();
				}
				else
					tile.te.removeTile(tile);
				
				//if(!world.isRemote)
					//tile.structure.removeWorldProperties();
			}else			
				throw new LittleActionException.StructureNotLoadedException();
		}else{
			
			destroyedTiles = new LittleAbsolutePreviews(pos, te.getContext());
			List<LittleTile> tiles = new ArrayList<>();
			
			if(BlockTile.selectEntireBlock(player, secondMode))
			{
				List<LittleTile> remains = new ArrayList<>();
				for (LittleTile toDestory : te.getTiles()) {
					if(!toDestory.isStructureBlock)
					{
						destroyedTiles.addTile(toDestory); // No need to use addPreivew as all previews are inside one block
						tiles.add(toDestory);
					}else
						remains.add(toDestory);
				}
				
				if(player.isCreative())
					addPreviewToInventory(player, destroyedTiles);
				else if(!world.isRemote){
					ItemStack drop = new ItemStack(LittleTiles.multiTiles);
					LittleTilePreview.saveTiles(world, te.getContext(), tiles, drop);
					WorldUtils.dropItem(world, drop, pos);
				}
				
				tiles.clear();
				
				te.getTiles().clear();
				te.getTiles().addAll(remains);
				te.updateTiles();
			}else{
				destroyedTiles.addTile(tile); // No need to use addPreivew as all previews are inside one block
				
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
		
		public LittleAbsolutePreviews previews;
		public boolean requiresItemStack;
		public NBTTagCompound structureNBT;
		public LittleStructure structure;
		
		public StructurePreview(LittleStructure structure) {
			if(!structure.hasLoaded())
				throw new RuntimeException("Structure is not loaded, can't create preview of it!");
			previews = new LittleAbsolutePreviews(structure.getMainTile().te.getPos(), structure.getMainTile().getContext());
			for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : structure.entrySet()) {
				previews.addTiles(entry.getValue());
			}
			previews.convertToSmallest();
			requiresItemStack = structure.canOnlyBePlacedByItemStack();
			this.structureNBT = new NBTTagCompound();
			structure.writeToNBTPreview(structureNBT, previews.pos);
			this.structure = structure;
		}
		
		public LittleAction getPlaceAction()
		{
			if(requiresItemStack)
				return new LittleActionPlaceAbsolute.LittleActionPlaceAbsolutePremade(previews, LittleStructure.createAndLoadStructure(structureNBT, null), PlacementMode.all, false);
			return new LittleActionPlaceAbsolute(previews, LittleStructure.createAndLoadStructure(structureNBT, null), PlacementMode.all, false);
		}
		
		@Override
		public int hashCode() {
			return structureNBT.hashCode();
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
