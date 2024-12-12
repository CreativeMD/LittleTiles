package team.creative.littletiles.common.structure.type.premade.signal.sensor;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class LittleDayLightSensor extends LittleStructurePremade {
    
    public LittleDayLightSensor(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
    @Override
    public void tick() {
        getInput(0).updateState(SignalState.of(getComponentLevel().isDay()));
    }
    
}
