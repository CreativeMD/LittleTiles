package team.creative.littletiles.common.placement.selection;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.client.tool.LittleToolSelection;
import team.creative.littletiles.client.tool.LittleToolSelection.SelectionRenderQueue;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
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
    
    public abstract SelectionScanResult scan(Level level, ItemStack stack, SelectionComponent config);
    
    public abstract SelectionComponent leftClick(LittleActionSource source, ItemStack stack, SelectionComponent config, BlockHitResult hit, @Nullable LittleTileContext context);
    
    public abstract SelectionComponent rightClick(LittleActionSource source, ItemStack stack, SelectionComponent config, BlockHitResult hit, @Nullable LittleTileContext context);
    
    public abstract LittleGroup select(Level level, LittleActionSource source, SelectionParameters selection, ItemStack stack,
            SelectionComponent config) throws LittleActionException;
    
    @OnlyIn(Dist.CLIENT)
    public void buildRender(Level level, ItemStack stack, SelectionComponent config, SelectionRenderQueue queue) {}
    
    @OnlyIn(Dist.CLIENT)
    public void tick(LittleActionSource source, Level level, ItemStack stack, SelectionComponent config, LittleToolSelection selection) {}
    
    @OnlyIn(Dist.CLIENT)
    public boolean hasRenderTick(ItemStack stack, SelectionComponent config) {
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void renderTick(LittleActionSource source, Level level, ItemStack stack, SelectionComponent config, PoseStack pose, boolean lines) {}
    
}