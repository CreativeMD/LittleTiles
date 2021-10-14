package team.creative.littletiles.client.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PickBlockEvent extends Event {
    
    public final Level level;
    public final Player player;
    public final BlockHitResult result;
    
    public PickBlockEvent(Level level, Player player, BlockHitResult result) {
        this.level = level;
        this.player = player;
        this.result = result;
    }
    
}
