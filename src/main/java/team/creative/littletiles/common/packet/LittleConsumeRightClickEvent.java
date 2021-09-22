package team.creative.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.event.LittleEventHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public class LittleConsumeRightClickEvent extends CreativeCorePacket {
    
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
        LittleEventHandler.consumeBlockTilePrevent(player, EnumHand.MAIN_HAND);
    }
    
}
