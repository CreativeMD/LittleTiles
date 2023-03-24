package team.creative.littletiles.common.structure.type.premade;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiCreator;
import team.creative.littletiles.common.gui.structure.GuiWorkbench;

public class LittleWorkbench extends LittleStructurePremade {
    
    public static final LittleStructureGuiCreator GUI = GuiCreator.register("workbench", new LittleStructureGuiCreator((nbt, player, structure) -> new GuiWorkbench()));
    
    public LittleWorkbench(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide)
            GUI.open(player, this);
        return InteractionResult.SUCCESS;
    }
    
}
