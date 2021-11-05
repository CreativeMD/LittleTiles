package com.creativemd.littletiles.common.packet;

import java.util.Iterator;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;
import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class LittleAnimationDataPacket extends CreativeCorePacket {
    
    public UUID uuid;
    public NBTTagCompound nbt;
    public boolean enteredAsChild;
    
    public LittleAnimationDataPacket(UUID uuid, NBTTagCompound nbt, boolean enteredAsChild) {
        this.uuid = uuid;
        this.nbt = nbt;
        this.enteredAsChild = enteredAsChild;
    }
    
    public LittleAnimationDataPacket(EntityAnimation animation) {
        this(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), animation.enteredAsChild);
    }
    
    public LittleAnimationDataPacket() {}
    
    @Override
    public void writeBytes(ByteBuf buf) {
        writeString(buf, uuid.toString());
        writeNBT(buf, nbt);
        buf.writeBoolean(enteredAsChild);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        uuid = UUID.fromString(readString(buf));
        nbt = readNBT(buf);
        enteredAsChild = buf.readBoolean();
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        EntityAnimation animation = WorldAnimationHandler.findAnimation(true, uuid);
        if (animation != null) {
            updateAnimation(animation);
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
                updateAnimation(animation);
                if (!animation.isDoorAdded())
                    animation.addDoor();
                return;
            }
        }
        System.out.println("Entity not found!");
    }
    
    public void updateAnimation(EntityAnimation animation) {
        animation.isDead = false;
        if (!this.enteredAsChild || animation.enteredAsChild != this.enteredAsChild) {
            animation.readFromNBT(nbt);
            animation.updateTickState();
        }
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        EntityAnimation animation = WorldAnimationHandler.findAnimation(false, uuid);
        if (animation != null)
            PacketHandler.sendPacketToPlayer(new LittleAnimationDataPacket(animation), (EntityPlayerMP) player);
        else
            PacketHandler.sendPacketToPlayer(new LittleAnimationDestroyPacket(uuid, false), (EntityPlayerMP) player);
    }
    
}
