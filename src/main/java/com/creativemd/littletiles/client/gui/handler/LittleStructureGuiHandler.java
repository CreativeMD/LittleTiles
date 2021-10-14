package com.creativemd.littletiles.client.gui.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

@FunctionalInterface
public interface LittleStructureGuiHandler extends GuiHandler {
    
    public static void openGui(String id, CompoundTag nbt, Player player, LittleStructure structure) {
        nbt.put("location", new StructureLocation(structure).write(new CompoundTag()));
        GuiHandler.openGui(id, nbt, player);
    }
    
    @Override
    public default GuiLayer create(Player player, CompoundTag nbt) {
        try {
            return create(player, nbt, new StructureLocation(nbt.getCompound("location")).find(player.level));
        } catch (LittleActionException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public abstract GuiLayer create(Player player, CompoundTag nbt, LittleStructure structure);
    
}
