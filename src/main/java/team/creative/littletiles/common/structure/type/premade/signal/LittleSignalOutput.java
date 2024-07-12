package team.creative.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.placement.box.LittlePlaceBox;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxFacing;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;

public class LittleSignalOutput extends LittleSignalCableBase implements ISignalStructureComponent {
    
    private SignalState state;
    @StructureDirectional
    public Facing facing;
    
    public LittleSignalOutput(LittleStructureTypeOutput type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
        this.state = SignalState.create(getBandwidth());
    }
    
    @Override
    public boolean canConnect(Facing facing) {
        return facing == this.facing;
    }
    
    @Override
    public SignalState getState() {
        return state;
    }
    
    @Override
    public void overwriteState(SignalState state) {
        this.state = this.state.overwrite(state);
        this.state.shrinkTo(getBandwidth());
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        state = state.load(nbt.get("state"));
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        nbt.put("state", state.save());
    }
    
    @Override
    public SignalComponentType getComponentType() {
        return SignalComponentType.OUTPUT;
    }
    
    @Override
    public void changed() {
        findNetwork().update();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderFace(Facing facing, LittleGrid grid, LittleBox renderBox, int distance, Axis axis, Axis one, Axis two, boolean positive, boolean oneSidedRenderer, IndexedCollector<LittleRenderBox> cubes) {
        super.renderFace(facing, grid, renderBox.copy(), distance, axis, one, two, positive, oneSidedRenderer, cubes);
        
        LittleRenderBox cube = renderBox.getRenderingBox(grid, LittleTilesRegistry.OUTPUT_ARROW.get().defaultBlockState().setValue(BlockStateProperties.FACING, facing
                .toVanilla()));
        cube.keepVU = true;
        cube.allowOverlap = true;
        
        if (positive) {
            cube.setMin(axis, cube.getMax(axis));
            cube.setMax(axis, cube.getMax(axis) + grid.toVanillaGridF(renderBox.getSize(axis)) * 0.7F);
        } else {
            cube.setMax(axis, cube.getMin(axis));
            cube.setMin(axis, cube.getMin(axis) - grid.toVanillaGridF(renderBox.getSize(axis)) * 0.7F);
        }
        float shrink = 0.14F;
        float shrinkOne = cube.getSize(one) * shrink;
        float shrinkTwo = cube.getSize(two) * shrink;
        cube.setMin(one, cube.getMin(one) + shrinkOne);
        cube.setMax(one, cube.getMax(one) - shrinkOne);
        cube.setMin(two, cube.getMin(two) + shrinkTwo);
        cube.setMax(two, cube.getMax(two) - shrinkTwo);
        cubes.add(cube);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(SurroundingBox box, LittleBox overallBox, IndexedCollector<LittleRenderBox> cubes) {
        super.render(box, overallBox, cubes);
        
        AlignedBox structureBox = new AlignedBox(overallBox.getBox(box.getGrid()));
        LittleRenderBox block = new LittleRenderBox(structureBox, LittleTilesRegistry.CLEAN.get().defaultBlockState()).setColor(color);
        block.allowOverlap = true;
        cubes.add(block);
        
        LittleRenderBox cube = new LittleRenderBox(structureBox, LittleTilesRegistry.CLEAN.get().defaultBlockState());
        cube.setColor(ColorUtils.ORANGE);
        
        float thickness = cube.getSize(facing.axis) * 0.25F;
        float sizePercentage = 0.25F;
        
        Axis one = facing.one();
        Axis two = facing.two();
        
        float sizeOne = cube.getSize(one);
        cube.setMin(one, cube.getMin(one) + sizeOne * sizePercentage);
        cube.setMax(one, cube.getMax(one) - sizeOne * sizePercentage);
        
        float sizeTwo = cube.getSize(two);
        cube.setMin(two, cube.getMin(two) + sizeTwo * sizePercentage);
        cube.setMax(two, cube.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.positive) {
            cube.setMin(facing.axis, cube.getMax(facing.axis));
            cube.setMax(facing.axis, cube.getMax(facing.axis) + thickness);
        } else {
            cube.setMax(facing.axis, cube.getMin(facing.axis));
            cube.setMin(facing.axis, cube.getMin(facing.axis) - thickness);
        }
        cubes.add(cube);
    }
    
    @Override
    public int getIndex(Facing facing) {
        return 0;
    }
    
    @Override
    public Facing getFacing(int index) {
        return facing;
    }
    
    @Override
    public int getId() {
        return getParent().childId;
    }
    
    @Override
    public LittleStructure getStructure() {
        try {
            return getParent().getStructure();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String info() {
        if (getParent() != null)
            return "o" + getId() + ":" + getState().print(getBandwidth());
        return "";
    }
    
    public static class LittleStructureTypeOutput extends LittleStructureTypeNetwork {
        
        public <T extends LittleSignalOutput> LittleStructureTypeOutput(String id, Class<T> structureClass, BiFunction<? extends LittleStructureTypeOutput, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid, int bandwidth) {
            super(id, structureClass, factory, attribute, modid, bandwidth, 1);
        }
        
        @Override
        public List<LittlePlaceBox> getSpecialBoxes(LittleGroup group) {
            List<LittlePlaceBox> result = super.getSpecialBoxes(group);
            Facing facing = (Facing) loadDirectional(group, "facing");
            LittleBox box = group.getSurroundingBox();
            result.add(new LittlePlaceBoxFacing(box, facing, ColorUtils.ORANGE));
            return result;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> getItemPreview(LittleGroup previews, boolean translucent) {
            List<RenderBox> cubes = new ArrayList<>();
            float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F + 0.05) * 1.4);
            cubes = new ArrayList<>();
            cubes.add(new RenderBox(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTilesRegistry.CLEAN.get()).setColor(getColor(previews)));
            cubes.add(new RenderBox(size * 2, 0.5F - size, 0.5F - size, size * 2.5F, 0.5F + size, 0.5F + size, LittleTilesRegistry.CLEAN.get()).setColor(ColorUtils.ORANGE));
            return cubes;
        }
        
        @Override
        public int getBandwidth() {
            return bandwidth;
        }
        
        @Override
        public void changed() {
            
        }
        
        @Override
        public SignalState getState() {
            return null;
        }
        
        @Override
        public void overwriteState(SignalState state) {}
        
        @Override
        public SignalComponentType getComponentType() {
            return SignalComponentType.OUTPUT;
        }
        
    }
    
}
