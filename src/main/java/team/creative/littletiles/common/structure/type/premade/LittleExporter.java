package team.creative.littletiles.common.structure.type.premade;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator.GuiCreatorBasic;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.gui.premade.GuiExport;

public class LittleExporter extends LittleStructurePremade {
    
    public static final GuiCreatorBasic GUI = GuiCreator.register("exporter", new GuiCreatorBasic((nbt, player) -> new GuiExport()));
    
    public LittleExporter(LittlePremadeType type, IStructureParentCollection mainBlock) {
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
            GUI.open(player);
        return InteractionResult.SUCCESS;
    }
    
}
