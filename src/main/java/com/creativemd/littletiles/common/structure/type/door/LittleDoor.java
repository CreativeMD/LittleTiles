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
import com.creativemd.littletiles.common.structure.IAnimatedStructure;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.structure.animation.event.ChildActivateEvent;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.output.InternalSignalOutput;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorBase.LittleDoorBaseType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

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
    
    public LittleDoor(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    public boolean activateParent = false;
    public boolean disableRightClick = false;
    public boolean opened = false;
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        activateParent = nbt.getBoolean("activateParent");
        disableRightClick = nbt.getBoolean("disableRightClick");
        opened = nbt.getBoolean("opened");
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setBoolean("activateParent", activateParent);
        nbt.setBoolean("disableRightClick", disableRightClick);
        nbt.setBoolean("opened", opened);
    }
    
    public EntityAnimation activate(DoorActivator activator, @Nullable EntityPlayer player, @Nullable UUID uuid) throws LittleActionException {
        if (mainBlock.isRemoved())
            throw new LittleActionException("Structure does not exist");
        
        if (activator == DoorActivator.RIGHTCLICK && disableRightClick)
            throw new LittleActionExceptionHidden("Door is locked!");
        
        load();
        
        if (activateParent && getParent() != null) {
            LittleStructure parentStructure = getParent().getStructure();
            if (parentStructure instanceof LittleDoor)
                return ((LittleDoor) parentStructure).activate(activator, player, uuid);
            throw new LittleActionException("Invalid parent");
        }
        
        if (isInMotion())
            throw new StillInMotionException();
        
        if (uuid == null)
            if (this instanceof IAnimatedStructure && ((IAnimatedStructure) this).isAnimated())
                uuid = ((IAnimatedStructure) this).getAnimation().getUniqueID();
            else
                uuid = UUID.randomUUID();
            
        if (getWorld().isRemote) {
            sendActivationToServer(activator, player, uuid);
            return null;
        }
        
        if (!canOpenDoor(player)) {
            if (player != null)
                player.sendStatusMessage(new TextComponentTranslation("exception.door.notenoughspace"), true);
            throw new LittleActionException("Cannot open door");
        }
        
        opened = !opened;
        if (activator != DoorActivator.SIGNAL && !getWorld().isRemote)
            getOutput(0).toggle();
        
        return openDoor(player, new UUIDSupplier(uuid), false);
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        activate(DoorActivator.RIGHTCLICK, playerIn, null);
        action.preventInteraction = true;
        return true;
    }
    
    public void startAnimation(EntityAnimation animation) {}
    
    public void beforeTick(EntityAnimation animation, int tick) {}
    
    public void afterTick(EntityAnimation animation, int tick) {}
    
    public void finishAnimation(EntityAnimation animation) {}
    
    public void sendActivationToServer(DoorActivator type, EntityPlayer activator, UUID uuid) {
        PacketHandler.sendPacketToServer(new LittleActivateDoorPacket(type, getStructureLocation(), uuid));
    }
    
    public abstract int getCompleteDuration();
    
    public abstract List<LittleDoor> collectDoorsToCheck();
    
    public abstract boolean isInMotion();
    
    public boolean canOpenDoor(@Nullable EntityPlayer player) {
        if (isInMotion())
            return false;
        
        for (BlockPos pos : positions())
            if (pos.getY() < 0 || pos.getY() >= 256)
                return false;
            
        for (LittleDoor door : collectDoorsToCheck())
            if (!door.canOpenDoor(player))
                return false;
            
        return true;
    }
    
    public LittleDoor getParentDoor() throws CorruptedConnectionException, NotYetConnectedException {
        if (activateParent && getParent() != null)
            return ((LittleDoor) getParent().getStructure()).getParentDoor();
        return this;
    }
    
    public abstract EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, boolean tickOnce) throws LittleActionException;
    
    public void onChildComplete(LittleDoor door, int childId) {}
    
    public void completeAnimation() {
        if (activateParent && getParent() != null) {
            try {
                LittleStructure parent = getParent().getStructure();
                if (parent instanceof LittleDoor)
                    ((LittleDoor) parent).onChildComplete(this, getParent().childId);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                e.printStackTrace();
            }
        }
        if (!mainBlock.isRemoved() && !isClient()) {
            getOutput(0).changed();
            notifyChange();
        }
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.identifier.equals("state"))
            if (opened != output.getState()[0] && !isInMotion())
                try {
                    activate(DoorActivator.SIGNAL, null, null);
                } catch (LittleActionException e) {}
    }
    
    @Override
    public NBTTagCompound writeToNBTPreview(NBTTagCompound nbt, BlockPos newCenter) {
        super.writeToNBTPreview(nbt, newCenter);
        NBTTagCompound stateNBT = nbt.getCompoundTag("state");
        stateNBT.setInteger("state", 0);
        return nbt;
    }
    
    public static class LittleDoorType extends LittleDoorBaseType {
        
        public LittleDoorType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
            super(id, category, structureClass, attribute);
        }
        
        @Override
        public void setBit(LittlePreviews previews, BitSet set) {
            NBTTagList list = previews.structureNBT.getTagList("events", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                AnimationEvent event = AnimationEvent.loadFromNBT(list.getCompoundTagAt(i));
                if (event instanceof ChildActivateEvent)
                    set.set(((ChildActivateEvent) event).childId);
            }
        }
        
    }
    
    public static class StillInMotionException extends LittleActionExceptionHidden {
        
        public StillInMotionException() {
            super("Structure is still in motion");
        }
        
    }
    
    public static enum DoorActivator {
        RIGHTCLICK,
        COMMAND,
        SIGNAL;
        
    }
    
}
