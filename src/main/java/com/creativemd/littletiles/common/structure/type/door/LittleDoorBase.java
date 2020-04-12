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
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.client.gui.dialogs.SubGuiDoorEvents.GuiDoorEventsButton;
import com.creativemd.littletiles.client.render.world.LittleRenderChunkSuppilier;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack.LittlePlaceResult;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.IAnimatedStructure;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.exception.MissingTileEntity;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.type.door.LittleAdvancedDoor.LittleAdvancedDoorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleAxisDoor.LittleAxisDoorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorActivator.LittleDoorActivatorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleSlidingDoor.LittleSlidingDoorParser;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviews;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.util.animation.AnimationTimeline;
import com.creativemd.littletiles.common.util.animation.ValueTimeline;
import com.creativemd.littletiles.common.util.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.util.animation.event.ChildActivateEvent;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleDoorBase extends LittleDoor implements IAnimatedStructure {
	
	public LittleDoorBase(LittleStructureType type) {
		super(type);
	}
	
	public int interpolation = 0;
	public int duration = 50;
	public boolean stayAnimated = false;
	public List<AnimationEvent> events = new ArrayList<>();
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		events = new ArrayList<>();
		NBTTagList list = nbt.getTagList("events", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			AnimationEvent event = AnimationEvent.loadFromNBT(list.getCompoundTagAt(i));
			if (event != null)
				events.add(event);
		}
		if (nbt.hasKey("duration"))
			duration = nbt.getInteger("duration");
		else
			duration = 50;
		stayAnimated = nbt.getBoolean("stayAnimated");
		
		interpolation = nbt.getInteger("interpolation");
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
		nbt.setInteger("interpolation", interpolation);
	}
	
	public abstract LittleTransformation[] getDoorTransformations(@Nullable EntityPlayer player);
	
	public abstract void transformDoorPreview(LittleAbsolutePreviewsStructure previews, LittleTransformation transformation);
	
	public LittleAbsolutePreviewsStructure getDoorPreviews(LittleTransformation transformation) {
		LittleAbsolutePreviewsStructure previews = getAbsolutePreviewsSameWorldOnly(transformation.center);
		transformDoorPreview(previews, transformation);
		transformation.transform(previews);
		return previews;
	}
	
	@Override
	public void startAnimation(EntityAnimation animation) {
		for (int i = 0; i < events.size(); i++)
			events.get(i).reset();
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
	public void finishAnimation(EntityAnimation animation) {
		int duration = getCompleteDuration();
		for (AnimationEvent event : events)
			event.invert(this, duration);
		events.sort(null);
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
			if (this.children.size() > integer && this.children.get(integer) instanceof LittleDoor)
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
		
		LittleTransformation[] transformations = getDoorTransformations(player); // Only done if the door is placed down
		for (LittleTransformation transformation : transformations) {
			List<PlacePreview> placePreviews = new ArrayList<>();
			
			LittleAbsolutePreviewsStructure previews = getDoorPreviews(transformation);
			
			previews.getPlacePreviews(placePreviews, null, true, LittleVec.ZERO);
			
			HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos);
			if (LittleActionPlaceStack.canPlaceTiles(player, getWorld(), splitted, PlacementMode.all.getCoordsToCheck(splitted, previews.pos), PlacementMode.all, (LittleTile x) -> !x.isChildOfStructure(this), true)) {
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
	
	public EntityAnimation place(World world, @Nullable EntityPlayer player, LittleAbsolutePreviewsStructure previews, DoorController controller, UUID uuid, StructureAbsolute absolute, LittleTransformation transformation, boolean tickOnce) {
		List<PlacePreview> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleVec.ZERO);
		
		HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos);
		ArrayList<TileEntityLittleTiles> blocks = new ArrayList<>();
		SubWorld fakeWorld = SubWorld.createFakeWorld(world);
		if (world.isRemote)
			fakeWorld.renderChunkSupplier = new LittleRenderChunkSuppilier();
		
		fakeWorld.preventNeighborUpdate = true;
		
		LittlePlaceResult result = LittleActionPlaceStack.placeTilesWithoutPlayer(fakeWorld, previews.context, splitted, previews.getStructure(), PlacementMode.all, previews.pos, null, null, null, null);
		
		if (result == null)
			throw new RuntimeException("Something went wrong during placing the door!");
		
		controller.activator = player;
		
		if (world.isRemote)
			controller.markWaitingForApprove();
		
		fakeWorld.preventNeighborUpdate = false;
		
		LittleDoorBase newDoor = (LittleDoorBase) previews.getStructure();
		
		EntityAnimation animation = new EntityAnimation(world, fakeWorld, controller, previews.pos, uuid, absolute, newDoor.getAbsoluteIdentifier());
		
		// Move animated worlds
		newDoor.transferChildrenToAnimation(animation);
		newDoor.transformAnimation(transformation);
		
		if (parent != null) {
			LittleStructure parentStructure = parent.getStructure(world);
			parentStructure.updateChildConnection(parent.getChildID(), newDoor);
			newDoor.updateParentConnection(parent.getChildID(), parentStructure);
		}
		
		animation.controller.startTransition(DoorController.openedState);
		
		world.spawnEntity(animation);
		
		if (tickOnce)
			animation.onUpdateForReal();
		return animation;
	}
	
	@Override
	public boolean canOpenDoor(@Nullable EntityPlayer player, DoorOpeningResult result) {
		if (!super.canOpenDoor(player, result))
			return false;
		LittleTransformation transform;
		if (!result.isEmpty() && result.nbt.hasKey("transform"))
			transform = new LittleTransformation(result.nbt.getIntArray("transform"));
		else
			transform = getDoorTransformations(player)[0];
		
		List<PlacePreview> placePreviews = new ArrayList<>();
		
		LittleAbsolutePreviewsStructure previews = getDoorPreviews(transform);
		
		previews.getPlacePreviews(placePreviews, null, true, LittleVec.ZERO);
		
		HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos);
		return LittleActionPlaceStack.canPlaceTiles(player, getWorld(), splitted, PlacementMode.all.getCoordsToCheck(splitted, previews.pos), PlacementMode.all, (LittleTile x) -> !x.isChildOfStructure(this), true);
	}
	
	@Override
	public EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, DoorOpeningResult result, boolean tickOnce) {
		if (isAnimated()) {
			((DoorController) animation.controller).activate();
			if (tickOnce)
				animation.onUpdateForReal();
			return animation;
		}
		
		LittleTransformation transform;
		if (!result.isEmpty() && result.nbt.hasKey("transform"))
			transform = new LittleTransformation(result.nbt.getIntArray("transform"));
		else
			transform = getDoorTransformations(player)[0];
		
		LittleAbsolutePreviewsStructure previews = getDoorPreviews(transform);
		StructureAbsolute absolute = getAbsoluteAxis();
		
		HashMapList<BlockPos, LittleTile> allTilesFromWorld = collectBlockTilesChildren(new HashMapList<>(), true);
		
		EntityAnimation animation = place(getWorld(), player, previews, createController(result, uuid, previews, transform, getCompleteDuration()), uuid.next(), absolute, transform, tickOnce);
		
		World world = getWorld();
		boolean sendUpdate = !world.isRemote && world instanceof WorldServer;
		
		for (Entry<BlockPos, ArrayList<LittleTile>> entry : allTilesFromWorld.entrySet()) {
			try {
				TileEntityLittleTiles te = loadTE(entry.getKey());
				te.updateTiles((x) -> x.removeAll(entry.getValue()));
				
				if (sendUpdate)
					((WorldServer) world).getPlayerChunkMap().markBlockForUpdate(te.getPos());
			} catch (MissingTileEntity e) {
				e.printStackTrace();
			}
		}
		
		return animation;
	}
	
	public abstract DoorController createController(DoorOpeningResult result, UUIDSupplier supplier, LittleAbsolutePreviewsStructure previews, LittleTransformation transformation, int completeDuration);
	
	public abstract StructureAbsolute getAbsoluteAxis();
	
	public EntityAnimation animation;
	
	@Override
	public void setAnimation(EntityAnimation animation) {
		this.animation = animation;
	}
	
	@Override
	public boolean isInMotion() {
		if (animation != null && animation.controller.isChanging())
			return true;
		return false;
	}
	
	@Override
	public boolean isAnimated() {
		return animation != null;
	}
	
	@Override
	public EntityAnimation getAnimation() {
		return animation;
	}
	
	@Override
	public void destroyAnimation() {
		animation.markRemoved();
	}
	
	@Override
	protected void fillActivateChildren(BitSet set) {
		for (AnimationEvent event : events)
			if (event instanceof ChildActivateEvent)
				set.set(((ChildActivateEvent) event).childId);
	}
	
	public static void initDoors() {
		LittleStructureRegistry.registerStructureType("door", "door", LittleAxisDoor.class, LittleStructureAttribute.NONE, LittleAxisDoorParser.class);
		LittleStructureRegistry.registerStructureType("slidingDoor", "door", LittleSlidingDoor.class, LittleStructureAttribute.NONE, LittleSlidingDoorParser.class);
		LittleStructureRegistry.registerStructureType("advancedDoor", "door", LittleAdvancedDoor.class, LittleStructureAttribute.NONE, LittleAdvancedDoorParser.class);
		LittleStructureRegistry.registerStructureType("doorActivator", "door", LittleDoorActivator.class, LittleStructureAttribute.NONE, LittleDoorActivatorParser.class);
	}
	
	public static abstract class LittleDoorBaseParser extends LittleStructureGuiParser {
		
		public LittleDoorBaseParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@SideOnly(Side.CLIENT)
		@CustomEventSubscribe
		public void onChanged(GuiControlChangedEvent event) {
			if (event.source.is("duration_s") || event.source.is("children_activate") || event.source.is("interpolation"))
				updateTimeline();
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			parent.controls.add(new GuiCheckBox("stayAnimated", CoreControl.translate("gui.door.stayAnimated"), 0, 123, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).stayAnimated : false).setCustomTooltip(CoreControl.translate("gui.door.stayAnimatedTooltip")));
			parent.controls.add(new GuiLabel(CoreControl.translate("gui.door.duration") + ":", 90, 122));
			parent.controls.add(new GuiSteppedSlider("duration_s", 140, 122, 50, 6, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).duration : 50, 1, 500));
			parent.controls.add(new GuiCheckBox("rightclick", CoreControl.translate("gui.door.rightclick"), 105, 93, structure instanceof LittleDoor ? !((LittleDoor) structure).disableRightClick : true));
			parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
			parent.controls.add(new GuiStateButton("interpolation", structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).interpolation : 0, 140, 107, 40, 7, ValueTimeline.interpolationTypes));
			
			updateTimeline();
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleDoorBase parseStructure(LittlePreviews previews) {
			GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
			GuiCheckBox stayAnimated = (GuiCheckBox) parent.get("stayAnimated");
			GuiDoorEventsButton button = (GuiDoorEventsButton) parent.get("children_activate");
			GuiCheckBox rightclick = (GuiCheckBox) parent.get("rightclick");
			GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
			
			int duration = (int) slider.value;
			LittleDoorBase door = parseStructure();
			door.duration = duration;
			door.stayAnimated = stayAnimated.value;
			door.disableRightClick = !rightclick.value;
			door.events = button.events;
			door.interpolation = interpolationButton.getState();
			
			return door;
		}
		
		@SideOnly(Side.CLIENT)
		public abstract LittleDoorBase parseStructure();
		
		@SideOnly(Side.CLIENT)
		public abstract void populateTimeline(AnimationTimeline timeline, int interpolation);
		
		public void updateTimeline() {
			GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
			AnimationTimeline timeline = new AnimationTimeline((int) slider.value, new PairList<>());
			GuiDoorEventsButton children = (GuiDoorEventsButton) parent.get("children_activate");
			GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
			
			populateTimeline(timeline, interpolationButton.getState());
			handler.setTimeline(timeline, children.events);
		}
		
	}
}
