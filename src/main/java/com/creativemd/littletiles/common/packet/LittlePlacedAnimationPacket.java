package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class LittlePlacedAnimationPacket extends CreativeCorePacket {
    
    public StructureLocation location;
    
    public UUID previousAnimation;
    
    public LittlePlacedAnimationPacket(StructureLocation location, UUID previousAnimation) {
        this.previousAnimation = previousAnimation;
        this.location = location;
    }
    
    public LittlePlacedAnimationPacket() {
        
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        LittleAction.writeStructureLocation(location, buf);
        writeString(buf, previousAnimation.toString());
        
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        location = LittleAction.readStructureLocation(buf);
        previousAnimation = UUID.fromString(readString(buf));
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        try {
            LittleStructure structure = location.find(player.world);
            if (structure instanceof LittleDoor)
                ((LittleDoor) structure).waitingForApproval = false;
        } catch (LittleActionException e) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(true, previousAnimation);
            if (animation != null && animation.controller != null)
                animation.controller.onServerPlaces();
        }
        
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        
    }
    
}
