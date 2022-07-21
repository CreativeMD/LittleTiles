package team.creative.littletiles.common.gui.handler;

import net.minecraft.data.models.blockstates.PropertyDispatch.TriFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

public class LittleStructureGuiCreator extends GuiCreator {
    
    public LittleStructureGuiCreator(TriFunction<CompoundTag, Player, LittleStructure, GuiLayer> function) {
        super((nbt, player) -> {
            try {
                return function.apply(nbt, player, new StructureLocation(nbt.getCompound("location")).find(player.level));
            } catch (LittleActionException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
    
    public void open(Player player, LittleStructure structure) {
        open(new CompoundTag(), player, structure);
    }
    
    public void open(CompoundTag nbt, Player player, LittleStructure structure) {
        nbt.put("location", new StructureLocation(structure).write(new CompoundTag()));
        openGui(nbt, player);
    }
    
}
