package com.creativemd.littletiles.server.world;

import java.util.HashSet;
import java.util.Set;

import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.LittleAnimationHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class LittleAnimationHandlerServer extends LittleAnimationHandler {
    
    public LittleAnimationHandlerServer(World world) {
        super(world);
    }
    
    public void tick(WorldTickEvent event) {
        if (event.phase == Phase.END && world == event.world) {
            for (EntityAnimation door : openDoors) {
                
                if (door.world != world || door.world instanceof CreativeWorld)
                    continue;
                
                door.onUpdateForReal();
            }
            
            openDoors.removeIf((x) -> {
                if (x.isDead) {
                    x.markRemoved();
                    return true;
                }
                return false;
            });
        }
    }
    
    private Set<EntityPlayer> queued = new HashSet<>();
    
    public void queueClickThrough(EntityPlayer player) {
        queued.add(player);
    }
    
    public void rightClick(PlayerInteractEvent event) {
        if (event instanceof RightClickBlock && queued.contains(event.getEntityPlayer())) {
            event.setCanceled(true);
            queued.remove(event.getEntityPlayer());
        }
    }
    
}
