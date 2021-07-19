package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;
import com.creativemd.littletiles.server.world.LittleAnimationHandlerServer;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class LittleCancelClickThrough extends CreativeCorePacket {
    
    public LittleCancelClickThrough() {
        
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        ((LittleAnimationHandlerServer) WorldAnimationHandler.getHandler(player.world)).queueClickThrough(player);
    }
    
}
