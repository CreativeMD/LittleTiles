package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.interact.LittleInteraction;
import com.creativemd.littletiles.server.LittleTilesServer;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class LittleInteractionPacket extends CreativeCorePacket {
    
    public int index;
    public boolean rightclick;
    public boolean start;
    
    public LittleInteractionPacket(LittleInteraction interaction, boolean start) {
        this.index = interaction.index;
        this.rightclick = interaction.rightclick;
        this.start = start;
    }
    
    public LittleInteractionPacket() {}
    
    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeInt(index);
        buf.writeBoolean(rightclick);
        buf.writeBoolean(start);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        this.index = buf.readInt();
        this.rightclick = buf.readBoolean();
        this.start = buf.readBoolean();
    }
    
    @Override
    public void executeClient(EntityPlayer player) {}
    
    @Override
    public void executeServer(EntityPlayer player) {
        if (start)
            LittleTilesServer.INTERACTION.start(player, index, rightclick);
        else
            LittleTilesServer.INTERACTION.end(player, index);
    }
    
}
