package com.creativemd.littletiles.client.gui.handler;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

public abstract class LittleStructureGuiHandler extends CustomGuiHandler {
    
    public static void openGui(String id, CompoundTag nbt, Player player, LittleStructure structure) {
        nbt.setTag("location", new StructureLocation(structure).write());
        GuiHandler.openGui(id, nbt, player);
    }
    
    public abstract SubContainer getContainer(Player player, CompoundTag nbt, LittleStructure structure);
    
    @Override
    public SubContainer getContainer(Player player, CompoundTag nbt) {
        try {
            return getContainer(player, nbt, new StructureLocation(nbt.getCompoundTag("location")).find(player.world));
        } catch (LittleActionException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    public abstract SubGui getGui(Player player, CompoundTag nbt, LittleStructure structure);
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGui getGui(Player player, CompoundTag nbt) {
        try {
            return getGui(player, nbt, new StructureLocation(nbt.getCompoundTag("location")).find(player.world));
        } catch (LittleActionException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
