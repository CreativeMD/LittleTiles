package team.creative.littletiles.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import team.creative.littletiles.common.entity.EntitySit;

public class LittleSitRenderer extends EntityRenderer<EntitySit> {
    
    public LittleSitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public ResourceLocation getTextureLocation(EntitySit animation) {
        return InventoryMenu.BLOCK_ATLAS;
    }
    
}
