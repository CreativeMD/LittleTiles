package team.creative.littletiles.common.structure.signal.component;

import java.util.Iterator;

import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.structure.signal.network.SignalNetwork;

public class InvalidSignalComponent implements ISignalStructureBase {
    
    public static final InvalidSignalComponent INSTANCE = new InvalidSignalComponent();
    
    private InvalidSignalComponent() {}
    
    @Override
    public Level getStructureLevel() {
        return null;
    }
    
    @Override
    public int getBandwidth() {
        return 0;
    }
    
    @Override
    public SignalNetwork getNetwork() {
        return null;
    }
    
    @Override
    public void setNetwork(SignalNetwork network) {}
    
    @Override
    public Iterator<ISignalStructureBase> connections() {
        return null;
    }
    
    @Override
    public boolean canConnect(Facing facing) {
        return false;
    }
    
    @Override
    public boolean connect(Facing facing, ISignalStructureBase base, LittleGrid grid, int distance, boolean oneSidedRenderer) {
        return false;
    }
    
    @Override
    public void disconnect(Facing facing, ISignalStructureBase base) {}
    
    @Override
    public SignalComponentType getComponentType() {
        return SignalComponentType.INVALID;
    }
    
    @Override
    public int getColor() {
        return 0;
    }
    
    @Override
    public void unload(Facing facing, ISignalStructureBase base) {}
    
}
