package com.creativemd.littletiles.common.structure.signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;

public class SignalNetwork {
	
	public final int bandwidth;
	private final boolean[] state;
	private List<ISignalTransmitter> transmitters = new ArrayList<>();
	/** are outputs of the network's perspective as they are inputs of machines (receive signals) */
	private List<ISignalInput> inputs = new ArrayList<>();
	/** are inputs of the network's perspective as they are outputs of machines (transmit signals) */
	private List<ISignalOutput> outputs = new ArrayList<>();
	
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
				inputs.get(i).setState(state);
	}
	
	public void merge(SignalNetwork network) {
		boolean[] oldState = Arrays.copyOf(state, bandwidth);
		boolean[] oldState2 = Arrays.copyOf(network.state, bandwidth);
		
		int sizeBefore = outputs.size();
		for (int i = 0; i < network.outputs.size(); i++) {
			ISignalOutput output = network.outputs.get(i);
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
				inputs.get(i).setState(state);
			
		sizeBefore = inputs.size();
		if (!network.inputs.isEmpty())
			for (int i = 0; i < network.inputs.size(); i++) {
				ISignalInput input = network.inputs.get(i);
				if (!containsUntil(inputs, input, sizeBefore)) {
					input.setNetwork(this);
					if (changed2)
						input.setState(state);
					inputs.add(input);
				}
			}
		
		sizeBefore = transmitters.size();
		if (!network.transmitters.isEmpty())
			for (int i = 0; i < network.transmitters.size(); i++) {
				ISignalTransmitter transmitter = network.transmitters.get(i);
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
	
	public void add(ISignalBase base) {
		if (base.getNetwork() == this)
			return;
		
		if (base.hasNetwork()) {
			merge(base.getNetwork());
			return;
		}
		
		base.setNetwork(this);
		
		switch (base.getType()) {
		case INPUT:
			inputs.add((ISignalInput) base);
			break;
		case OUTPUT:
			outputs.add((ISignalOutput) base);
			break;
		case TRANSMITTER:
			Iterator<ISignalBase> connections = base.connections();
			while (connections.hasNext())
				add(connections.next());
			transmitters.add((ISignalTransmitter) base);
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
	
	public boolean stillConnected(ISignalBase base) {
		for (Iterator iterator = base.connections(); iterator.hasNext();) {
			ISignalInput iSignalInput = (ISignalInput) iterator.next();
			if (iSignalInput.getNetwork() == this)
				return true;
		}
		return false;
	}
	
	public void remove(ISignalBase base) {
		base.setNetwork(null);
		
		switch (base.getType()) {
		case INPUT:
			if (!stillConnected(base))
				inputs.remove(base);
			break;
		case OUTPUT:
			if (!stillConnected(base))
				outputs.remove(base);
			break;
		case TRANSMITTER:
			deleteNetwork();
			break;
		}
	}
}
