package team.creative.littletiles.common.action;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToConvertBlockException;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public abstract class LittleAction extends CreativePacket {
    
    /** Must be implemented by every action **/
    public LittleAction() {
        
    }
    
    @OnlyIn(Dist.CLIENT)
    public abstract boolean canBeReverted();
    
    /** @return null if an revert action is not available */
    @OnlyIn(Dist.CLIENT)
    public abstract LittleAction revert(Player player) throws LittleActionException;
    
    public abstract boolean action(Player player) throws LittleActionException;
    
    @Override
    public final void executeClient(Player player) {}
    
    @Override
    public final void executeServer(ServerPlayer player) {
        try {
            action(player);
        } catch (LittleActionException e) {
            player.sendMessage(new TextComponent(e.getLocalizedMessage()), Util.NIL_UUID);
        }
    }
    
    public abstract LittleAction flip(Axis axis, LittleBoxAbsolute box);
    
    public static boolean canConvertBlock(Player player, Level level, BlockPos pos, BlockState state, int affected) throws LittleActionException {
        if (LittleTiles.CONFIG.build.get(player).limitAffectedBlocks && LittleTiles.CONFIG.build.get(player).maxAffectedBlocks < affected)
            throw new NotAllowedToConvertBlockException(player);
        if (!LittleTiles.CONFIG.build.get(player).editUnbreakable)
            return state.getBlock().defaultDestroyTime() > 0;
        return LittleTiles.CONFIG.canEditBlock(player, state, pos);
    }
    
    public static boolean canPlace(Player player) {
        GameType type = PlayerUtils.getGameType(player);
        if (type == GameType.CREATIVE || type == GameType.SURVIVAL || type == GameType.ADVENTURE)
            return true;
        return false;
    }
    
}
