package team.creative.littletiles.common.block.mc;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.entity.BESignalConverter;

public class BlockSignalConverter extends BaseEntityBlock {
    
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    public BlockSignalConverter() {
        super(BlockBehaviour.Properties.of(Material.METAL));
    }
    
    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos origin, boolean p_60514_) {
        super.neighborChanged(state, level, pos, block, origin, p_60514_);
        changed(level, pos);
    }
    
    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        changed(level, pos);
    }
    
    public void changed(LevelReader level, BlockPos pos) {
        Optional<BESignalConverter> result = level.getBlockEntity(pos, LittleTilesRegistry.BE_SIGNALCONVERTER_TYPE.get());
        if (result.isEmpty())
            return;
        
        Direction direction = level.getBlockState(pos).getValue(FACING);
        
        if (!level.isClientSide())
            result.get().setPower(getPowerOnSide(level, pos.relative(direction.getOpposite()), direction));
    }
    
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }
    
    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (state.getValue(FACING) == side.getOpposite()) {
            Optional<BESignalConverter> result = level.getBlockEntity(pos, LittleTilesRegistry.BE_SIGNALCONVERTER_TYPE.get());
            if (result.isPresent())
                return result.get().getPower();
        }
        return 0;
    }
    
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return state.getValue(FACING) == direction;
    }
    
    public static int getPowerOnSide(LevelReader level, BlockPos pos, Direction side) {
        BlockState iblockstate = level.getBlockState(pos);
        if (iblockstate.isSignalSource())
            return level.getDirectSignal(pos, side);
        return 0;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BESignalConverter(pos, state);
    }
    
}
