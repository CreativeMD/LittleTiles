package team.creative.littletiles.common.grid;

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
    
    public void convertTo(LittleGrid to);
    
    public int getSmallest();
    
    public default void convertToSmallest() {
        LittleGrid grid = LittleGrid.get(getSmallest());
        if (grid != getGrid())
            convertTo(grid);
    }
    
}
