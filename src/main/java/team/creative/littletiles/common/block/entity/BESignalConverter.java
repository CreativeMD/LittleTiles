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
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.mc.BlockSignalConverter;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureBase;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.network.SignalNetwork;

public class BESignalConverter extends BlockEntity implements ISignalStructureComponent {
    
    private SignalNetwork network;
    private boolean[] inputSignalState = new boolean[4];
    private boolean[] inputRedstoneState = new boolean[4];
    
    private List<SignalConnection> connections = new ArrayList<>();
    
    public BESignalConverter(BlockPos pos, BlockState state) {
        super(LittleTiles.BE_SIGNALCONVERTER_TYPE, pos, state);
    }
    
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Facing side) {
        return state.isSignalSource() && side != null;
    }
    
    @Override
    public Level getStructureLevel() {
        return level;
    }
    
    @Override
    public void handleUpdate(NBTTagCompound nbt, boolean chunkUpdate) {
        readFromNBT(nbt);
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
    public void updateState(boolean[] state) {
        if (!BooleanUtils.equals(state, inputSignalState)) {
            BooleanUtils.set(inputSignalState, state);
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
        boolean[] toReturn = new boolean[4];
        BooleanUtils.or(toReturn, inputRedstoneState);
        BooleanUtils.or(toReturn, inputSignalState);
        return BooleanUtils.toNumber(toReturn);
    }
    
    @Override
    public boolean[] getState() {
        return inputRedstoneState;
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
        boolean[] newLevel = BooleanUtils.toBits(level, 4);
        if (!BooleanUtils.equals(inputRedstoneState, newLevel)) {
            BooleanUtils.set(inputRedstoneState, newLevel);
            changed();
            findNetwork().update();
        }
    }
    
    @Override
    public void unload(EnumFacing facing, ISignalStructureBase base) {
        int index = indexOf(base);
        if (index != -1)
            connections.remove(index);
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
