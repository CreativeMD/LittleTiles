package com.creativemd.littletiles.common.structure.signal.output;

import java.text.ParseException;
import java.util.Arrays;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.InternalSignal;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;

import net.minecraft.nbt.NBTTagCompound;

public class InternalSignalOutput extends InternalSignal {
	
	public final SignalMode defaultMode;
	public SignalInputCondition condition;
	public SignalOutputHandler handler;
	
	public InternalSignalOutput(LittleStructure parent, String name, int bandwidth, SignalMode defaultMode) {
		super(parent, name, bandwidth);
		this.defaultMode = defaultMode;
	}
	
	@Override
	public void changed() {
		parent.performInternalOutputChange(this);
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
		if (condition != null) {
			SignalMode mode = defaultMode;
			if (nbt.hasKey("mode"))
				mode = SignalMode.valueOf(nbt.getString("mode"));
			int delay = Math.max((int) Math.ceil(condition.calculateDelay()), nbt.getInteger("delay"));
			handler = SignalOutputHandler.create(this, mode, delay, nbt);
		} else
			handler = null;
	}
	
	@Override
	public NBTTagCompound write(NBTTagCompound nbt) {
		nbt.setInteger("state", BooleanUtils.boolToInt(getState()));
		if (condition != null)
			nbt.setString("con", condition.write());
		nbt.setString("mode", handler == null ? defaultMode.name() : handler.getMode().name());
		if (handler != null) {
			nbt.setInteger("delay", handler.delay);
			handler.write(nbt);
		}
		return nbt;
	}
	
	public void update() {
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
