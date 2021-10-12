package team.creative.littletiles.common.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntitySizeHandler {
    
    @SubscribeEvent
    public static void entitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof PrimedSizedTnt) {
            PrimedSizedTnt tnt = (PrimedSizedTnt) event.getEntity();
            event.setNewSize(new EntityDimensions((float) tnt.size.getPosX(tnt.grid), (float) tnt.size.getPosY(tnt.grid), false));
        }
    }
    
}
