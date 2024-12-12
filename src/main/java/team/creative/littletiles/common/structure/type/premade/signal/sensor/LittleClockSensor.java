package team.creative.littletiles.common.structure.type.premade.signal.sensor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class LittleClockSensor extends LittleStructurePremade {
    
    public LittleClockSensor(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
    @Override
    public void tick() {
        var level = getComponentLevel();
        long dayTime = level.dayTime();
        double duration = 24000;
        double fraction = Mth.clamp(dayTime / duration, 0, 1);
        getInput(0).updateState(SignalState.of((int) (fraction * 16)));
    }
    
    public static class LittleClockSensorAdvanced extends LittleStructurePremade {
        
        public LittleClockSensorAdvanced(LittlePremadeType type, IStructureParentCollection mainBlock) {
            super(type, mainBlock);
        }
        
        @Override
        protected void loadExtra(CompoundTag nbt) {}
        
        @Override
        protected void saveExtra(CompoundTag nbt) {}
        
        @Override
        public void tick() {
            getInput(0).updateState(SignalState.of((int) getComponentLevel().dayTime()));
        }
        
    }
    
}
