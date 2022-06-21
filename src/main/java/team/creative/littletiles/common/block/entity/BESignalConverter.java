package team.creative.littletiles.common.block.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.mc.BlockSignalConverter;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureBase;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.network.SignalNetwork;

public class BESignalConverter extends BlockEntity implements ISignalStructureComponent {
    
    private SignalNetwork network;
    private SignalState inputSignalState = SignalState.create(4);
    private SignalState inputRedstoneState = SignalState.create(4);
    
    private List<SignalConnection> connections = new ArrayList<>();
    
    public BESignalConverter(BlockPos pos, BlockState state) {
        super(LittleTilesRegistry.BE_SIGNALCONVERTER_TYPE.get(), pos, state);
    }
    
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Facing side) {
        return state.isSignalSource() && side != null;
    }
    
    @Override
    public Level getStructureLevel() {
        return level;
    }
    
    @Override
    public int getBandwidth() {
        return 4;
    }
    
    @Override
    public SignalNetwork getNetwork() {
        return network;
    }
    
    @Override
    public void setNetwork(SignalNetwork network) {
        this.network = network;
    }
    
    @Override
    public Iterator<ISignalStructureBase> connections() {
        return new Iterator<ISignalStructureBase>() {
            
            Iterator<SignalConnection> iterator = connections.iterator();
            
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            
            @Override
            public ISignalStructureBase next() {
                return iterator.next().base;
            }
        };
    }
    
    @Override
    public boolean canConnect(Facing facing) {
        return true;
    }
    
    public int indexOf(ISignalStructureBase base) {
        for (int i = 0; i < connections.size(); i++)
            if (connections.get(i).base == base)
                return i;
        return -1;
    }
    
    @Override
    public boolean connect(Facing facing, ISignalStructureBase base, LittleGrid context, int distance, boolean oneSidedRenderer) {
        int index = indexOf(base);
        if (index == -1)
            connections.add(new SignalConnection(facing, base));
        return true;
    }
    
    @Override
    public void disconnect(Facing facing, ISignalStructureBase base) {
        int index = indexOf(base);
        if (index != -1)
            connections.remove(index);
    }
    
    @Override
    public SignalComponentType getComponentType() {
        return SignalComponentType.IOSPECIAL;
    }
    
    @Override
    public void updateState(SignalState state) {
        if (!state.equals(inputSignalState)) {
            inputSignalState = inputSignalState.overwrite(state);
            changed();
        }
    }
    
    @Override
    public void changed() {
        BlockState state = level.getBlockState(worldPosition);
        Direction facing = state.getValue(BlockSignalConverter.FACING);
        level.neighborChanged(worldPosition.relative(facing), state.getBlock(), worldPosition);
    }
    
    public int getPower() {
        SignalState toReturn = SignalState.create(4);
        toReturn = toReturn.or(inputRedstoneState);
        toReturn = toReturn.or(inputSignalState);
        return toReturn.number();
    }
    
    @Override
    public SignalState getState() {
        return inputRedstoneState;
    }
    
    @Override
    public void overwriteState(SignalState state) throws CorruptedConnectionException, NotYetConnectedException {
        inputRedstoneState = inputRedstoneState.overwrite(state);
        inputRedstoneState.shrinkTo(4);
    }
    
    @Override
    public LittleStructure getStructure() {
        return null;
    }
    
    @Override
    public int getId() {
        return 0;
    }
    
    @Override
    public int getColor() {
        return -1;
    }
    
    public void setPower(int level) {
        SignalState newLevel = SignalState.of(4);
        if (!inputRedstoneState.equals(newLevel)) {
            inputRedstoneState = inputRedstoneState.overwrite(newLevel);
            changed();
            findNetwork().update();
        }
    }
    
    public class SignalConnection {
        
        public final Facing facing;
        public final ISignalStructureBase base;
        
        public SignalConnection(Facing facing, ISignalStructureBase base) {
            this.facing = facing;
            this.base = base;
        }
        
    }
    
}
