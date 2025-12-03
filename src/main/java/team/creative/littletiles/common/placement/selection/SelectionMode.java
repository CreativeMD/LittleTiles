package team.creative.littletiles.common.placement.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.item.component.SelectionComponent;

public abstract class SelectionMode {
    
    public static final NamedHandlerRegistry<SelectionMode> REGISTRY = new NamedHandlerRegistry<>(null);
    
    static {
        REGISTRY.registerDefault("area", new AreaSelectionMode());
    }
    
    public SelectionMode() {}
    
    public String getName() {
        return REGISTRY.getId(this);
    }
    
    public Component getTranslation() {
        return Component.translatable("mode.selection." + getName());
    }
    
    public abstract SelectionResult scan(Level level, SelectionComponent component);
    
    public abstract SelectionComponent leftClick(Player player, SelectionComponent component, BlockPos pos);
    
    public abstract SelectionComponent rightClick(Player player, SelectionComponent component, BlockPos pos);
    
    public abstract LittleGroup select(SelectionParameters selection, SelectionComponent component) throws LittleActionException;
    
}