package team.creative.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.placement.box.LittlePlaceBox;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxFacing;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;

public class LittleSignalInput extends LittleSignalCableBase implements ISignalStructureComponent {
    
    private final boolean[] state;
    @StructureDirectional
    public Facing facing;
    
    public LittleSignalInput(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
        this.state = new boolean[getBandwidth()];
    }
    
    @Override
    public boolean canConnect(Facing facing) {
        return facing == this.facing;
    }
    
    @Override
    public boolean[] getState() {
        return state;
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        BooleanUtils.intToBool(nbt.getInt("state"), state);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        nbt.putInt("state", BooleanUtils.boolToInt(state));
    }
    
    @Override
    public SignalComponentType getComponentType() {
        return SignalComponentType.INPUT;
    }
    
    @Override
    public void changed() {
        try {
            if (getParent() != null)
                getParent().getStructure().changed(this);
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderFace(Facing facing, LittleGrid grid, LittleBox renderBox, int distance, Axis axis, Axis one, Axis two, boolean positive, boolean oneSidedRenderer, List<LittleRenderBox> cubes) {
        super.renderFace(facing, grid, renderBox.copy(), distance, axis, one, two, positive, oneSidedRenderer, cubes);
        
        LittleRenderBox cube = renderBox.getRenderingCube(grid, LittleTiles.INPUT_ARROW, facing.ordinal());
        //cube.color = color;
        cube.keepVU = true;
        cube.allowOverlap = true;
        
        if (positive) {
            cube.setMin(axis, cube.getMax(axis));
            cube.setMax(axis, cube.getMax(axis) + (float) grid.toVanillaGrid(renderBox.getSize(axis)) * 0.7F);
        } else {
            cube.setMax(axis, cube.getMin(axis));
            cube.setMin(axis, cube.getMin(axis) - (float) grid.toVanillaGrid(renderBox.getSize(axis)) * 0.7F);
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
    public void render(SurroundingBox box, LittleBox overallBox, List<LittleRenderBox> cubes) {
        super.render(box, overallBox, cubes);
        
        AlignedBox cube = new AlignedBox(overallBox.getBox(box.getGrid()));
        
        float sizePercentage = 0.25F;
        
        Axis one = facing.one();
        Axis two = facing.two();
        
        float sizeOne = cube.getSize(one);
        float sizeTwo = cube.getSize(two);
        float sizeAxis = cube.getSize(facing.axis);
        
        LittleRenderBox top = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(color);
        top.allowOverlap = true;
        top.setMin(one, top.getMax(one) - sizeOne * sizePercentage);
        cubes.add(top);
        
        LittleRenderBox bottom = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(color);
        bottom.allowOverlap = true;
        bottom.setMax(one, bottom.getMin(one) + sizeOne * sizePercentage);
        cubes.add(bottom);
        
        LittleRenderBox left = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(color);
        left.allowOverlap = true;
        left.setMin(two, top.getMax(two) - sizeTwo * sizePercentage);
        cubes.add(left);
        
        LittleRenderBox right = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(color);
        right.allowOverlap = true;
        right.setMax(two, right.getMin(two) + sizeTwo * sizePercentage);
        cubes.add(right);
        
        LittleRenderBox behind = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(color);
        behind.allowOverlap = true;
        
        behind.setMin(one, behind.getMin(one) + sizeOne * sizePercentage);
        behind.setMax(one, behind.getMax(one) - sizeOne * sizePercentage);
        
        behind.setMin(two, behind.getMin(two) + sizeTwo * sizePercentage);
        behind.setMax(two, behind.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.positive)
            behind.setMax(facing.axis, behind.getMin(facing.axis) + sizeAxis * 0.5F);
        else
            behind.setMin(facing.axis, behind.getMax(facing.axis) - sizeAxis * 0.5F);
        cubes.add(behind);
        
        LittleRenderBox front = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        front.allowOverlap = true;
        
        front.setMin(one, front.getMin(one) + sizeOne * sizePercentage);
        front.setMax(one, front.getMax(one) - sizeOne * sizePercentage);
        
        front.setMin(two, front.getMin(two) + sizeTwo * sizePercentage);
        front.setMax(two, front.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.positive) {
            front.setMin(facing.axis, front.getMax(facing.axis) - sizeAxis * 0.5F);
            front.setMax(facing.axis, front.getMax(facing.axis) - sizeAxis * sizePercentage);
        } else {
            front.setMax(facing.axis, front.getMin(facing.axis) + sizeAxis * 0.5F);
            front.setMin(facing.axis, front.getMin(facing.axis) + sizeAxis * sizePercentage);
        }
        cubes.add(front);
        
        float thickness = 0.0001F;
        LittleRenderBox frontTop = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        frontTop.allowOverlap = true;
        
        frontTop.setMin(one, frontTop.getMin(one) + sizeOne * sizePercentage);
        frontTop.setMax(one, frontTop.getMin(one) + thickness);
        
        frontTop.setMin(two, frontTop.getMin(two) + sizeTwo * sizePercentage);
        frontTop.setMax(two, frontTop.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.positive)
            frontTop.setMin(facing.axis, frontTop.getMax(facing.axis) - sizeAxis * sizePercentage);
        else
            frontTop.setMax(facing.axis, frontTop.getMin(facing.axis) + sizeAxis * sizePercentage);
        
        cubes.add(frontTop);
        
        LittleRenderBox frontBottom = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        frontBottom.allowOverlap = true;
        frontBottom.setMax(one, frontBottom.getMax(one) - sizeOne * sizePercentage);
        frontBottom.setMin(one, frontBottom.getMax(one) - thickness);
        
        frontBottom.setMin(two, frontBottom.getMin(two) + sizeTwo * sizePercentage);
        frontBottom.setMax(two, frontBottom.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.positive)
            frontBottom.setMin(facing.axis, frontBottom.getMax(facing.axis) - sizeAxis * sizePercentage);
        else
            frontBottom.setMax(facing.axis, frontBottom.getMin(facing.axis) + sizeAxis * sizePercentage);
        
        cubes.add(frontBottom);
        
        LittleRenderBox frontRight = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        frontRight.allowOverlap = true;
        frontRight.setMin(one, frontRight.getMin(one) + sizeOne * sizePercentage);
        frontRight.setMax(one, frontRight.getMax(one) - sizeOne * sizePercentage);
        
        frontRight.setMin(two, frontRight.getMin(two) + sizeTwo * sizePercentage);
        frontRight.setMax(two, frontRight.getMin(two) + thickness);
        
        if (facing.positive)
            frontRight.setMin(facing.axis, frontRight.getMax(facing.axis) - sizeAxis * sizePercentage);
        else
            frontRight.setMax(facing.axis, frontRight.getMin(facing.axis) + sizeAxis * sizePercentage);
        
        cubes.add(frontRight);
        
        LittleRenderBox frontLeft = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.CLEAN, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        frontLeft.allowOverlap = true;
        frontLeft.setMin(one, frontLeft.getMin(one) + sizeOne * sizePercentage);
        frontLeft.setMax(one, frontLeft.getMax(one) - sizeOne * sizePercentage);
        
        frontLeft.setMax(two, frontLeft.getMax(two) - sizeTwo * sizePercentage);
        frontLeft.setMin(two, frontLeft.getMax(two) - thickness);
        
        if (facing.positive)
            frontLeft.setMin(facing.axis, frontLeft.getMax(facing.axis) - sizeAxis * sizePercentage);
        else
            frontLeft.setMax(facing.axis, frontLeft.getMin(facing.axis) + sizeAxis * sizePercentage);
        
        cubes.add(frontLeft);
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
            return "i" + getId() + ":" + BooleanUtils.print(getState());
        return "";
    }
    
    public static class LittleStructureTypeInput extends LittleStructureTypeNetwork {
        
        public LittleStructureTypeInput(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid, int bandwidth) {
            super(id, category, structureClass, attribute, modid, bandwidth, 1);
        }
        
        @Override
        public List<LittlePlaceBox> getSpecialBoxes(LittleGroup group) {
            List<LittlePlaceBox> result = super.getSpecialBoxes(group);
            Facing facing = (Facing) loadDirectional(group, "facing");
            LittleBox box = group.getSurroundingBox();
            result.add(new LittlePlaceBoxFacing(box, facing, ColorUtils.LIGHT_BLUE));
            return result;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> getRenderingCubes(LittleGroup previews) {
            List<RenderBox> cubes = new ArrayList<>();
            float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F + 0.05) * 1.4);
            cubes = new ArrayList<>();
            cubes.add(new RenderBox(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTiles.CLEAN).setColor(getColor(previews)));
            cubes.add(new RenderBox(size * 2, 0.5F - size, 0.5F - size, size * 2.5F, 0.5F + size, 0.5F + size, LittleTiles.CLEAN).setColor(ColorUtils.LIGHT_BLUE));
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
        public boolean[] getState() {
            return null;
        }
        
        @Override
        public SignalComponentType getComponentType() {
            return SignalComponentType.INPUT;
        }
        
    }
    
}
