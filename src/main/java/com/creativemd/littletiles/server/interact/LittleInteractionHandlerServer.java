package com.creativemd.littletiles.server.interact;

import java.util.HashMap;

import com.creativemd.littletiles.common.action.interact.LittleInteraction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class LittleInteractionHandlerServer {
    
    public LittleInteractionHandlerServer() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    protected HashMap<EntityPlayer, LittleInteraction> interactions = new HashMap<>();
    
    public void start(EntityPlayer player, int index, boolean rightclick) {
        interactions.put(player, new LittleInteraction(index, rightclick));
    }
    
    public void end(EntityPlayer player, int index) {
        LittleInteraction interaction = interactions.get(player);
        if (interaction != null && interaction.index <= index)
            interactions.remove(player);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent event) {
        if (!event.getWorld().isRemote && interactions.containsKey(event.getEntityPlayer())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        interactions.remove(event.player);
    }
    
}
