package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.client.render.world.RenderUploader;
import com.creativemd.littletiles.client.render.world.RenderUtils;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack.LittlePlaceResult;
import com.creativemd.littletiles.common.packet.LittlePlacedAnimationPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationController;
import com.creativemd.littletiles.common.structure.animation.AnimationState;
import com.creativemd.littletiles.common.structure.animation.AnimationTimeline;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor.DoorOpeningResult;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class DoorController extends EntityAnimationController {
	
	protected boolean placedOnServer = false;
	protected boolean isWaitingForApprove = false;
	protected int ticksToWait = -1;
	protected static final int waitTimeApprove = 300;
	protected Boolean placed = null;
	
	public static final String openedState = "opened";
	public static final String closedState = "closed";
	public Boolean turnBack;
	public int duration;
	public int completeDuration;
	public int interpolation;
	public EntityPlayer activator;
	public DoorOpeningResult result;
	public UUIDSupplier supplier;
	
	protected boolean modifiedTransition;
	
	public DoorController() {
		
	}
	
	public DoorController(DoorOpeningResult result, UUIDSupplier supplier, AnimationState closed, AnimationState opened, Boolean turnBack, int duration, int completeDuration, int interpolation) {
		this.result = result;
		this.supplier = supplier;
		
		this.turnBack = turnBack;
		this.duration = duration;
		this.completeDuration = completeDuration;
		
		this.interpolation = interpolation;
		
		addState(openedState, opened);
		addStateAndSelect(closedState, closed);
		
		generateAllTransistions(duration);
		modifiedTransition = false;
		
		stretchTransitions();
	}
	
	public DoorController(DoorOpeningResult result, UUIDSupplier supplier, AnimationState closed, AnimationState opened, Boolean turnBack, int duration, int completeDuration, AnimationTimeline open, AnimationTimeline close, int interpolation) {
		this.result = result;
		this.supplier = supplier;
		
		this.turnBack = turnBack;
		this.duration = duration;
		this.completeDuration = completeDuration;
		
		this.interpolation = interpolation;
		
		addState(openedState, opened);
		addStateAndSelect(closedState, closed);
		
		addTransition("closed", "opened", open);
		addTransition("opened", "closed", close);
		
		stretchTransitions();
	}
	
	@Override
	public AnimationController addTransition(String from, String to, AnimationTimeline animation) {
		modifiedTransition = true;
		return super.addTransition(from, to, animation);
	}
	
	public DoorController(DoorOpeningResult result, UUIDSupplier supplier, AnimationState opened, Boolean turnBack, int duration, int completeDuration, int interpolation) {
		this(result, supplier, new AnimationState(), opened, turnBack, duration, completeDuration, interpolation);
	}
	
	protected void stretchTransitions() {
		completeDuration = Math.max(completeDuration, duration);
		for (AnimationTimeline timeline : stateTransition.values()) {
			timeline.duration = completeDuration;
		}
	}
	
	@Override
	public int getInterpolationType() {
		return interpolation;
	}
	
	@Override
	public EntityPlayer activator() {
		return activator;
	}
	
	public void markWaitingForApprove() {
		isWaitingForApprove = true;
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
		((LittleDoor) parent.structure).startAnimation(parent);
	}
	
	@Override
	public void endTransition() {
		super.endTransition();
		((LittleDoor) parent.structure).finishAnimation(parent);
		if (turnBack != null && turnBack == currentState.name.equals(openedState)) {
			if (isWaitingForApprove)
				placed = false;
			else
				place();
		}
	}
	
	public void place() {
		if (!parent.structure.load()) {
			System.out.println(new TextComponentTranslation("exception.door.notloaded").getFormattedText());
			return;
		}
		
		if (!parent.structure.loadChildren()) {
			System.out.println(new TextComponentTranslation("exception.door.brokenchild").getFormattedText());
			return;
		}
		
		if (!parent.structure.loadParent()) {
			System.out.println(new TextComponentTranslation("exception.door.brokenparent").getFormattedText());
			return;
		}
		
		World world = parent.world;
		LittleAbsolutePreviewsStructure previews = parent.structure.getAbsolutePreviewsSameWorldOnly(parent.absolutePreviewPos);
		
		List<PlacePreview> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleVec.ZERO);
		
		LittleStructure newDoor = previews.getStructure();
		if (!(world instanceof CreativeWorld) && world.isRemote && !placedOnServer)
			((LittleDoor) newDoor).waitingForApproval = true;
		LittlePlaceResult result;
		if ((result = LittleActionPlaceStack.placeTilesWithoutPlayer(world, previews.context, placePreviews, previews.getStructure(), PlacementMode.all, previews.pos, null, null, null, EnumFacing.EAST)) != null) {
			
			if (parent.structure.parent != null) {
				LittleStructure parentStructure = parent.structure.parent.getStructure(world);
				newDoor.updateParentConnection(parent.structure.parent.getChildID(), parentStructure);
				parentStructure.updateChildConnection(parent.structure.parent.getChildID(), newDoor);
			}
			
			newDoor.transferChildrenFromAnimation(parent);
		} else {
			parent.markRemoved();
			if (!world.isRemote)
				WorldUtils.dropItem(world, parent.structure.getStructureDrop(), parent.center.baseOffset);
			return;
		}
		
		parent.markRemoved();
		
		if (!world.isRemote) {
			WorldServer serverWorld = (WorldServer) (world instanceof IOrientatedWorld ? ((IOrientatedWorld) world).getRealWorld() : world);
			PacketHandler.sendPacketToTrackingPlayers(new LittlePlacedAnimationPacket(newDoor.getMainTile(), parent.getUniqueID()), parent.getAbsoluteParent(), serverWorld, null);
		} else {
			boolean subWorld = world instanceof IOrientatedWorld;
			HashMapList<RenderChunk, TileEntityLittleTiles> chunks = subWorld ? null : new HashMapList<>();
			for (TileEntityLittleTiles te : result.tileEntities) {
				TileEntity oldTE = parent.fakeWorld.getTileEntity(te.getPos());
				if (oldTE instanceof TileEntityLittleTiles && ((TileEntityLittleTiles) oldTE).buffer != null) {
					synchronized (te.inRenderingQueue) {
						if (te.inRenderingQueue.get() || te.buffer.isEmpty()) {
							if (te.buffer == null)
								te.buffer = ((TileEntityLittleTiles) oldTE).buffer;
							else
								te.buffer.combine(((TileEntityLittleTiles) oldTE).buffer);
						}
						
						if (subWorld)
							RenderUtils.getRenderChunk((IOrientatedWorld) te.getWorld(), te.getPos()).addRenderData(te);
						else
							chunks.add(RenderUtils.getRenderChunk(RenderUtils.getViewFrustum(), te.getPos()), (TileEntityLittleTiles) oldTE);
					}
				}
			}
			
			if (!subWorld)
				for (Entry<RenderChunk, ArrayList<TileEntityLittleTiles>> entry : chunks.entrySet())
					RenderUploader.uploadRenderData(entry.getKey(), entry.getValue());
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
		
		nbt.setInteger("interpolation", interpolation);
		
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
		interpolation = nbt.getInteger("interpolation");
		
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
	public void onServerPlaces() {
		placedOnServer = true;
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
