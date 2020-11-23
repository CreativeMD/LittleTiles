package com.creativemd.littletiles.common.structure.signal.schedule;

import java.lang.ref.WeakReference;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputCondition;

public class SignalScheduleTicket implements ISignalScheduleTicket {
	
	private int delay;
	private final WeakReference<SignalOutputCondition> outputCondition;
	private final WeakReference<LittleStructure> structure;
	private final boolean[] result;
	
	public SignalScheduleTicket(SignalOutputCondition outputCondition, LittleStructure structure, boolean[] result, int delay) {
		this.outputCondition = new WeakReference<SignalOutputCondition>(outputCondition);
		this.structure = new WeakReference<LittleStructure>(structure);
		this.result = result;
		this.delay = delay;
	}
	
	public int tick() {
		delay--;
		return delay;
	}
	
	public void run() {
		SignalOutputCondition condition = outputCondition.get();
		LittleStructure cached = structure.get();
		if (condition != null && cached != null)
			condition.performStateChange(cached, result);
	}
	
	@Override
	public int getDelay() {
		if (inShortQueue()) {
			LittleStructure cached = structure.get();
			if (cached != null)
				return SignalTicker.get(cached).getDelayOfQueue(delay);
		}
		return delay;
	}
	
	public boolean is(SignalOutputCondition output) {
		return outputCondition.get() == output;
	}
	
	public boolean inShortQueue() {
		return delay < SignalTicker.queueLength;
	}
	
	public void enterShortQueue(int index) {
		this.delay = index;
	}
	
	@Override
	public boolean[] getState() {
		return result;
	}
	
	@Override
	public void overwriteState(boolean[] newState) {
		BooleanUtils.set(result, newState);
	}
	
	@Override
	public void markObsolete() {
		outputCondition.clear();
		structure.clear();
	}
	
}
