package com.creativemd.littletiles.common.structure.signal.logic;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.StructureChildConnection;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.signal.logic.SignalCondition.SignalConditionInput;

public class SignalConditionChild extends SignalConditionInput {
	
	public SignalConditionInput subCondition;
	
	public SignalConditionChild(int id, SignalConditionInput subCondition) {
		super(id);
		this.subCondition = subCondition;
	}
	
	@Override
	public void test(LittleStructure structure, boolean[] state) {
		if (childId < structure.getChildren().size()) {
			StructureChildConnection child = structure.getChild(childId);
			try {
				subCondition.test(child.getStructure(), state);
			} catch (CorruptedConnectionException | NotYetConnectedException e) {}
		}
	}
	
	@Override
	public String write() {
		return "c" + childId + "." + subCondition.write();
	}
	
}
