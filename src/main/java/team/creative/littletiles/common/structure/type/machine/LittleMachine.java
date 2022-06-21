package team.creative.littletiles.common.structure.type.machine;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.common.animation.program.StateProgram;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.logic.StructureAction;

public abstract class LittleMachine extends LittleStructure {
    
    private static final NamedHandlerRegistry<StateProgram<StructureAction>> PROGRAM_REGISTRY = new NamedHandlerRegistry<StateProgram<StructureAction>>(null);
    
    public static void initMachines() {
        PROGRAM_REGISTRY.register("particle", new StateProgram<StructureAction>());
    }
    
    public StateProgram<StructureAction> program;
    
    public LittleMachine(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    public abstract boolean isDefaultProgram();
    
    @Override
    protected void loadExtra(CompoundTag nbt) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
}
