package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor.DoorActivator;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class LittleActivateDoorPacket extends CreativeCorePacket {
    
    public StructureLocation location;
    public DoorActivator activator;
    public UUID uuid;
    
    public LittleActivateDoorPacket(DoorActivator activator, StructureLocation location, UUID uuid) {
        this.location = location;
        this.activator = activator;
        this.uuid = uuid;
    }
    
    public LittleActivateDoorPacket() {}
    
    @Override
    public void writeBytes(ByteBuf buf) {
        LittleAction.writeStructureLocation(location, buf);
        writeString(buf, uuid.toString());
        buf.writeInt(activator.ordinal());
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        location = LittleAction.readStructureLocation(buf);
        uuid = UUID.fromString(readString(buf));
        activator = DoorActivator.values()[buf.readInt()];
    }
    
    @Override
    public void executeClient(EntityPlayer player) {}
    
    @Override
    public void executeServer(EntityPlayer player) {
        LittleTile tile;
        EntityAnimation animation = null;
        
        try {
            LittleStructure structure = location.find(player.world);
            if (structure instanceof LittleDoor && !structure.mainBlock.isRemoved()) {
                LittleDoor door = (LittleDoor) structure;
                World world = door.getWorld();
                if (world instanceof SubWorld)
                    animation = (EntityAnimation) ((SubWorld) world).parent;
                
                door.activate(activator, player, uuid);
            }
        } catch (LittleActionException e) {
            if (!e.isHidden())
                player.sendStatusMessage(new TextComponentString(e.getLocalizedMessage()), true);
        }
    }
    
}
