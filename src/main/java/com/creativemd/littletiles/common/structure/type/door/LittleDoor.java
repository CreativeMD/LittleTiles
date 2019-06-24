package com.creativemd.littletiles.common.structure.type.door;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.packet.LittleDoorPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		activateParent = nbt.getBoolean("activateParent");
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		if (activateParent)
			nbt.setBoolean("activateParent", activateParent);
	}
	
	public boolean activate(@Nullable EntityPlayer player, BlockPos pos, @Nullable LittleTile tile) {
		if (!hasLoaded()) {
			player.sendStatusMessage(new TextComponentTranslation("exception.door.notloaded"), true);
			return false;
		}
		
		if (!loadChildren()) {
			player.sendStatusMessage(new TextComponentTranslation("exception.door.brokenparent"), true);
			return false;
		}
		
		if (!loadParent()) {
			player.sendStatusMessage(new TextComponentTranslation("exception.door.brokenchild"), true);
			return false;
		}
		
		if (activateParent) {
			LittleStructure parentStructure = parent.getStructureWithoutLoading();
			if (parentStructure instanceof LittleDoor)
				return ((LittleDoor) parentStructure).activate(player, pos, null);
			return false;
		}
		
		DoorOpeningResult result = canOpenDoor(player);
		if (result == null)
			return false;
		
		UUIDSupplier uuid = new UUIDSupplier();
		
		if (getWorld().isRemote)
			PacketHandler.sendPacketToServer(new LittleDoorPacket(getMainTile(), uuid.uuid, result));
		
		openDoor(player, uuid, result);
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (world.isRemote) {
			activate(player, pos, tile);
			action.preventInteraction = true;
		}
		return true;
	}
	
	public void beforeTick(EntityAnimation animation, int tick) {
		
	}
	
	public void afterTick(EntityAnimation animation, int tick) {
		
	}
	
	public abstract int getCompleteDuration();
	
	public abstract List<LittleDoor> collectDoorsToCheck();
	
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
	
	public abstract EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, DoorOpeningResult result);
	
	public EntityAnimation animation;
	
	public void setAnimation(EntityAnimation animation) {
		this.animation = animation;
	}
	
	public boolean isInMotion() {
		if (animation != null && animation.controller.isChanging())
			return true;
		return false;
	}
	
	public boolean isAnimated() {
		return animation != null;
	}
	
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
}
