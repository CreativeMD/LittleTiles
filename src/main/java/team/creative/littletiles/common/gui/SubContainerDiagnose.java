package team.creative.littletiles.common.gui;

import java.util.UUID;

import com.creativemd.creativecore.common.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.level.WorldAnimationHandler;

public class SubContainerDiagnose extends SubContainer {
    
    public SubContainerDiagnose(EntityPlayer player) {
        super(player);
    }
    
    @Override
    public void createControls() {
        
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        EntityAnimation animation = WorldAnimationHandler.getHandler(player.world).findAnimation(UUID.fromString(nbt.getString("uuid")));
        if (animation != null)
            animation.destroyAnimation();
    }
    
}
