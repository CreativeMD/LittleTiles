package team.creative.littletiles.common.animation.program;

import java.util.HashMap;

import team.creative.littletiles.common.animation.AnimationState;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;

public class StateProgram<T> {
    
    private HashMap<String, ProgramState> states = new HashMap<>();
    private ProgramState current;
    
    public StateProgram() {}
    
    public ProgramState get(String in) {
        return states.get(in);
    }
    
    public ProgramState state() {
        return current;
    }
    
    public class ProgramState {
        
        public final AnimationState state;
        private final HashMap<T, MachineTransition> commands = new HashMap<>();
        
        public ProgramState(AnimationState state) {
            this.state = state;
        }
        
        public MachineTransition transition(String input) {
            return commands.get(input);
        }
        
    }
    
    public class MachineTransition {
        
        public final String start;
        public final String end;
        public final AnimationTimeline timeline;
        
        public MachineTransition(String start, String end, AnimationTimeline timeline) {
            this.start = start;
            this.end = end;
            this.timeline = timeline;
        }
        
    }
    
}
