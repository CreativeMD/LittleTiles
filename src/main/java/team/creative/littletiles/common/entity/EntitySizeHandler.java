package team.creative.littletiles.common.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class EntitySizeHandler {
    
    @SubscribeEvent
    public static void entitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof PrimedSizedTnt tnt && tnt.grid != null)
            event.setNewSize(EntityDimensions.scalable((float) tnt.size.getPosX(tnt.grid), (float) tnt.size.getPosY(tnt.grid)));
    }
    
}
