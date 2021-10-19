package team.creative.littletiles.common.structure.type.premade;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.mc.InventoryUtils;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiHandler;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleBlankOMatic extends LittleStructurePremade {
    
    public SimpleContainer inventory;
    public int whiteColor;
    
    public LittleBlankOMatic(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        inventory = InventoryUtils.load(nbt, 1);
        whiteColor = nbt.getInt("white");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        InventoryUtils.save(inventory);
        nbt.putInt("white", whiteColor);
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide)
            LittleStructureGuiHandler.openGui("blankomatic", new CompoundTag(), player, this);
        return InteractionResult.SUCCESS;
    }
    
}
