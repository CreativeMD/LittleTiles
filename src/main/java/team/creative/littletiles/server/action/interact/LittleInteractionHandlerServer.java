package team.creative.littletiles.server.action.interact;

import java.util.HashMap;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import team.creative.littletiles.common.action.LittleInteraction;

public class LittleInteractionHandlerServer {
    
    public LittleInteractionHandlerServer() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::interact);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogout);
    }
    
    protected HashMap<Player, LittleInteraction> interactions = new HashMap<>();
    
    public void start(Player player, int index, boolean rightclick) {
        interactions.put(player, new LittleInteraction(index, rightclick));
    }
    
    public void end(Player player, int index) {
        LittleInteraction interaction = interactions.get(player);
        if (interaction != null && interaction.index <= index)
            interactions.remove(player);
    }
    
    public void interact(PlayerInteractEvent event) {
        if (!event.getLevel().isClientSide && interactions.containsKey(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        interactions.remove(event.getEntity());
    }
    
}
