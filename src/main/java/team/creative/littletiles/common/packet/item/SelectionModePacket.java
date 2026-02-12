package team.creative.littletiles.common.packet.item;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleSelector;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.math.location.TileLocation;

public class SelectionModePacket extends CreativePacket {
    
    public BlockHitResult hit;
    @CanBeNull
    public TileLocation tile;
    
    public boolean rightClick;
    public boolean secondMode;
    
    public SelectionModePacket(BlockHitResult hit, @Nullable LittleTileContext context, boolean secondMode, boolean rightClick) {
        this.hit = hit;
        if (context.isComplete())
            this.tile = new TileLocation(context);
        else
            this.tile = null;
        this.secondMode = secondMode;
        this.rightClick = rightClick;
    }
    
    public SelectionModePacket() {}
    
    @Override
    public void execute(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ILittleSelector selector && selector.hasSelection(stack)) {
            SelectionComponent sel = selector.getSelection(stack);
            try {
                if (rightClick)
                    stack.set(LittleTilesRegistry.SELECTION, sel.mode.rightClick((LittleActionSource) player, stack, sel, selector.getSelectorGrid(player, stack), hit,
                        tile != null ? tile.find(player.level()) : null, secondMode));
                else
                    stack.set(LittleTilesRegistry.SELECTION, sel.mode.leftClick((LittleActionSource) player, stack, sel, selector.getSelectorGrid(player, stack), hit,
                        tile != null ? tile.find(player.level()) : null, secondMode));
            } catch (LittleActionException e) {}
            
        }
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
