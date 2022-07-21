package team.creative.littletiles.client.export;

import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;

public abstract class LittleExportType {
    
    public static final NamedHandlerRegistry<LittleExportType> REGISTRY = new NamedHandlerRegistry<>(null);
    
    static {
        REGISTRY.register("data", new LittleExportTypeData());
    }
    
    public abstract String export(ItemStack stack);
    
    public static class LittleExportTypeData extends LittleExportType {
        
        @Override
        public String export(ItemStack stack) {
            return stack.getTag().toString();
        }
        
    }
    
}
