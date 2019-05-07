package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.common.world.FakeWorld;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AnimationPreview {
	
	public final EntityAnimation animation;
	public final LittlePreviews previews;
	public final LittleTileBox entireBox;
	public final LittleGridContext context;
	public final AxisAlignedBB box;
	
	public AnimationPreview(LittlePreviews previews) {
		this.previews = previews;
		BlockPos pos = new BlockPos(0, 75, 0);
		FakeWorld fakeWorld = FakeWorld.createFakeWorld("animationViewer", true);
		
		List<PlacePreviewTile> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
		
		previews.deleteCachedStructure();
		
		HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, pos);
		ArrayList<TileEntityLittleTiles> blocks = new ArrayList<>();
		
		LittleStructure structure = previews.getStructure();
		LittleActionPlaceStack.placeTilesWithoutPlayer(fakeWorld, previews.context, splitted, structure, PlacementMode.all, pos, null, null, null, null);
		for (Iterator iterator = fakeWorld.loadedTileEntityList.iterator(); iterator.hasNext();) {
			TileEntity te = (TileEntity) iterator.next();
			if (te instanceof TileEntityLittleTiles)
				blocks.add((TileEntityLittleTiles) te);
		}
		
		entireBox = previews.getSurroundingBox();
		context = previews.context;
		box = entireBox.getBox(context);
		
		animation = new EntityAnimation(fakeWorld, fakeWorld, (EntityAnimationController) new EntityAnimationController() {
			
			@Override
			protected void writeToNBTExtra(NBTTagCompound nbt) {
				
			}
			
			@Override
			protected void readFromNBT(NBTTagCompound nbt) {
				
			}
			
			@Override
			public boolean onRightClick() {
				return false;
			}
		}.addStateAndSelect("nothing", new AnimationState()), pos, UUID.randomUUID(), new StructureAbsolute(pos, entireBox, previews.context), structure == null ? null : structure.getAbsoluteIdentifier()) {
			
			@Override
			public boolean shouldAddDoor() {
				return false;
			}
		};
		
		previews.deleteCachedStructure();
	}
	
}
