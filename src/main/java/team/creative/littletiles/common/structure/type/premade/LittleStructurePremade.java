package team.creative.littletiles.common.structure.type.premade;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;

public abstract class LittleStructurePremade extends LittleStructure {
    
    public LittleStructurePremade(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public ItemStack getStructureDrop() throws CorruptedConnectionException, NotYetConnectedException {
        ItemStack stack = LittlePremadeRegistry.createStack(type.id);
        
        checkConnections();
        BlockPos pos = getMinPos(getPos().mutable());
        
        CompoundTag structureNBT = new CompoundTag();
        this.savePreview(structureNBT, pos);
        
        if (!stack.hasTag())
            stack.setTag(new CompoundTag());
        stack.getTag().put("structure", structureNBT);
        
        if (name != null) {
            CompoundTag display = new CompoundTag();
            display.putString("Name", name);
            stack.getTag().put("display", display);
        }
        return stack;
    }
    
}
