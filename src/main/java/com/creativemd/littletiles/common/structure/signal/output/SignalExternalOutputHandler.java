package com.creativemd.littletiles.common.structure.signal.output;

import java.text.ParseException;
import java.util.Arrays;
import java.util.function.Function;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.StructureChildConnection;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalOutput;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class SignalExternalOutputHandler implements ISignalComponent {
    
    public final LittleStructure structure;
    public final int index;
    public SignalInputCondition condition;
    public SignalOutputHandler handler;
    
    public SignalExternalOutputHandler(LittleStructure structure, int index, SignalInputCondition condition, Function<ISignalComponent, SignalOutputHandler> function) {
        this.structure = structure;
        this.index = index;
        this.condition = condition;
        this.handler = function.apply(this);
    }
    
    public SignalExternalOutputHandler(LittleStructure structure, NBTTagCompound nbt) throws ParseException {
        this.structure = structure;
        this.index = nbt.getInteger("index");
        try {
            if (nbt.hasKey("con"))
                condition = SignalInputCondition.parseInput(nbt.getString("con"));
            else
                condition = null;
        } catch (ParseException e) {
            condition = null;
        }
        SignalMode mode = SignalMode.EQUAL;
        if (nbt.hasKey("mode"))
            mode = SignalMode.valueOf(nbt.getString("mode"));
        int delay = nbt.getInteger("delay");
        if (condition != null)
            delay = Math.max((int) Math.ceil(condition.calculateDelay()), nbt.getInteger("delay"));
        handler = SignalOutputHandler.create(this, mode, delay, nbt, structure);
    }
    
    public ISignalStructureComponent getOutput() throws CorruptedConnectionException, NotYetConnectedException {
        StructureChildConnection connection = structure.getChild(index);
        LittleStructure output = connection.getStructure();
        if (output instanceof LittleSignalOutput)
            return (ISignalStructureComponent) output;
        throw new RuntimeException("Invalid structure child expected output " + output);
    }
    
    public void update() {
        if (condition == null)
            return;
        try {
            int bandwidth = handler.getBandwidth();
            if (bandwidth > 0) {
                boolean[] outputState = new boolean[bandwidth];
                boolean[] result = condition.test(structure, false);
                if (result.length == 1)
                    Arrays.fill(outputState, result[0]);
                else
                    for (int i = 0; i < result.length; i++)
                        if (i < outputState.length)
                            outputState[i] = result[i];
                handler.schedule(outputState);
            }
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
    }
    
    public NBTTagCompound write(boolean preview) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("index", index);
        if (structure != null)
            try {
                nbt.setInteger("state", BooleanUtils.boolToInt(getState()));
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        if (condition != null)
            nbt.setString("con", condition.write());
        nbt.setString("mode", handler == null ? SignalMode.EQUAL.name() : handler.getMode().name());
        if (handler != null) {
            nbt.setInteger("delay", handler.delay);
            handler.write(preview, nbt);
        }
        return nbt;
    }
    
    @Override
    public int getBandwidth() throws CorruptedConnectionException, NotYetConnectedException {
        return getOutput().getBandwidth();
    }
    
    @Override
    public void changed() throws CorruptedConnectionException, NotYetConnectedException {
        getOutput().changed();
        structure.schedule();
        
    }
    
    @Override
    public boolean[] getState() throws CorruptedConnectionException, NotYetConnectedException {
        return getOutput().getState();
    }
    
    @Override
    public SignalComponentType getType() {
        return SignalComponentType.OUTPUT;
    }
    
    @Override
    public LittleStructure getStructure() {
        return structure;
    }
    
    @Override
    public World getWorld() {
        if (structure == null)
            return null;
        return structure.getWorld();
    }
    
    @Override
    public String toString() {
        return "o" + index;
    }
}
