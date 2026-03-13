package team.creative.littletiles.common.action;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.action.cancel.ActionCancelContext;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;

public class LittleActionWrapper<T> extends LittleAction<T> {
    
    private final Supplier<LittleAction<T>> supplier;
    private LittleAction<T> cached;
    private List<LittleBoxes> exclude;
    private List<LittleBoxes> include;
    
    public LittleActionWrapper(Supplier<LittleAction<T>> supplier) {
        this.supplier = supplier;
    }
    
    @Override
    public boolean canBeReverted() {
        return cached.canBeReverted();
    }
    
    @Override
    public LittleAction revert(LittleActionSource source) throws LittleActionException {
        return cached.revert(source);
    }
    
    @Override
    public T action(LittleActionSource source) throws LittleActionException {
        cached = supplier.get();
        
        if (exclude != null)
            for (LittleBoxes boxes : exclude)
                cached.exclude(boxes);
            
        if (include != null)
            for (LittleBoxes boxes : include)
                cached.include(boxes);
            
        return cached.action(source);
    }
    
    @Override
    public boolean wasSuccessful(T result) {
        return cached.wasSuccessful(result);
    }
    
    @Override
    public T failed() {
        return cached.failed();
    }
    
    @Override
    public void cancel(ActionCancelContext context) throws LittleActionException {
        cached.cancel(context);
    }
    
    @Override
    public LittleAction mirror(Axis axis, LittleBoxAbsolute box) {
        return cached.mirror(axis, box);
    }
    
    @Override
    public void include(LittleBoxes boxes) {
        if (include == null)
            include = new ArrayList<>();
        include.add(boxes);
        
    }
    
    @Override
    public void exclude(LittleBoxes boxes) {
        if (exclude == null)
            exclude = new ArrayList<>();
        exclude.add(boxes);
    }
    
}
