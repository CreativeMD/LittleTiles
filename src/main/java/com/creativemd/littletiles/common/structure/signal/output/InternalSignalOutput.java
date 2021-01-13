package com.creativemd.littletiles.common.structure.signal.output;

import java.text.ParseException;
import java.util.Arrays;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.packet.LittleUpdateOutputPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.InternalComponentOutput;
import com.creativemd.littletiles.common.structure.signal.component.InternalSignal;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode.SignalOutputHandlerToggle;

import net.minecraft.nbt.NBTTagCompound;

public class InternalSignalOutput extends InternalSignal<InternalComponentOutput> {
    
    public final SignalMode defaultMode;
    public final boolean syncToClient;
    public SignalInputCondition condition;
    public SignalOutputHandler handler;
    
    public InternalSignalOutput(LittleStructure parent, InternalComponentOutput component) {
        super(parent, component);
        this.defaultMode = component.defaultMode;
        this.syncToClient = component.syncToClient;
    }
    
    @Override
    public void changed() {
        parent.performInternalOutputChange(this);
        parent.schedule();
        if (syncToClient)
            PacketHandler.sendPacketToTrackingPlayers(new LittleUpdateOutputPacket(parent.getStructureLocation(), component.index, getState()), getWorld(), parent.getPos(), null);
    }
    
    @Override
    public SignalComponentType getType() {
        return SignalComponentType.OUTPUT;
    }
    
    @Override
    public void load(NBTTagCompound nbt) {
        BooleanUtils.intToBool(nbt.getInteger("state"), getState());
        try {
            if (nbt.hasKey("con"))
                condition = SignalInputCondition.parseInput(nbt.getString("con"));
            else
                condition = null;
        } catch (ParseException e) {
            condition = null;
        }
        SignalMode mode = defaultMode;
        if (nbt.hasKey("mode"))
            mode = SignalMode.valueOf(nbt.getString("mode"));
        int delay = nbt.getInteger("delay");
        if (condition != null)
            delay = Math.max((int) Math.ceil(condition.calculateDelay()), nbt.getInteger("delay"));
        handler = SignalOutputHandler.create(this, mode, delay, nbt, parent);
    }
    
    @Override
    public NBTTagCompound write(boolean preview, NBTTagCompound nbt) {
        nbt.setInteger("state", BooleanUtils.boolToInt(getState()));
        if (condition != null)
            nbt.setString("con", condition.write());
        nbt.setString("mode", handler == null ? defaultMode.name() : handler.getMode().name());
        if (handler != null) {
            nbt.setInteger("delay", handler.delay);
            handler.write(preview, nbt);
        }
        return nbt;
    }
    
    public void toggle() {
        if (handler instanceof SignalOutputHandlerToggle)
            ((SignalOutputHandlerToggle) handler).triggerToggle();
    }
    
    public void update() {
        if (condition == null)
            return;
        int bandwidth = getBandwidth();
        if (bandwidth > 0) {
            boolean[] outputState = new boolean[bandwidth];
            boolean[] result = condition.test(getStructure(), false);
            if (result.length == 1)
                Arrays.fill(outputState, result[0]);
            else
                for (int i = 0; i < result.length; i++)
                    if (i < outputState.length)
                        outputState[i] = result[i];
            handler.schedule(outputState);
        }
    }
    
}
