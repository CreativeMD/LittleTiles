package team.creative.littletiles.common.structure.type.premade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.inventory.InventoryUtils;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;

public class LittleBlankOMatic extends LittleStructurePremade {
    
    public SimpleContainer inventory;
    public int whiteColor;
    
    public LittleBlankOMatic(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        inventory = InventoryUtils.load(getStructureLevel().registryAccess(), nbt.getCompound("inv"), 1);
        inventory.addListener(x -> markDirty());
        whiteColor = nbt.getInt("white");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        nbt.put("inv", InventoryUtils.save(getStructureLevel().registryAccess(), inventory));
        nbt.putInt("white", whiteColor);
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide)
            LittleTilesGuiRegistry.BLANKOMATIC.open(player, this);
        return InteractionResult.SUCCESS;
    }
    
}
