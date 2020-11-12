package com.creativemd.littletiles.common.structure.signal.logic;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.SignalUtils;
import com.creativemd.littletiles.common.structure.signal.logic.SignalCondition.SignalConditionInput;

public class SignalConditionExternal extends SignalConditionInput {
	
	public SignalConditionExternal(int id) {
		super(id);
	}
	
	@Override
	public void test(LittleStructure structure, boolean[] state) {
		SignalUtils.combine(state, SignalUtils.getInputState(structure, childId, true));
	}
	
	@Override
	public String write() {
		return "i" + childId;
	}
	
	public static class SignalConditionExternalExact extends SignalConditionInput {
		
		public int[] indexes;
		
		public SignalConditionExternalExact(int id, int[] indexes) {
			super(id);
			this.indexes = indexes;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			boolean found = SignalUtils.is(SignalUtils.getInputState(structure, childId, true), indexes);
			for (int i = 0; i < state.length; i++)
				state[i] = found;
		}
		
		@Override
		public String write() {
			String result = "i" + childId + "{";
			for (int i = 0; i < indexes.length; i++) {
				int index = indexes[i];
				if (index < 0)
					result += ",";
				result += (char) index;
			}
			return result + "}";
		}
		
	}
	
}
