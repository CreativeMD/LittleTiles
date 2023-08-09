package team.creative.littletiles.common.grid;

import net.minecraft.network.chat.Component;

public class LittleGridException extends RuntimeException {
    
    public final Component translatable;
    
    public LittleGridException(int grid) {
        this("gui.error.invalid_grid", grid);
        
    }
    
    public LittleGridException(String translatable) {
        super(translatable);
        this.translatable = Component.translatable(translatable);
    }
    
    public LittleGridException(String translatable, Object... objects) {
        super(translatable);
        this.translatable = Component.translatable(translatable, objects);
    }
    
    public Component translatable() {
        return translatable;
    }
    
}
