package team.creative.littletiles.common.grid;

import java.util.function.Supplier;

public interface IGridBased {
    
    public LittleGrid getGrid();
    
    @Deprecated
    public default void forceSameGrid(IGridBased other) {
        if (getGrid() != other.getGrid()) {
            if (getGrid().count > other.getGrid().count)
                other.convertTo(getGrid());
            else
                convertTo(other.getGrid());
        }
    }
    
    public default void sameGrid(IGridBased other, Runnable runnable) {
        if (getGrid() != other.getGrid()) {
            if (getGrid().count > other.getGrid().count)
                other.convertTo(getGrid());
            else
                convertTo(other.getGrid());
        }
        
        runnable.run();
        
        convertToSmallest();
        other.convertToSmallest();
    }
    
    public default <T> T sameGrid(IGridBased other, Supplier<T> supplier) {
        if (getGrid() != other.getGrid()) {
            if (getGrid().count > other.getGrid().count)
                other.convertTo(getGrid());
            else
                convertTo(other.getGrid());
        }
        
        T result = supplier.get();
        
        convertToSmallest();
        other.convertToSmallest();
        return result;
    }
    
    public default void unsafeSameGridRestore(IGridBased other, Runnable runnable) {
        LittleGrid thisGrid = getGrid();
        LittleGrid otherGrid = other.getGrid();
        
        if (getGrid() != other.getGrid()) {
            if (getGrid().count > other.getGrid().count)
                other.convertTo(getGrid());
            else
                convertTo(other.getGrid());
        }
        
        runnable.run();
        
        if (thisGrid != getGrid())
            convertTo(thisGrid);
        if (otherGrid != other.getGrid())
            other.convertTo(otherGrid);
    }
    
    public default <T> T unsafeSameGridRestore(IGridBased other, Supplier<T> supplier) {
        LittleGrid thisGrid = getGrid();
        LittleGrid otherGrid = other.getGrid();
        
        if (getGrid() != other.getGrid()) {
            if (getGrid().count > other.getGrid().count)
                other.convertTo(getGrid());
            else
                convertTo(other.getGrid());
        }
        
        T result = supplier.get();
        
        if (thisGrid != getGrid())
            convertTo(thisGrid);
        if (otherGrid != other.getGrid())
            other.convertTo(otherGrid);
        return result;
    }
    
    public void convertTo(LittleGrid to);
    
    public int getSmallest();
    
    public default void convertToSmallest() {
        LittleGrid grid = LittleGrid.get(getSmallest());
        if (grid != getGrid())
            convertTo(grid);
    }
    
    public default void minGrid(IGridBased other) {
        if (this.getGrid().count < other.getGrid().count)
            convertTo(other.getGrid());
    }
    
    public default void minGrid(LittleGrid grid) {
        if (this.getGrid().count < grid.count)
            convertTo(grid);
    }
}
