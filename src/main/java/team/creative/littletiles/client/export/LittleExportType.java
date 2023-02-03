package team.creative.littletiles.client.export;

import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.item.ItemMultiTiles;

public abstract class LittleExportType {
    
    public static final NamedHandlerRegistry<LittleExportType> REGISTRY = new NamedHandlerRegistry<>(null);
    
    static {
        REGISTRY.register("data", new LittleExportTypeData());
    }
    
    public abstract String export(ItemStack stack);
    
    public static class LittleExportTypeData extends LittleExportType {
        
        @Override
        public String export(ItemStack stack) {
            if (stack.getItem() instanceof ItemLittleBlueprint)
                return stack.getOrCreateTagElement(ItemLittleBlueprint.CONTENT_KEY).toString();
            if (stack.getItem() instanceof ItemMultiTiles)
                return stack.getOrCreateTag().toString();
            return "";
        }
        
    }
    
}
