package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.api.ILittleTool;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class LittleRotatePacket extends CreativeCorePacket {
    
    public LittleRotatePacket() {
        
    }
    
    public Rotation rotation;
    
    public LittleRotatePacket(Rotation rotation) {
        this.rotation = rotation;
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeInt(rotation.ordinal());
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        rotation = Rotation.values()[buf.readInt()];
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        execute(player);
    }
    
    public void execute(EntityPlayer player) {
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.getItem() instanceof ILittleTool)
            ((ILittleTool) stack.getItem()).rotate(player, stack, rotation);
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        execute(player);
    }
    
}
