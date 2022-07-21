package team.creative.littletiles.client.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class WheelClick extends Event {
    
    public final Level level;
    public final Player player;
    
    public WheelClick(Level level, Player player) {
        this.level = level;
        this.player = player;
    }
    
}
