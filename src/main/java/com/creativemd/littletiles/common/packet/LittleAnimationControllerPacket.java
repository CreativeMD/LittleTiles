package com.creativemd.littletiles.common.packet;

import java.util.Iterator;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;
import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class LittleAnimationControllerPacket extends CreativeCorePacket {
    
    public UUID uuid;
    public NBTTagCompound nbt;
    
    public LittleAnimationControllerPacket(EntityAnimation animation) {
        this(animation.getUniqueID(), animation.controller.writeToNBT(new NBTTagCompound()));
    }
    
    public LittleAnimationControllerPacket(UUID uuid, NBTTagCompound nbt) {
        this.uuid = uuid;
        this.nbt = nbt;
    }
    
    public LittleAnimationControllerPacket() {}
    
    @Override
    public void writeBytes(ByteBuf buf) {
        writeString(buf, uuid.toString());
        writeNBT(buf, nbt);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        uuid = UUID.fromString(readString(buf));
        nbt = readNBT(buf);
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        EntityAnimation animation = WorldAnimationHandler.findAnimation(true, uuid);
        if (animation != null) {
            animation.controller = DoorController.parseController(animation, nbt);
            animation.updateTickState();
            return;
        }
        
        for (Iterator<EntityAnimation> iterator = player.world.getEntities(EntityAnimation.class, new Predicate<EntityAnimation>() {
            
            @Override
            public boolean apply(EntityAnimation input) {
                return true;
            }
            
        }).iterator();iterator.hasNext();) {
            Entity entity = iterator.next();
            if (entity instanceof EntityAnimation && entity.getUniqueID().equals(uuid)) {
                animation = (EntityAnimation) entity;
                animation.controller = DoorController.parseController(animation, nbt);
                animation.updateTickState();
                return;
            }
        }
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        EntityAnimation animation = WorldAnimationHandler.findAnimation(false, uuid);
        if (animation != null)
            PacketHandler.sendPacketToPlayer(new LittleAnimationControllerPacket(uuid, animation.controller.writeToNBT(new NBTTagCompound())), (EntityPlayerMP) player);
        else
            PacketHandler.sendPacketToPlayer(new LittleAnimationDestroyPacket(uuid, false), (EntityPlayerMP) player);
    }
    
}
