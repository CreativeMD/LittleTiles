package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.ArrayList;
import java.util.List;

public class RecipeTestResults {
    
    private List<RecipeTestError> errors = new ArrayList<>();
    
    public RecipeTestResults() {}
    
    public void reportError(RecipeTestError error) {
        errors.add(error);
    }
    
    public boolean success() {
        return errors.isEmpty();
    }
    
}
