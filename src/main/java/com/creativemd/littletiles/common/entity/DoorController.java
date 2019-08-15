package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack.LittlePlaceResult;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor.DoorOpeningResult;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.animation.AnimationController;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.animation.AnimationTimeline;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.vec.LittleTransformation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DoorController extends EntityAnimationController {
	
	protected boolean isWaitingForApprove = false;
	protected int ticksToWait = -1;
	protected static final int waitTimeApprove = 300;
	protected static final int waitTimeRender = 200;
	protected Boolean placed = null;
	
	public static final String openedState = "opened";
	public static final String closedState = "closed";
	public Boolean turnBack;
	public int duration;
	public int completeDuration;
	public EntityPlayer activator;
	public DoorOpeningResult result;
	public UUIDSupplier supplier;
	
	protected boolean modifiedTransition;
	
	@SideOnly(Side.CLIENT)
	List<TileEntityLittleTiles> waitingForRender;
	
	public DoorController() {
		
	}
	
	public DoorController(DoorOpeningResult result, UUIDSupplier supplier, AnimationState closed, AnimationState opened, Boolean turnBack, int duration, int completeDuration) {
		this.result = result;
		this.supplier = supplier;
		
		this.turnBack = turnBack;
		this.duration = duration;
		this.completeDuration = completeDuration;
		
		addState(openedState, opened);
		addStateAndSelect(closedState, closed);
		
		generateAllTransistions(duration);
		modifiedTransition = false;
		
		stretchTransitions();
		startTransition(openedState);
	}
	
	public DoorController(DoorOpeningResult result, UUIDSupplier supplier, AnimationState closed, AnimationState opened, Boolean turnBack, int duration, int completeDuration, AnimationTimeline open, AnimationTimeline close) {
		this.result = result;
		this.supplier = supplier;
		
		this.turnBack = turnBack;
		this.duration = duration;
		this.completeDuration = completeDuration;
		
		addState(openedState, opened);
		addStateAndSelect(closedState, closed);
		
		addTransition("closed", "opened", open);
		addTransition("opened", "closed", close);
		
		stretchTransitions();
		startTransition(openedState);
	}
	
	@Override
	public AnimationController addTransition(String from, String to, AnimationTimeline animation) {
		modifiedTransition = true;
		return super.addTransition(from, to, animation);
	}
	
	public DoorController(DoorOpeningResult result, UUIDSupplier supplier, AnimationState opened, Boolean turnBack, int duration, int completeDuration) {
		this(result, supplier, new AnimationState(), opened, turnBack, duration, completeDuration);
	}
	
	protected void stretchTransitions() {
		completeDuration = Math.max(completeDuration, duration);
		for (AnimationTimeline timeline : stateTransition.values()) {
			timeline.duration = completeDuration;
		}
	}
	
	@Override
	public EntityPlayer activator() {
		return activator;
	}
	
	public void markWaitingForApprove() {
		isWaitingForApprove = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void removeWaitingTe(TileEntityLittleTiles te) {
		if (!isWaitingForRender())
			return;
		synchronized (waitingForRender) {
			waitingForRender.remove(te);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isWaitingForRender() {
		return waitingForRender != null;
	}
	
	public boolean activate() {
		if (placed != null)
			return false;
		
		boolean isOpen = currentState.name.equals(openedState);
		if (!isChanging()) {
			startTransition(isOpen ? closedState : openedState);
			return true;
		}
		return false;
	}
	
	@Override
	public AnimationState tick() {
		if (parent.world.isRemote && placed != null) {
			if (placed) {
				ticksToWait--;
				
				/*if (ticksToWait % 10 == 0) {
					synchronized (waitingForRender) {
						for (Iterator iterator = waitingForRender.iterator(); iterator.hasNext();) {
							TileEntityLittleTiles te = (TileEntityLittleTiles) iterator.next();
							if (te != te.getWorld().getTileEntity(te.getPos())) {
								iterator.remove();
							}
						}
					}
				}*/
				
				if (waitingForRender.size() == 0 || ticksToWait < 0) {
					parent.getRenderChunkSuppilier().unloadRenderCache();
					parent.isDead = true;
				} else
					parent.isDead = false;
				
			} else {
				if (isWaitingForApprove) {
					if (ticksToWait < 0)
						ticksToWait = waitTimeApprove;
					else if (ticksToWait == 0)
						parent.isDead = true;
					else
						ticksToWait--;
				} else
					place();
			}
		}
		
		if (isChanging()) {
			if (!parent.structure.loadChildren())
				return currentState.state;
			
			((LittleDoor) parent.structure).beforeTick(parent, tick);
			AnimationState state = super.tick();
			((LittleDoor) parent.structure).afterTick(parent, tick);
			return state;
		} else
			return super.tick();
	}
	
	@Override
	public void startTransition(String key) {
		super.startTransition(key);
	}
	
	@Override
	public void endTransition() {
		super.endTransition();
		((LittleDoor) parent.structure).onFinished(parent);
		if (turnBack != null && turnBack == currentState.name.equals(openedState)) {
			if (isWaitingForApprove)
				placed = false;
			else
				place();
		}
	}
	
	public void place() {
		if (!parent.structure.hasLoaded()) {
			System.out.println(new TextComponentTranslation("exception.door.notloaded").getFormattedText());
			return;
		}
		
		if (!parent.structure.loadChildren()) {
			System.out.println(new TextComponentTranslation("exception.door.brokenparent").getFormattedText());
			return;
		}
		
		if (!parent.structure.loadParent()) {
			System.out.println(new TextComponentTranslation("exception.door.brokenchild").getFormattedText());
			return;
		}
		
		World world = parent.world;
		LittleAbsolutePreviewsStructure previews = parent.structure.getAbsolutePreviewsSameWorldOnly(parent.absolutePreviewPos);
		
		List<PlacePreviewTile> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
		
		LittleStructure newDoor = previews.getStructure();
		
		LittlePlaceResult result;
		if ((result = LittleActionPlaceStack.placeTilesWithoutPlayer(world, previews.context, placePreviews, previews.getStructure(), PlacementMode.all, previews.pos, null, null, null, EnumFacing.EAST)) != null) {
			if (parent.structure.parent != null && parent.structure.parent.isConnected(world)) {
				LittleStructure parentStructure = parent.structure.parent.getStructureWithoutLoading();
				newDoor.updateParentConnection(parent.structure.parent.getChildID(), parentStructure);
				parentStructure.updateChildConnection(parent.structure.parent.getChildID(), newDoor);
			}
			
			newDoor.transferChildrenFromAnimation(parent);
		} else {
			parent.isDead = true;
			if (!world.isRemote)
				WorldUtils.dropItem(world, parent.structure.getStructureDrop(), parent.center.baseOffset);
			return;
		}
		
		if (!world.isRemote)
			parent.isDead = true;
		else {
			waitingForRender = new ArrayList<>();
			synchronized (waitingForRender) {
				for (TileEntityLittleTiles te : result.tileEntities) {
					te.addWaitingAnimation(parent);
					waitingForRender.add(te);
				}
			}
			ticksToWait = waitTimeRender;
			parent.isDead = false;
			placed = true;
		}
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setTag("closed", getState(closedState).state.writeToNBT(new NBTTagCompound()));
		nbt.setTag("opened", getState(openedState).state.writeToNBT(new NBTTagCompound()));
		
		if (!result.isEmpty())
			nbt.setTag("result", result.nbt);
		nbt.setString("originaluuid", supplier.original().toString());
		nbt.setString("uuid", supplier.uuid.toString());
		
		nbt.setBoolean("isOpen", currentState.name.equals(openedState));
		if (isChanging())
			nbt.setInteger("tick", this.tick);
		
		nbt.setInteger("duration", duration);
		nbt.setInteger("completeDuration", completeDuration);
		nbt.setByte("turnBack", (byte) (turnBack == null ? 0 : (turnBack ? 1 : -1)));
		
		if (modifiedTransition) {
			NBTTagList list = new NBTTagList();
			for (Entry<String, AnimationTimeline> entry : stateTransition.entrySet()) {
				NBTTagCompound transitionNBT = entry.getValue().writeToNBT(new NBTTagCompound());
				transitionNBT.setString("key", entry.getKey());
				list.appendTag(transitionNBT);
			}
			nbt.setTag("transitions", list);
		}
	}
	
	@Override
	protected void readFromNBT(NBTTagCompound nbt) {
		addState(closedState, new AnimationState(nbt.getCompoundTag("closed")));
		addState(openedState, new AnimationState(nbt.getCompoundTag("opened")));
		
		if (nbt.hasKey("result"))
			result = new DoorOpeningResult(nbt.getCompoundTag("result"));
		else
			result = LittleDoor.EMPTY_OPENING_RESULT;
		supplier = new UUIDSupplier(UUID.fromString(nbt.getString("originaluuid")), UUID.fromString(nbt.getString("uuid")));
		
		duration = nbt.getInteger("duration");
		completeDuration = nbt.getInteger("completeDuration");
		if (nbt.hasKey("transitions")) {
			NBTTagList list = nbt.getTagList("transitions", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound transitionNBT = list.getCompoundTagAt(i);
				addTransition(transitionNBT.getString("key"), new AnimationTimeline(transitionNBT));
			}
			modifiedTransition = true;
		} else {
			generateAllTransistions(duration);
			stretchTransitions();
		}
		
		boolean isOpen = nbt.getBoolean("isOpen");
		if (isOpen)
			currentState = getState(openedState);
		else
			currentState = getState(closedState);
		
		if (nbt.hasKey("tick")) {
			startTransition(isOpen ? closedState : openedState);
			this.tick = nbt.getInteger("tick");
		}
		
		byte turnBackData = nbt.getByte("turnBack");
		turnBack = turnBackData == 0 ? null : (turnBackData > 0 ? true : false);
	}
	
	@Override
	public void onServerApproves() {
		isWaitingForApprove = false;
	}
	
	@Override
	public void transform(LittleTransformation transformation) {
		for (AnimationControllerState state : states.values())
			state.transform(transformation);
		for (AnimationTimeline timeline : stateTransition.values()) {
			if (transformation.rotX != 0) {
				Rotation rotation = transformation.getRotation(Axis.X);
				for (int i = 0; i < Math.abs(transformation.rotX); i++)
					timeline.transform(rotation);
			}
			if (transformation.rotY != 0) {
				Rotation rotation = transformation.getRotation(Axis.Y);
				for (int i = 0; i < Math.abs(transformation.rotY); i++)
					timeline.transform(rotation);
			}
			if (transformation.rotZ != 0) {
				Rotation rotation = transformation.getRotation(Axis.Z);
				for (int i = 0; i < Math.abs(transformation.rotZ); i++)
					timeline.transform(rotation);
			}
		}
		
		if (tickingState != null)
			tickingState.clear();
		else if (isChanging())
			tickingState = new AnimationState();
		if (isChanging())
			animation.tick(tick, tickingState);
	}
	
}
