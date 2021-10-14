package team.creative.littletiles.client.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class HoldLeftClick extends Event {
    
    public final Level level;
    public final Player player;
    
    public final boolean leftClick;
    
    private boolean leftClickResult;
    
    public HoldLeftClick(Level level, Player player, boolean leftClick) {
        this.level = level;
        this.player = player;
        this.leftClick = leftClick;
        this.leftClickResult = leftClick;
    }
    
    public void setLeftClickResult(boolean leftClick) {
        this.leftClickResult = leftClick;
    }
    
    public boolean getLeftClickResult() {
        return leftClickResult;
    }
    
}
