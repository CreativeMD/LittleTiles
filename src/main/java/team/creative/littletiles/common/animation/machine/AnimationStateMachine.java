package team.creative.littletiles.common.animation.machine;

import java.util.HashMap;

import team.creative.littletiles.common.animation.AnimationState;

public class AnimationStateMachine {
    
    private HashMap<String, MachineState> states = new HashMap<>();
    private MachineState current;
    
    public AnimationStateMachine() {}
    
    public class MachineState {
        
        public final AnimationState state;
        
        public MachineState(AnimationState state) {
            this.state = state;
        }
        
    }
    
}
