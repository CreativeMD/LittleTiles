package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.common.world.FakeWorld;
import com.creativemd.littletiles.client.render.world.LittleRenderChunkSuppilier;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationState;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.type.LittleFixedStructure;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviewsStructure;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AnimationPreview {
	
	public final EntityAnimation animation;
	public final LittlePreviews previews;
	public final LittleBox entireBox;
	public final LittleGridContext context;
	public final AxisAlignedBB box;
	
	public AnimationPreview(LittlePreviews previews) {
		this.previews = previews;
		BlockPos pos = new BlockPos(0, 75, 0);
		FakeWorld fakeWorld = FakeWorld.createFakeWorld("animationViewer", true);
		fakeWorld.renderChunkSupplier = new LittleRenderChunkSuppilier();
		
		if (!previews.hasStructure()) {
			NBTTagCompound nbt = new NBTTagCompound();
			new LittleFixedStructure(LittleStructureRegistry.getStructureType(LittleFixedStructure.class)).writeToNBT(nbt);
			LittlePreviewsStructure newPreviews = new LittlePreviewsStructure(nbt, previews.context);
			newPreviews.assign(previews);
			previews = newPreviews;
		}
		
		List<PlacePreview> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleVec.ZERO);
		
		previews.deleteCachedStructure();
		
		HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, pos);
		
		LittleStructure structure = previews.getStructure();
		LittleActionPlaceStack.placeTilesWithoutPlayer(fakeWorld, previews.context, splitted, structure, PlacementMode.all, pos, null, null, null, null);
		
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
			public void transform(LittleTransformation transformation) {
				
			}
			
		}.addStateAndSelect("nothing", new AnimationState()), pos, UUID.randomUUID(), new StructureAbsolute(pos, entireBox, previews.context), structure == null ? null : structure.getAbsoluteIdentifier());
		
		previews.deleteCachedStructure();
	}
	
}
