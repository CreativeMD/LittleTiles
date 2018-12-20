package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.StructureLink;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DoorController extends EntityAnimationController {
	
	protected boolean isWaitingForApprove = false;
	protected int ticksToWait = -1;
	protected static final int waitTimeApprove = 300;
	protected static final int waitTimeRender = 200;
	protected Boolean placed = null;
	
	public String openedState;
	public String closedState;
	public Boolean turnBack;
	public long duration;
	public EntityPlayer activator;
	
	@SideOnly(Side.CLIENT)
	List<TileEntityLittleTiles> waitingForRender;
	
	public DoorController() {
		
	}
	
	public DoorController(AnimationState closed, AnimationState opened, Boolean turnBack, long duration) {
		this.openedState = opened != null ? opened.name : null;
		this.closedState = closed.name;
		this.turnBack = turnBack;
		this.duration = duration;
		
		addState(opened);
		addStateAndSelect(closed);
		generateAllTransistions(duration);
		
		startTransition(openedState);
	}
	
	public DoorController(AnimationState opened, Boolean turnBack, long duration) {
		this(new AnimationState("closed", null, null), opened, turnBack, duration);
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
		waitingForRender.remove(te);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isWaitingForRender() {
		return waitingForRender != null;
	}
	
	@Override
	public boolean onRightClick() {
		if (placed != null)
			return false;
		
		boolean isOpen = currentState.name.equals(openedState);
		if (!isChanging() && isOpen ? closedState != null : openedState != null) {
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
				
				if (ticksToWait % 10 == 0) {
					List<TileEntityLittleTiles> tileEntities = null;
					for (Iterator iterator = waitingForRender.iterator(); iterator.hasNext();) {
						TileEntityLittleTiles te = (TileEntityLittleTiles) iterator.next();
						if (te != te.getWorld().getTileEntity(te.getPos())) {
							if (tileEntities == null)
								tileEntities = new ArrayList<>();
							tileEntities.add(te);
						}
					}
					if (tileEntities != null)
						waitingForRender.removeAll(tileEntities);
				}
				
				if (waitingForRender.size() == 0 || ticksToWait < 0) {
					parent.unloadRenderCache();
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
		return super.tick();
	}
	
	@Override
	public void endTransition() {
		super.endTransition();
		if (turnBack != null && turnBack == currentState.name.equals(openedState)) {
			if (isWaitingForApprove)
				placed = false;
			else
				place();
		}
	}
	
	public void place() {
		
		LittleStructure placedStructureParent = parent.getParentStructure();
		LittleAbsolutePreviewsStructure previews = parent.getAbsolutePreviews(placedStructureParent);
		
		List<PlacePreviewTile> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
		
		LittleStructure newDoor = previews.getStructure();
		World world = parent.world;
		
		if (LittleActionPlaceStack.placeTilesWithoutPlayer(world, previews.context, placePreviews, previews.getStructure(), PlacementMode.all, previews.pos, null, null, null, EnumFacing.EAST) != null) {
			if (placedStructureParent.parent != null && placedStructureParent.parent.isConnected(world)) {
				LittleStructure parentStructure = placedStructureParent.parent.getStructureWithoutLoading();
				newDoor.parent = new StructureLink(newDoor.getMainTile().te, parentStructure.getMainTile().te.getPos(), parentStructure.getMainTile().getContext(), parentStructure.getMainTile().getIdentifier(), parentStructure.attribute, newDoor, placedStructureParent.parent.getChildID(), true);
				parentStructure.children.put(newDoor.parent.getChildID(), new StructureLink(parentStructure.getMainTile().te, newDoor.getMainTile().te.getPos(), newDoor.getMainTile().getContext(), newDoor.getMainTile().getIdentifier(), newDoor.attribute, parentStructure, newDoor.parent.getChildID(), false));
			}
		} else {
			parent.isDead = true;
			if (!world.isRemote)
				WorldUtils.dropItem(world, placedStructureParent.getStructureDrop(), parent.baseOffset);
			return;
		}
		
		if (!world.isRemote)
			parent.isDead = true;
		else {
			waitingForRender = new CopyOnWriteArrayList<>();
			ArrayList<BlockPos> coordsToCheck = new ArrayList<>(LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos).keySet());
			for (int i = 0; i < coordsToCheck.size(); i++) {
				TileEntity te = world.getTileEntity(coordsToCheck.get(i));
				if (te instanceof TileEntityLittleTiles) {
					((TileEntityLittleTiles) te).waitingAnimation = parent;
					waitingForRender.add((TileEntityLittleTiles) te);
				}
			}
			ticksToWait = waitTimeRender;
			parent.isDead = false;
			placed = true;
		}
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setTag("closed", getState(closedState).writeToNBT(new NBTTagCompound()));
		nbt.setTag("opened", getState(openedState).writeToNBT(new NBTTagCompound()));
		
		nbt.setBoolean("isOpen", currentState.name.equals(openedState));
		if (isChanging())
			nbt.setLong("tick", animation.progress());
		
		nbt.setLong("duration", duration);
		nbt.setByte("turnBack", (byte) (turnBack == null ? 0 : (turnBack ? 1 : -1)));
	}
	
	@Override
	protected void readFromNBT(NBTTagCompound nbt) {
		AnimationState closed = new AnimationState(nbt.getCompoundTag("closed"));
		closedState = closed.name;
		addState(closed);
		
		AnimationState opened = new AnimationState(nbt.getCompoundTag("opened"));
		openedState = opened.name;
		addState(opened);
		
		duration = nbt.getLong("duration");
		
		generateAllTransistions(duration);
		
		boolean isOpen = nbt.getBoolean("isOpen");
		if (!isOpen)
			currentState = closed;
		else
			currentState = opened;
		
		if (nbt.hasKey("tick")) {
			startTransition(isOpen ? closedState : openedState);
			animation.setProgress(nbt.getLong("tick"));
		}
		
		byte turnBackData = nbt.getByte("turnBack");
		turnBack = turnBackData == 0 ? null : (turnBackData > 0 ? true : false);
		
	}
	
	@Override
	public void onServerApproves() {
		isWaitingForApprove = false;
	}
	
}
