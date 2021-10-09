package team.creative.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import team.creative.littletiles.common.entity.EntityAnimation;

public class LittleResetAnimationPacket extends CreativeCorePacket {
    
    public UUID animationUUID;
    
    public LittleResetAnimationPacket(UUID animationUUID) {
        this.animationUUID = animationUUID;
    }
    
    public LittleResetAnimationPacket() {
        
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        writeString(buf, animationUUID.toString());
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        animationUUID = UUID.fromString(readString(buf));
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        EntityAnimation animation = WorldAnimationHandler.findAnimation(true, animationUUID);
        if (animation == null)
            return;
        animation.isDead = true;
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        
    }
    
}
