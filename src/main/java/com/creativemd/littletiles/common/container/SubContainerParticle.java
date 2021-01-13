package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

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
