package team.creative.littletiles.common.structure.type.door;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.common.action.LittleActionActivated;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.action.LittleActionException.LittleActionExceptionHidden;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.animation.event.AnimationEvent;
import team.creative.littletiles.common.animation.event.ChildActivateEvent;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.packet.LittleActivateDoorPacket;
import team.creative.littletiles.common.structure.IAnimatedStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.type.door.LittleDoorBase.LittleDoorBaseType;

public abstract class LittleDoor extends LittleStructure {
    
    public LittleDoor(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    public boolean activateParent = false;
    public boolean waitingForApproval = false;
    public boolean disableRightClick = false;
    public boolean opened = false;
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        activateParent = nbt.getBoolean("activateParent");
        disableRightClick = nbt.getBoolean("disableRightClick");
        opened = nbt.getBoolean("opened");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putBoolean("activateParent", activateParent);
        nbt.putBoolean("disableRightClick", disableRightClick);
        nbt.putBoolean("opened", opened);
    }
    
    public EntityAnimation activate(DoorActivator activator, @Nullable Player player, @Nullable UUID uuid, boolean sendUpdate) throws LittleActionException {
        if (waitingForApproval)
            throw new LittleActionExceptionHidden("Door has not been approved yet!");
        
        if (activator == DoorActivator.RIGHTCLICK && disableRightClick)
            throw new LittleActionExceptionHidden("Door is locked!");
        
        load();
        
        if (activateParent && getParent() != null) {
            LittleStructure parentStructure = getParent().getStructure();
            if (parentStructure instanceof LittleDoor)
                return ((LittleDoor) parentStructure).activate(activator, player, uuid, sendUpdate);
            throw new LittleActionException("Invalid parent");
        }
        
        if (isInMotion())
            throw new StillInMotionException();
        
        if (!canOpenDoor(player)) {
            if (player != null)
                player.sendStatusMessage(new TextComponentTranslation("exception.door.notenoughspace"), true);
            throw new LittleActionException("Cannot open door");
        }
        
        if (uuid == null)
            if (this instanceof IAnimatedStructure && ((IAnimatedStructure) this).isAnimated())
                uuid = ((IAnimatedStructure) this).getAnimation().getUniqueID();
            else
                uuid = UUID.randomUUID();
            
        opened = !opened;
        if (activator != DoorActivator.SIGNAL && !getWorld().isRemote)
            getOutput(0).toggle();
        
        if (sendUpdate) {
            if (getWorld().isRemote)
                sendActivationToServer(activator, player, uuid);
            else
                sendActivationToClient(activator, player, uuid);
        }
        
        return openDoor(player, new UUIDSupplier(uuid), false);
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, Player playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        activate(DoorActivator.RIGHTCLICK, playerIn, null, true);
        action.preventInteraction = true;
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
    
    public void sendActivationToServer(DoorActivator type, Player activator, UUID uuid) {
        PacketHandler.sendPacketToServer(new LittleActivateDoorPacket(type, getStructureLocation(), uuid));
    }
    
    public void sendActivationToClient(DoorActivator type, Player activator, UUID uuid) {
        PacketHandler
                .sendPacketToTrackingPlayers(new LittleActivateDoorPacket(type, getStructureLocation(), uuid), getWorld(), getPos(), activator != null ? (x) -> x != activator : null);
    }
    
    public abstract int getCompleteDuration();
    
    public abstract List<LittleDoor> collectDoorsToCheck();
    
    public abstract boolean isInMotion();
    
    public boolean canOpenDoor(@Nullable Player player) {
        if (isInMotion())
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
    
    public abstract EntityAnimation openDoor(@Nullable Player player, boolean tickOnce) throws LittleActionException;
    
    public void onChildComplete(LittleDoor door, int childId) {
        
    }
    
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
                    activate(DoorActivator.SIGNAL, null, null, true);
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
