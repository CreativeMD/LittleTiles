package com.creativemd.littletiles.common.structure.type.door;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionException.LittleActionExceptionHidden;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.packet.LittleActivateDoorPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.structure.animation.event.ChildActivateEvent;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorBase.LittleDoorBaseType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public abstract class LittleDoor extends LittleStructure {
	
	public LittleDoor(LittleStructureType type) {
		super(type);
	}
	
	public boolean activateParent = false;
	public boolean waitingForApproval = false;
	public boolean disableRightClick = false;
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		activateParent = nbt.getBoolean("activateParent");
		disableRightClick = nbt.getBoolean("disableRightClick");
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setBoolean("activateParent", activateParent);
		nbt.setBoolean("disableRightClick", disableRightClick);
	}
	
	public DoorActivationResult activate(@Nullable EntityPlayer player, @Nullable LittleTile tile, @Nullable UUID uuid, boolean sendUpdate) throws LittleActionException {
		if (waitingForApproval)
			throw new LittleActionExceptionHidden("Door has not been approved yet!");
		
		if (player != null && disableRightClick)
			throw new LittleActionExceptionHidden("Door is locked!");
		
		if (!load()) {
			if (player != null)
				player.sendStatusMessage(new TextComponentTranslation("exception.door.notloaded"), true);
			return null;
		}
		
		if (!loadChildren()) {
			if (player != null)
				player.sendStatusMessage(new TextComponentTranslation("exception.door.brokenchild"), true);
			return null;
		}
		
		if (!loadParent()) {
			if (player != null)
				player.sendStatusMessage(new TextComponentTranslation("exception.door.brokenparent"), true);
			return null;
		}
		
		if (activateParent && parent != null) {
			LittleStructure parentStructure = parent.getStructureWithoutLoading();
			if (parentStructure instanceof LittleDoor)
				return ((LittleDoor) parentStructure).activate(player, null, uuid, sendUpdate);
			return null;
		}
		
		if (isInMotion())
			return null;
		
		DoorOpeningResult result = canOpenDoor(player);
		if (result == null) {
			if (player != null)
				player.sendStatusMessage(new TextComponentTranslation("exception.door.notenoughspace"), true);
			return null;
		}
		
		if (uuid == null)
			uuid = UUID.randomUUID();
		
		if (sendUpdate) {
			if (getWorld().isRemote)
				sendActivationToServer(player, uuid, result);
			else
				sendActivationToClient(player, uuid, result);
		}
		
		EntityAnimation animation = openDoor(player, new UUIDSupplier(uuid), result, false);
		
		return new DoorActivationResult(animation, result);
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
		if (world.isRemote) {
			activate(player, tile, null, true);
			action.preventInteraction = true;
		}
		return true;
	}
	
	public void startAnimation(EntityAnimation animation) {
		
	}
	
	public void beforeTick(EntityAnimation animation, int tick) {
		
	}
	
	public void afterTick(EntityAnimation animation, int tick) {
		
	}
	
	public void finishAnimation(EntityAnimation animation) {
		
	}
	
	public void sendActivationToServer(EntityPlayer activator, UUID uuid, DoorOpeningResult result) {
		PacketHandler.sendPacketToServer(new LittleActivateDoorPacket(getMainTile(), uuid, result));
	}
	
	public void sendActivationToClient(EntityPlayer activator, UUID uuid, DoorOpeningResult result) {
		PacketHandler.sendPacketToTrackingPlayers(new LittleActivateDoorPacket(getMainTile(), uuid, result), this.getWorld(), this.getMainTile().getBlockPos(), activator != null ? (x) -> x != activator : null);
	}
	
	public abstract int getCompleteDuration();
	
	public abstract List<LittleDoor> collectDoorsToCheck();
	
	public abstract boolean isInMotion();
	
	public DoorOpeningResult canOpenDoor(@Nullable EntityPlayer player) {
		if (isInMotion())
			return null;
		
		NBTTagCompound nbt = null;
		
		for (LittleDoor door : collectDoorsToCheck()) {
			
			DoorOpeningResult subResult = door.canOpenDoor(player);
			
			if (subResult == null)
				return null;
			
			if (!subResult.isEmpty()) {
				if (nbt == null)
					nbt = new NBTTagCompound();
				nbt.setTag("e" + door.parent.getChildID(), subResult.nbt);
			}
		}
		
		if (nbt == null)
			return EMPTY_OPENING_RESULT;
		return new DoorOpeningResult(nbt);
	}
	
	public boolean canOpenDoor(@Nullable EntityPlayer player, DoorOpeningResult result) {
		if (isInMotion())
			return false;
		
		for (LittleDoor door : collectDoorsToCheck())
			if (!door.canOpenDoor(player, result))
				return false;
		return true;
	}
	
	public LittleDoor getParentDoor() {
		if (activateParent && parent != null)
			return ((LittleDoor) parent.getStructure(getWorld())).getParentDoor();
		return this;
	}
	
	public abstract EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, DoorOpeningResult result, boolean tickOnce);
	
	public static final DoorOpeningResult EMPTY_OPENING_RESULT = new DoorOpeningResult(null);
	
	public static class DoorOpeningResult {
		
		public final NBTTagCompound nbt;
		
		public DoorOpeningResult(NBTTagCompound nbt) {
			this.nbt = nbt;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof DoorOpeningResult)
				return nbt != null ? nbt.equals(((DoorOpeningResult) obj).nbt) : ((DoorOpeningResult) obj).nbt == null;
			return false;
		}
		
		public boolean isEmpty() {
			return nbt == null;
		}
		
		@Override
		public int hashCode() {
			return nbt != null ? nbt.hashCode() : super.hashCode();
		}
		
		@Override
		public String toString() {
			return "" + nbt;
		}
	}
	
	public static class LittleDoorType extends LittleDoorBaseType {
		
		public LittleDoorType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
			super(id, category, structureClass, attribute);
		}
		
		@Override
		public void setBit(LittlePreviews previews, BitSet set) {
			NBTTagList list = previews.structure.getTagList("events", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				AnimationEvent event = AnimationEvent.loadFromNBT(list.getCompoundTagAt(i));
				if (event instanceof ChildActivateEvent)
					set.set(((ChildActivateEvent) event).childId);
			}
		}
		
	}
	
	public static class DoorActivationResult {
		
		public final DoorOpeningResult result;
		public final EntityAnimation animation;
		
		public DoorActivationResult(EntityAnimation animation, DoorOpeningResult result) {
			this.animation = animation;
			this.result = result;
		}
		
	}
	
}
