package team.creative.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter;

public class SubContainerParticle extends SubContainer {
    
    public LittleParticleEmitter particle;
    
    public SubContainerParticle(EntityPlayer player, LittleParticleEmitter particle) {
        super(player);
        this.particle = particle;
    }
    
    @Override
    public void createControls() {
        
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        particle.loadSettings(nbt);
        particle.updateStructure();
    }
    
}
