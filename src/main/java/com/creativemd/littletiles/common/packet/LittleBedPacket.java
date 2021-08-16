package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.LittleBed;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import team.creative.littletiles.common.action.LittleActionException;

public class LittleBedPacket extends CreativeCorePacket {
    
    public StructureLocation location;
    public int playerID;
    
    public LittleBedPacket() {
        
    }
    
    public LittleBedPacket(StructureLocation location) {
        this.location = location;
        this.playerID = -1;
    }
    
    public LittleBedPacket(StructureLocation location, EntityPlayer player) {
        this(location);
        this.playerID = player.getEntityId();
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        LittleAction.writeStructureLocation(location, buf);
        buf.writeInt(playerID);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        location = LittleAction.readStructureLocation(buf);
        playerID = buf.readInt();
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        Entity entity = playerID == -1 ? player : player.world.getEntityByID(playerID);
        if (entity instanceof EntityPlayer) {
            try {
                LittleStructure structure = location.find(player.world);
                if (structure instanceof LittleBed)
                    ((LittleBed) structure).trySleep((EntityPlayer) entity, structure.getHighestCenterVec());
            } catch (LittleActionException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        
    }
    
}
