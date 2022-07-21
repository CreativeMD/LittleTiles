package team.creative.littletiles.common.gui.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.math.location.TileLocation;

@FunctionalInterface
public interface LittleTileGuiHandler extends GuiHandler {
    
    public static void openGui(String id, CompoundTag nbt, Player player, LittleTileContext context) {
        nbt.put("location", new TileLocation(context).write(new CompoundTag()));
        GuiHandler.openGui(id, nbt, player);
    }
    
    @Override
    public default GuiLayer create(Player player, CompoundTag nbt) {
        try {
            return create(player, nbt, new TileLocation(nbt.getCompound("location")).find(player.level));
        } catch (LittleActionException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public GuiLayer create(Player player, CompoundTag nbt, LittleTileContext context);
    
}
