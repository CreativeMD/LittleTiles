package com.creativemd.littletiles.common.structure.signal.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureBase;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.schedule.ISignalSchedulable;

import net.minecraft.world.World;

public class SignalNetwork implements ISignalSchedulable {
    
    public final int bandwidth;
    private final boolean[] state;
    private boolean changed = false;
    private boolean forceUpdate = false;
    private boolean containsUnloadedComponents = false;
    private List<ISignalStructureTransmitter> transmitters = new ArrayList<>();
    /** are outputs of the network's perspective as they are inputs of machines (receive signals) */
    private List<ISignalStructureComponent> inputs = new ArrayList<>();
    /** are inputs of the network's perspective as they are outputs of machines (transmit signals) */
    private List<ISignalStructureComponent> outputs = new ArrayList<>();
    
    public SignalNetwork(int bandwidth) {
        this.bandwidth = bandwidth;
        this.state = new boolean[bandwidth];
    }
    
    public List<ISignalStructureComponent> getOutputs() {
        return outputs;
    }
    
    public boolean requiresResearch() {
        return containsUnloadedComponents;
    }
    
    @Override
    public void notifyChange() {
        if (!forceUpdate) {
            boolean[] oldState = Arrays.copyOf(state, bandwidth);
            BooleanUtils.reset(state);
            
            for (int i = 0; i < outputs.size(); i++)
                try {
                    BooleanUtils.or(state, outputs.get(i).getState());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            
            if (BooleanUtils.equals(state, oldState) && !inputs.isEmpty())
                return;
        } else
            for (int i = 0; i < outputs.size(); i++)
                try {
                    BooleanUtils.or(state, outputs.get(i).getState());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        forceUpdate = false;
        for (int i = 0; i < inputs.size(); i++)
            inputs.get(i).updateState(state);
    }
    
    @Override
    public boolean hasChanged() {
        return changed;
    }
    
    @Override
    public void markChanged() {
        changed = true;
    }
    
    @Override
    public void markUnchanged() {
        changed = false;
    }
    
    public void update() {
        schedule();
    }
    
    @Override
    public World getWorld() {
        if (!inputs.isEmpty())
            return inputs.get(0).getStructureWorld();
        if (!outputs.isEmpty())
            return outputs.get(0).getStructureWorld();
        if (!transmitters.isEmpty())
            return transmitters.get(0).getStructureWorld();
        return null;
    }
    
    public void merge(SignalNetwork network) {
        int sizeBefore = outputs.size();
        for (int i = 0; i < network.outputs.size(); i++) {
            ISignalStructureComponent output = network.outputs.get(i);
            if (!containsUntil(outputs, output, sizeBefore)) {
                output.setNetwork(this);
                outputs.add(output);
            }
        }
        
        sizeBefore = inputs.size();
        if (!network.inputs.isEmpty())
            for (int i = 0; i < network.inputs.size(); i++) {
                ISignalStructureComponent input = network.inputs.get(i);
                if (!containsUntil(inputs, input, sizeBefore)) {
                    input.setNetwork(this);
                    inputs.add(input);
                }
            }
        
        sizeBefore = transmitters.size();
        if (!network.transmitters.isEmpty())
            for (int i = 0; i < network.transmitters.size(); i++) {
                ISignalStructureTransmitter transmitter = network.transmitters.get(i);
                if (!containsUntil(transmitters, transmitter, sizeBefore)) {
                    transmitter.setNetwork(this);
                    transmitters.add(transmitter);
                }
            }
        
        forceUpdate = true;
        schedule();
    }
    
    /** @param list
     *            list to search in
     * @param object
     *            object to find in list
     * @param index
     *            will search from 0 to index - 1 (exclusive)
     * @return */
    public static <T> boolean containsUntil(List<T> list, T object, int index) {
        for (int i = 0; i < index; i++)
            if (list.get(i) == object)
                return true;
        return false;
    }
    
    public void add(ISignalStructureBase base) {
        if (base.getType() == SignalComponentType.INVALID)
            containsUnloadedComponents = true;
        
        if (base.getNetwork() == this)
            return;
        
        if (base.hasNetwork()) {
            merge(base.getNetwork());
            return;
        }
        
        base.setNetwork(this);
        
        Iterator<ISignalStructureBase> connections = base.connections();
        while (connections.hasNext())
            add(connections.next());
        
        switch (base.getType()) {
        case INPUT:
            inputs.add((ISignalStructureComponent) base);
            forceUpdate = true;
            schedule();
            break;
        case OUTPUT:
            outputs.add((ISignalStructureComponent) base);
            forceUpdate = true;
            schedule();
            break;
        case TRANSMITTER:
            transmitters.add((ISignalStructureTransmitter) base);
            break;
        case IOSPECIAL:
            inputs.add((ISignalStructureComponent) base);
            outputs.add((ISignalStructureComponent) base);
            forceUpdate = true;
            schedule();
            break;
        }
    }
    
    public void deleteNetwork() {
        for (int i = 0; i < inputs.size(); i++)
            inputs.get(i).setNetwork(null);
        for (int i = 0; i < outputs.size(); i++)
            outputs.get(i).setNetwork(null);
        for (int i = 0; i < transmitters.size(); i++)
            transmitters.get(i).setNetwork(null);
        
        inputs.clear();
        outputs.clear();
        transmitters.clear();
    }
    
    public void unload(ISignalStructureBase base) {
        deleteNetwork();
    }
    
    public boolean remove(ISignalStructureBase base) {
        base.setNetwork(null);
        
        switch (base.getType()) {
        case INPUT:
            inputs.remove(base);
            schedule();
            return false;
        case OUTPUT:
            outputs.remove(base);
            schedule();
            return false;
        case TRANSMITTER:
            deleteNetwork();
            return true;
        case IOSPECIAL:
            inputs.remove(base);
            outputs.remove(base);
            schedule();
            return false;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return Integer.toHexString(hashCode());
    }
    
    @Override
    public boolean isStillAvailable() {
        return true;
    }
}
