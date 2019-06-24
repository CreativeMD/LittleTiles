package com.creativemd.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.client.render.LittleRenderChunkSuppilier;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.gui.dialogs.SubGuiDoorEvents.GuiDoorEventsButton;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.connection.IStructureChildConnector;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.type.door.LittleAdvancedDoor.LittleAdvancedDoorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleAxisDoor.LittleAxisDoorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorActivator.LittleDoorActivatorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleSlidingDoor.LittleSlidingDoorParser;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.utils.animation.AnimationTimeline;
import com.creativemd.littletiles.common.utils.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.utils.animation.event.ChildActivateEvent;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleDoorBase extends LittleDoor {
	
	public LittleDoorBase(LittleStructureType type) {
		super(type);
	}
	
	public int duration = 50;
	public boolean stayAnimated = false;
	public List<AnimationEvent> events;
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		events = new ArrayList<>();
		NBTTagList list = nbt.getTagList("events", 10);
		for (int i = 0; i < list.tagCount(); i++)
			events.add(AnimationEvent.loadFromNBT(list.getCompoundTagAt(i)));
		if (nbt.hasKey("duration"))
			duration = nbt.getInteger("duration");
		else
			duration = 50;
		stayAnimated = nbt.getBoolean("stayAnimated");
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		NBTTagList list = new NBTTagList();
		for (AnimationEvent event : events)
			list.appendTag(event.writeToNBT(new NBTTagCompound()));
		nbt.setTag("events", list);
		nbt.setInteger("duration", duration);
		if (stayAnimated)
			nbt.setBoolean("stayAnimated", stayAnimated);
	}
	
	public abstract DoorTransformation[] getDoorTransformations(@Nullable EntityPlayer player);
	
	public abstract void transformPreview(LittleAbsolutePreviewsStructure previews, DoorTransformation transformation);
	
	public LittleAbsolutePreviewsStructure getDoorPreviews(DoorTransformation transformation) {
		LittleAbsolutePreviewsStructure previews = getAbsolutePreviewsSameWorld(transformation.center);
		transformPreview(previews, transformation);
		transformation.transform(previews);
		return previews;
	}
	
	@Override
	public void beforeTick(EntityAnimation animation, int tick) {
		super.beforeTick(animation, tick);
		DoorController controller = (DoorController) animation.controller;
		for (AnimationEvent event : events)
			if (event.shouldBeProcessed(tick))
				event.process(controller);
			
	}
	
	@Override
	public int getCompleteDuration() {
		int duration = this.duration;
		for (AnimationEvent event : events)
			duration = Math.max(duration, event.getMinimumRequiredDuration(this));
		return duration;
	}
	
	@Override
	public List<LittleDoor> collectDoorsToCheck() {
		List<Integer> children = new ArrayList<>();
		for (AnimationEvent event : events)
			if (event instanceof ChildActivateEvent && !children.contains(((ChildActivateEvent) event).childId))
				children.add(((ChildActivateEvent) event).childId);
			
		List<LittleDoor> doors = new ArrayList<>();
		if (children.isEmpty())
			return doors;
		for (Integer integer : children)
			doors.add((LittleDoor) this.children.get(integer).getStructure(getWorld()));
		return doors;
	}
	
	@Override
	public DoorOpeningResult canOpenDoor(@Nullable EntityPlayer player) {
		DoorOpeningResult result = super.canOpenDoor(player);
		
		if (result == null)
			return null;
		
		for (AnimationEvent event : events)
			event.reset();
		
		if (isAnimated()) // No transformations done if the door is already an animation
			return result;
		
		DoorTransformation[] transformations = getDoorTransformations(player); // Only done if the door is placed down
		for (DoorTransformation transformation : transformations) {
			List<PlacePreviewTile> placePreviews = new ArrayList<>();
			
			LittleAbsolutePreviewsStructure previews = getDoorPreviews(transformation);
			
			previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
			
			HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos);
			if (LittleActionPlaceStack.canPlaceTiles(player, getWorld(), splitted, PlacementMode.all.getCoordsToCheck(splitted, previews.pos), PlacementMode.all, (LittleTile x) -> !x.isChildOfStructure(this))) {
				if (transformations.length == 1)
					return result;
				
				if (result.isEmpty())
					result = new DoorOpeningResult(new NBTTagCompound());
				result.nbt.setIntArray("transform", transformation.array());
				
				return result;
			}
		}
		return null;
	}
	
	public EntityAnimation place(World world, @Nullable EntityPlayer player, LittleAbsolutePreviewsStructure previews, DoorController controller, UUID uuid, StructureAbsolute absolute) {
		List<PlacePreviewTile> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
		
		HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos);
		ArrayList<TileEntityLittleTiles> blocks = new ArrayList<>();
		SubWorld fakeWorld = SubWorld.createFakeWorld(world);
		fakeWorld.renderChunkSupplier = new LittleRenderChunkSuppilier();
		
		LittleActionPlaceStack.placeTilesWithoutPlayer(fakeWorld, previews.context, splitted, previews.getStructure(), PlacementMode.all, previews.pos, null, null, null, null);
		
		controller.activator = player;
		
		if (world.isRemote) {
			controller.markWaitingForApprove();
			
			for (TileEntityLittleTiles te : tiles.keySet())
				if (te.waitingAnimation != null)
					te.clearWaitingAnimations();
		}
		
		LittleStructure newDoor = previews.getStructure();
		
		EntityAnimation animation = new EntityAnimation(world, fakeWorld, controller, previews.pos, uuid, absolute, newDoor.getAbsoluteIdentifier());
		
		if (parent != null) {
			LittleStructure parentStructure = parent.getStructure(world);
			parentStructure.updateChildConnection(parent.getChildID(), newDoor);
			newDoor.updateParentConnection(parent.getChildID(), parentStructure);
		}
		
		// Move animated worlds
		for (IStructureChildConnector child : children.values()) {
			if (child.isLinkToAnotherWorld()) {
				LittleStructure childStructure = child.getStructure(getWorld());
				World childWorld = childStructure.getWorld();
				if (childWorld instanceof SubWorld) {
					Entity entity = ((SubWorld) childWorld).parent;
					getWorld().loadedEntityList.remove(entity);
					if (entity instanceof EntityAnimation)
						((EntityAnimation) entity).setParentWorld(fakeWorld);
					else
						entity.world = fakeWorld;
					fakeWorld.spawnEntity(entity);
				}
				
			}
		}
		
		world.spawnEntity(animation);
		return animation;
	}
	
	@Override
	public EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, DoorOpeningResult result) {
		if (isAnimated()) {
			((DoorController) animation.controller).activate();
			return animation;
		}
		
		DoorTransformation transform;
		if (!result.isEmpty() && result.nbt.hasKey("transform"))
			transform = new DoorTransformation(result.nbt.getIntArray("transform"));
		else
			transform = getDoorTransformations(player)[0];
		
		LittleAbsolutePreviewsStructure previews = getDoorPreviews(transform);
		StructureAbsolute absolute = getAbsoluteAxis();
		
		for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : getAllTiles(new HashMapList<>()).entrySet()) {
			entry.getKey().removeTiles(entry.getValue());
		}
		
		return place(getWorld(), player, previews, createController(result, uuid, previews, transform, getCompleteDuration()), uuid.next(), absolute);
	}
	
	public abstract DoorController createController(DoorOpeningResult result, UUIDSupplier supplier, LittleAbsolutePreviewsStructure previews, DoorTransformation transformation, int completeDuration);
	
	public abstract StructureAbsolute getAbsoluteAxis();
	
	public static void initDoors() {
		LittleStructureRegistry.registerStructureType("door", "door", LittleAxisDoor.class, LittleStructureAttribute.NONE, LittleAxisDoorParser.class);
		LittleStructureRegistry.registerStructureType("slidingDoor", "door", LittleSlidingDoor.class, LittleStructureAttribute.NONE, LittleSlidingDoorParser.class);
		LittleStructureRegistry.registerStructureType("advancedDoor", "door", LittleAdvancedDoor.class, LittleStructureAttribute.NONE, LittleAdvancedDoorParser.class);
		LittleStructureRegistry.registerStructureType("doorActivator", "door", LittleDoorActivator.class, LittleStructureAttribute.NONE, LittleDoorActivatorParser.class);
	}
	
	public static class DoorTransformation {
		
		public BlockPos center;
		
		public int rotX;
		public int rotY;
		public int rotZ;
		
		public LittleTileVec doubledRotationCenter;
		
		public LittleTileVecContext offset;
		
		public DoorTransformation(int[] array) {
			if (array.length != 13)
				throw new IllegalArgumentException("Invalid array when creating door transformation!");
			
			center = new BlockPos(array[0], array[1], array[2]);
			rotX = array[3];
			rotY = array[4];
			rotZ = array[5];
			doubledRotationCenter = new LittleTileVec(array[6], array[7], array[8]);
			offset = new LittleTileVecContext(LittleGridContext.get(array[12]), new LittleTileVec(array[9], array[10], array[11]));
		}
		
		public DoorTransformation(BlockPos center, int rotX, int rotY, int rotZ, LittleTileVec doubledRotationCenter, LittleTileVecContext offset) {
			this.center = center;
			this.rotX = rotX;
			this.rotY = rotY;
			this.rotZ = rotZ;
			this.doubledRotationCenter = doubledRotationCenter;
			this.offset = offset;
		}
		
		public DoorTransformation(BlockPos center, Rotation rotation) {
			this.center = center;
			this.rotX = rotation.axis == Axis.X ? (rotation.clockwise ? 1 : -1) : 0;
			this.rotY = rotation.axis == Axis.Y ? (rotation.clockwise ? 1 : -1) : 0;
			this.rotZ = rotation.axis == Axis.Z ? (rotation.clockwise ? 1 : -1) : 0;
			this.doubledRotationCenter = new LittleTileVec(0, 0, 0);
			this.offset = new LittleTileVecContext();
		}
		
		public Rotation getRotation(Axis axis) {
			switch (axis) {
			case X:
				if (rotX == 0)
					return null;
				return Rotation.getRotation(axis, rotX > 0);
			case Y:
				if (rotY == 0)
					return null;
				return Rotation.getRotation(axis, rotY > 0);
			case Z:
				if (rotZ == 0)
					return null;
				return Rotation.getRotation(axis, rotZ > 0);
			}
			return null;
		}
		
		public void transform(LittleAbsolutePreviews previews) {
			if (rotX != 0) {
				Rotation rotation = getRotation(Axis.X);
				for (int i = 0; i < Math.abs(rotX); i++)
					previews.rotatePreviews(rotation, doubledRotationCenter);
			}
			if (rotY != 0) {
				Rotation rotation = getRotation(Axis.Y);
				for (int i = 0; i < Math.abs(rotY); i++)
					previews.rotatePreviews(rotation, doubledRotationCenter);
			}
			if (rotZ != 0) {
				Rotation rotation = getRotation(Axis.Z);
				for (int i = 0; i < Math.abs(rotZ); i++)
					previews.rotatePreviews(rotation, doubledRotationCenter);
			}
			
			if (offset != null)
				previews.movePreviews(offset.context, offset.vec);
		}
		
		public int[] array() {
			return new int[] { center.getX(), center.getY(), center.getZ(), rotX, rotY, rotZ, doubledRotationCenter.x,
			        doubledRotationCenter.y, doubledRotationCenter.z, offset.vec.x, offset.vec.y, offset.vec.z,
			        offset.context.size };
		}
		
		@Override
		public String toString() {
			return "center:" + center.getX() + "," + center.getY() + "," + center.getZ() + ";rotation:" + rotX + "," + rotY + "," + rotZ + ";offset:" + offset.vec.x + "," + offset.vec.y + "," + offset.vec.z + ";context:" + offset.context;
		}
	}
	
	public static abstract class LittleDoorBaseParser extends LittleStructureGuiParser {
		
		public LittleDoorBaseParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@SideOnly(Side.CLIENT)
		@CustomEventSubscribe
		public void onChanged(GuiControlChangedEvent event) {
			if (event.source.is("duration_s") || event.source.is("children_activate"))
				updateTimeline();
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			parent.controls.add(new GuiCheckBox("stayAnimated", CoreControl.translate("gui.door.stayAnimated"), 0, 120, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).stayAnimated : false).setCustomTooltip(CoreControl.translate("gui.door.stayAnimatedTooltip")));
			parent.controls.add(new GuiLabel(CoreControl.translate("gui.door.duration") + ":", 90, 122));
			parent.controls.add(new GuiSteppedSlider("duration_s", 140, 122, 50, 6, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).duration : 50, 1, 500));
			parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
			updateTimeline();
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleDoorBase parseStructure(LittlePreviews previews) {
			GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
			GuiCheckBox checkBox = (GuiCheckBox) parent.get("stayAnimated");
			GuiDoorEventsButton button = (GuiDoorEventsButton) parent.get("children_activate");
			int duration = (int) slider.value;
			boolean stayAnimated = checkBox.value;
			LittleDoorBase door = parseStructure();
			door.duration = duration;
			door.stayAnimated = stayAnimated;
			door.events = button.events;
			List<LittlePreviews> previewChildren = previews.getChildren();
			
			if (!previewChildren.isEmpty()) {
				BitSet set = new BitSet(previewChildren.size());
				for (AnimationEvent event : door.events)
					if (event instanceof ChildActivateEvent)
						set.set(((ChildActivateEvent) event).childId);
					
				for (int i = 0; i < previewChildren.size(); i++)
					if (set.get(i))
						previewChildren.get(i).getStructureData().setBoolean("activateParent", true);
					else
						previewChildren.get(i).getStructureData().removeTag("activateParent");
			}
			return door;
		}
		
		@SideOnly(Side.CLIENT)
		public abstract LittleDoorBase parseStructure();
		
		@SideOnly(Side.CLIENT)
		public abstract void populateTimeline(AnimationTimeline timeline);
		
		public void updateTimeline() {
			GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
			AnimationTimeline timeline = new AnimationTimeline((int) slider.value, new PairList<>());
			GuiDoorEventsButton children = (GuiDoorEventsButton) parent.get("children_activate");
			populateTimeline(timeline);
			handler.setTimeline(timeline, children.events);
		}
		
	}
}
