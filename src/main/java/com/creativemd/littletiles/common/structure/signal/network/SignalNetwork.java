package com.creativemd.littletiles.common.structure.signal.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureBase;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureComponent;

public class SignalNetwork {
	
	public final int bandwidth;
	private final boolean[] state;
	private List<ISignalStructureTransmitter> transmitters = new ArrayList<>();
	/** are outputs of the network's perspective as they are inputs of machines (receive signals) */
	private List<ISignalStructureComponent> inputs = new ArrayList<>();
	/** are inputs of the network's perspective as they are outputs of machines (transmit signals) */
	private List<ISignalStructureComponent> outputs = new ArrayList<>();
	
	public SignalNetwork(int bandwidth) {
		this.bandwidth = bandwidth;
		this.state = new boolean[bandwidth];
	}
	
	public void update() {
		boolean[] oldState = Arrays.copyOf(state, bandwidth);
		BooleanUtils.reset(state);
		
		for (int i = 0; i < outputs.size(); i++)
			BooleanUtils.or(state, outputs.get(i).getState());
		
		if (!BooleanUtils.equals(state, oldState) && !inputs.isEmpty())
			for (int i = 0; i < inputs.size(); i++)
				inputs.get(i).updateState(state);
	}
	
	public void merge(SignalNetwork network) {
		boolean[] oldState = Arrays.copyOf(state, bandwidth);
		boolean[] oldState2 = Arrays.copyOf(network.state, bandwidth);
		
		int sizeBefore = outputs.size();
		for (int i = 0; i < network.outputs.size(); i++) {
			ISignalStructureComponent output = network.outputs.get(i);
			if (!containsUntil(outputs, output, sizeBefore)) {
				BooleanUtils.or(state, output.getState());
				output.setNetwork(this);
				outputs.add(output);
			}
		}
		
		boolean changed = !BooleanUtils.equals(state, oldState);
		boolean changed2 = !BooleanUtils.equals(state, oldState2);
		
		if (changed && !inputs.isEmpty())
			for (int i = 0; i < inputs.size(); i++)
				inputs.get(i).updateState(state);
			
		sizeBefore = inputs.size();
		if (!network.inputs.isEmpty())
			for (int i = 0; i < network.inputs.size(); i++) {
				ISignalStructureComponent input = network.inputs.get(i);
				if (!containsUntil(inputs, input, sizeBefore)) {
					input.setNetwork(this);
					if (changed2)
						input.updateState(state);
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
			break;
		case OUTPUT:
			outputs.add((ISignalStructureComponent) base);
			break;
		case TRANSMITTER:
			transmitters.add((ISignalStructureTransmitter) base);
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
	
	public void remove(ISignalStructureBase base) {
		base.setNetwork(null);
		
		switch (base.getType()) {
		case INPUT:
			inputs.remove(base);
			break;
		case OUTPUT:
			outputs.remove(base);
			break;
		case TRANSMITTER:
			deleteNetwork();
			break;
		}
	}
}
