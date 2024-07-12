package team.creative.littletiles.server.action.interact;

import java.util.HashMap;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import team.creative.littletiles.common.action.LittleInteraction;

public class LittleInteractionHandlerServer {
    
    public LittleInteractionHandlerServer() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::interactEntitySpecific);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::interactEntity);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::interactBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::interactItem);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::clickBlock);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
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
    
    public void interactEntitySpecific(EntityInteractSpecific event) {
        if (!event.getLevel().isClientSide && interactions.containsKey(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    public void interactEntity(EntityInteract event) {
        if (!event.getLevel().isClientSide && interactions.containsKey(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    public void interactBlock(RightClickBlock event) {
        if (!event.getLevel().isClientSide && interactions.containsKey(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    public void interactItem(RightClickItem event) {
        if (!event.getLevel().isClientSide && interactions.containsKey(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    public void clickBlock(LeftClickBlock event) {
        if (!event.getLevel().isClientSide && interactions.containsKey(event.getEntity()))
            event.setCanceled(true);
    }
    
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        interactions.remove(event.getEntity());
    }
    
}
