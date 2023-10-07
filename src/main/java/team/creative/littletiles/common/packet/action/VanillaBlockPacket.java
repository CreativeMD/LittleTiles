package team.creative.littletiles.common.packet.action;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleGlove;

public class VanillaBlockPacket extends CreativePacket {
    
    public static enum VanillaBlockAction {
        
        CHISEL {
            
            @Override
            public void action(Level level, Player player, BlockPos pos, BlockState state) {
                if (LittleAction.isBlockValid(state))
                    ItemLittleChisel.setElement(player.getMainHandItem(), new LittleElement(state, ColorUtils.WHITE));
            }
            
        },
        GLOVE {
            
            @Override
            public void action(Level level, Player player, BlockPos pos, BlockState state) {
                if (LittleAction.isBlockValid(state)) {
                    ItemStack stack = player.getMainHandItem();
                    ItemLittleGlove.getMode(stack).vanillaBlockAction(level, stack, pos, state);
                }
            }
            
        };
        
        public abstract void action(Level level, Player player, BlockPos pos, BlockState state);
        
    }
    
    public BlockPos pos;
    public VanillaBlockAction action;
    
    public VanillaBlockPacket(BlockPos pos, VanillaBlockAction action) {
        this.action = action;
        this.pos = pos;
    }
    
    public VanillaBlockPacket() {
        
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        action.action(player.level(), player, pos, player.level().getBlockState(pos));
        player.inventoryMenu.broadcastChanges();
    }
    
}
