package com.creativemd.littletiles.common.structure.signal.logic.event;

import java.text.ParseException;

import com.creativemd.littletiles.common.structure.signal.logic.SignalCondition;

import net.minecraft.nbt.NBTTagCompound;

public abstract class SignalEventCondition extends SignalEvent {
	
	public SignalCondition condition;
	
	public SignalEventCondition(NBTTagCompound nbt) {
		super(nbt);
		try {
			condition = SignalCondition.parse(nbt.getString("condition"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = super.writeToNBT();
		nbt.setString("condition", condition.write());
		return nbt;
	}
	
}
