package team.creative.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.util.type.PairList;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.type.door.LittleDoorBase.LittleDoorBaseType;

public class LittleDoorActivator extends LittleDoor {
    
    public int[] toActivate;
    
    public boolean inMotion = false;
    
    public LittleDoorActivator(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        nbt.putIntArray("activate", toActivate);
        nbt.putBoolean("inMotion", inMotion);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        toActivate = nbt.getIntArray("activate");
        inMotion = nbt.getBoolean("inMotion");
    }
    
    public LittleDoor getChildrenDoor(int index) throws CorruptedConnectionException, NotYetConnectedException {
        if (index >= 0 && index < countChildren()) {
            LittleStructure structure = getChild(index).getStructure();
            if (structure instanceof LittleDoor)
                return (LittleDoor) structure;
            return null;
        }
        return null;
    }
    
    @Override
    public EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, boolean tickOnce) throws LittleActionException {
        inMotion = true;
        for (int i : toActivate) {
            LittleDoor child = getChildrenDoor(i);
            if (child == null)
                continue;
            EntityAnimation childAnimation = child.openDoor(player, uuid, tickOnce);
            if (childAnimation != null)
                childAnimation.controller.onServerApproves();
        }
        return null;
    }
    
    @Override
    public int getCompleteDuration() {
        int duration = 0;
        for (int i : toActivate) {
            
            try {
                LittleDoor child;
                child = getChildrenDoor(i);
                if (child == null)
                    continue;
                duration = Math.max(duration, child.getCompleteDuration());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        return duration;
    }
    
    @Override
    public List<LittleDoor> collectDoorsToCheck() {
        List<LittleDoor> doors = new ArrayList<>();
        for (int i : toActivate) {
            try {
                LittleDoor child = getChildrenDoor(i);
                if (child == null)
                    continue;
                doors.add(child);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        return doors;
    }
    
    @Override
    public boolean isInMotion() {
        return inMotion;
    }
    
    public boolean checkChildrenInMotion() {
        for (int i : toActivate) {
            try {
                LittleDoor child = getChildrenDoor(i);
                if (child == null)
                    continue;
                if (child.isInMotion())
                    return true;
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        return false;
    }
    
    @Override
    public void onChildComplete(LittleDoor door, int childId) {
        inMotion = checkChildrenInMotion();
        if (!inMotion)
            completeAnimation();
    }
    
    public static class LittleDoorActivatorType extends LittleDoorBaseType {
        
        public LittleDoorActivatorType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
            super(id, category, structureClass, attribute);
        }
        
        @Override
        public void setBit(LittlePreviews previews, BitSet set) {
            for (int i : previews.structureNBT.getIntArray("activate"))
                set.set(i);
        }
    }
}
