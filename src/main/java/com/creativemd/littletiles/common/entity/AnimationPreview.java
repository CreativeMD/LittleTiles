package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.common.world.FakeWorld;
import com.creativemd.littletiles.client.render.world.LittleRenderChunkSuppilier;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.animation.AnimationState;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.type.LittleFixedStructure;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementResult;
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
			previews = new LittlePreviews(nbt, previews);
		}
		
		Placement placement = new Placement(null, PlacementHelper.getAbsolutePreviews(fakeWorld, previews, pos, PlacementMode.all));
		List<PlacePreview> placePreviews = new ArrayList<>();
		PlacementResult result = null;
		try {
			result = placement.place();
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		
		entireBox = previews.getSurroundingBox();
		context = previews.getContext();
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
			
		}.addStateAndSelect("nothing", new AnimationState()), pos, UUID.randomUUID(), new StructureAbsolute(pos, entireBox, previews.getContext()), result.parentStructure == null ? null : result.parentStructure.getAbsoluteIdentifier());
			
	}
	
}
