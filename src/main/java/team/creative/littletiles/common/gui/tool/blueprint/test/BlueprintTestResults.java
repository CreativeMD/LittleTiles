package team.creative.littletiles.common.gui.tool.blueprint.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlueprintTestResults implements Iterable<BlueprintTestError> {
    
    private List<BlueprintTestError> errors = new ArrayList<>();
    
    public BlueprintTestResults() {}
    
    public void reportError(BlueprintTestError error) {
        errors.add(error);
    }
    
    public boolean success() {
        return errors.isEmpty();
    }
    
    @Override
    public Iterator<BlueprintTestError> iterator() {
        return errors.iterator();
    }
    
    public int errorCount() {
        return errors.size();
    }
    
}
