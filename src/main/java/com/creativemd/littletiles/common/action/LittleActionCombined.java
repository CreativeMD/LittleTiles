package com.creativemd.littletiles.common.action;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing.Axis;

public class LittleActionCombined extends LittleAction {
    
    public LittleAction[] actions;
    
    public LittleActionCombined(LittleAction... actions) {
        this.actions = actions;
    }
    
    public LittleActionCombined() {
        
    }
    
    @Override
    public boolean canBeReverted() {
        for (int i = 0; i < actions.length; i++) {
            if (!actions[i].canBeReverted())
                return false;
        }
        return true;
    }
    
    @Override
    public LittleAction revert(EntityPlayer player) throws LittleActionException {
        LittleAction[] newActions = new LittleAction[actions.length];
        for (int i = 0; i < newActions.length; i++) {
            if (actions[actions.length - 1 - i] != null)
                newActions[i] = actions[actions.length - 1 - i].revert(player);
        }
        return new LittleActionCombined(newActions);
    }
    
    @Override
    protected boolean action(EntityPlayer player) throws LittleActionException {
        if (actions.length == 0)
            return true;
        boolean success = false;
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] != null && actions[i].action(player))
                success = true;
        }
        return success;
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeInt(actions.length);
        
        for (int i = 0; i < actions.length; i++) {
            buf.writeInt(CreativeCorePacket.getId(actions[i]));
            actions[i].writeBytes(buf);
        }
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        actions = new LittleAction[buf.readInt()];
        for (int i = 0; i < actions.length; i++) {
            
            int id = buf.readInt();
            Class PacketClass = CreativeCorePacket.getClass(id);
            LittleAction packet = null;
            try {
                packet = (LittleAction) PacketClass.getConstructor().newInstance();
            } catch (Exception e) {
                System.out.println("Invalid packet id=" + id);
            }
            
            packet.readBytes(buf);
            actions[i] = packet;
        }
    }
    
    @Override
    public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
        LittleAction[] newActions = new LittleAction[actions.length];
        for (int i = 0; i < actions.length; i++)
            newActions[i] = actions[i].flip(axis, box);
        return new LittleActionCombined(newActions);
    }
    
}
