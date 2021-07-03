package com.creativemd.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviewFacing;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.vec.SurroundingBox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleSignalInput extends LittleSignalCableBase implements ISignalStructureComponent {
    
    private final boolean[] state;
    @StructureDirectional
    public EnumFacing facing;
    
    public LittleSignalInput(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
        this.state = new boolean[getBandwidth()];
    }
    
    @Override
    public boolean canConnect(EnumFacing facing) {
        return facing == this.facing;
    }
    
    @Override
    public boolean[] getState() {
        return state;
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        super.loadFromNBTExtra(nbt);
        BooleanUtils.intToBool(nbt.getInteger("state"), state);
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        super.writeToNBTExtra(nbt);
        nbt.setInteger("state", BooleanUtils.boolToInt(state));
    }
    
    @Override
    public SignalComponentType getType() {
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
    @SideOnly(Side.CLIENT)
    public void renderFace(EnumFacing facing, LittleGridContext context, LittleBox renderBox, int distance, Axis axis, Axis one, Axis two, boolean positive, boolean oneSidedRenderer, List<LittleRenderBox> cubes) {
        super.renderFace(facing, context, renderBox.copy(), distance, axis, one, two, positive, oneSidedRenderer, cubes);
        
        LittleRenderBox cube = renderBox.getRenderingCube(context, LittleTiles.inputArrow, facing.ordinal());
        //cube.color = color;
        cube.keepVU = true;
        cube.allowOverlap = true;
        
        if (positive) {
            cube.setMin(axis, cube.getMax(axis));
            cube.setMax(axis, cube.getMax(axis) + (float) context.toVanillaGrid(renderBox.getSize(axis)) * 0.7F);
        } else {
            cube.setMax(axis, cube.getMin(axis));
            cube.setMin(axis, cube.getMin(axis) - (float) context.toVanillaGrid(renderBox.getSize(axis)) * 0.7F);
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
    @SideOnly(Side.CLIENT)
    public void render(SurroundingBox box, LittleBox overallBox, List<LittleRenderBox> cubes) {
        super.render(box, overallBox, cubes);
        
        AlignedBox cube = new AlignedBox(overallBox.getBox(box.getContext()));
        
        Axis axis = facing.getAxis();
        
        float sizePercentage = 0.25F;
        
        Axis one = RotationUtils.getOne(axis);
        Axis two = RotationUtils.getTwo(axis);
        
        float sizeOne = cube.getSize(one);
        float sizeTwo = cube.getSize(two);
        float sizeAxis = cube.getSize(axis);
        
        LittleRenderBox top = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(color);
        top.allowOverlap = true;
        top.setMin(one, top.getMax(one) - sizeOne * sizePercentage);
        cubes.add(top);
        
        LittleRenderBox bottom = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(color);
        bottom.allowOverlap = true;
        bottom.setMax(one, bottom.getMin(one) + sizeOne * sizePercentage);
        cubes.add(bottom);
        
        LittleRenderBox left = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(color);
        left.allowOverlap = true;
        left.setMin(two, top.getMax(two) - sizeTwo * sizePercentage);
        cubes.add(left);
        
        LittleRenderBox right = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(color);
        right.allowOverlap = true;
        right.setMax(two, right.getMin(two) + sizeTwo * sizePercentage);
        cubes.add(right);
        
        LittleRenderBox behind = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(color);
        behind.allowOverlap = true;
        
        float depth = sizeAxis * 0.12F;
        
        behind.setMin(one, behind.getMin(one) + sizeOne * sizePercentage);
        behind.setMax(one, behind.getMax(one) - sizeOne * sizePercentage);
        
        behind.setMin(two, behind.getMin(two) + sizeTwo * sizePercentage);
        behind.setMax(two, behind.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            behind.setMax(axis, behind.getMin(axis) + sizeAxis * 0.5F);
        else
            behind.setMin(axis, behind.getMax(axis) - sizeAxis * 0.5F);
        cubes.add(behind);
        
        LittleRenderBox front = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        front.allowOverlap = true;
        
        front.setMin(one, front.getMin(one) + sizeOne * sizePercentage);
        front.setMax(one, front.getMax(one) - sizeOne * sizePercentage);
        
        front.setMin(two, front.getMin(two) + sizeTwo * sizePercentage);
        front.setMax(two, front.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
            front.setMin(axis, front.getMax(axis) - sizeAxis * 0.5F);
            front.setMax(axis, front.getMax(axis) - depth);
        } else {
            front.setMax(axis, front.getMin(axis) + sizeAxis * 0.5F);
            front.setMin(axis, front.getMin(axis) + depth);
        }
        cubes.add(front);
        
        float thickness = 0.0001F;
        LittleRenderBox frontTop = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        frontTop.allowOverlap = true;
        
        frontTop.setMin(one, frontTop.getMin(one) + sizeOne * sizePercentage);
        frontTop.setMax(one, frontTop.getMin(one) + thickness);
        
        frontTop.setMin(two, frontTop.getMin(two) + sizeTwo * sizePercentage);
        frontTop.setMax(two, frontTop.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            frontTop.setMin(axis, frontTop.getMax(axis) - sizeAxis * sizePercentage);
        else
            frontTop.setMax(axis, frontTop.getMin(axis) + sizeAxis * sizePercentage);
        
        cubes.add(frontTop);
        
        LittleRenderBox frontBottom = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        frontBottom.allowOverlap = true;
        frontBottom.setMax(one, frontBottom.getMax(one) - sizeOne * sizePercentage);
        frontBottom.setMin(one, frontBottom.getMax(one) - thickness);
        
        frontBottom.setMin(two, frontBottom.getMin(two) + sizeTwo * sizePercentage);
        frontBottom.setMax(two, frontBottom.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            frontBottom.setMin(axis, frontBottom.getMax(axis) - sizeAxis * sizePercentage);
        else
            frontBottom.setMax(axis, frontBottom.getMin(axis) + sizeAxis * sizePercentage);
        
        cubes.add(frontBottom);
        
        LittleRenderBox frontRight = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        frontRight.allowOverlap = true;
        frontRight.setMin(one, frontRight.getMin(one) + sizeOne * sizePercentage);
        frontRight.setMax(one, frontRight.getMax(one) - sizeOne * sizePercentage);
        
        frontRight.setMin(two, frontRight.getMin(two) + sizeTwo * sizePercentage);
        frontRight.setMax(two, frontRight.getMin(two) + thickness);
        
        if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            frontRight.setMin(axis, frontRight.getMax(axis) - sizeAxis * sizePercentage);
        else
            frontRight.setMax(axis, frontRight.getMin(axis) + sizeAxis * sizePercentage);
        
        cubes.add(frontRight);
        
        LittleRenderBox frontLeft = (LittleRenderBox) new LittleRenderBox(cube, null, LittleTiles.dyeableBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
        
        frontLeft.allowOverlap = true;
        frontLeft.setMin(one, frontLeft.getMin(one) + sizeOne * sizePercentage);
        frontLeft.setMax(one, frontLeft.getMax(one) - sizeOne * sizePercentage);
        
        frontLeft.setMax(two, frontLeft.getMax(two) - sizeTwo * sizePercentage);
        frontLeft.setMin(two, frontLeft.getMax(two) - thickness);
        
        if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            frontLeft.setMin(axis, frontLeft.getMax(axis) - sizeAxis * sizePercentage);
        else
            frontLeft.setMax(axis, frontLeft.getMin(axis) + sizeAxis * sizePercentage);
        
        cubes.add(frontLeft);
    }
    
    @Override
    public int getIndex(EnumFacing facing) {
        return 0;
    }
    
    @Override
    public EnumFacing getFacing(int index) {
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
        public List<PlacePreview> getSpecialTiles(LittlePreviews previews) {
            List<PlacePreview> result = super.getSpecialTiles(previews);
            EnumFacing facing = (EnumFacing) loadDirectional(previews, "facing");
            LittleBox box = previews.getSurroundingBox();
            result.add(new PlacePreviewFacing(box, facing, ColorUtils.LIGHT_BLUE));
            return result;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public List<RenderBox> getRenderingCubes(LittlePreviews previews) {
            List<RenderBox> cubes = new ArrayList<>();
            float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F + 0.05) * 1.4);
            cubes = new ArrayList<>();
            cubes.add(new RenderBox(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTiles.dyeableBlock).setColor(getColor(previews)));
            cubes.add(new RenderBox(size * 2, 0.5F - size, 0.5F - size, size * 2.5F, 0.5F + size, 0.5F + size, LittleTiles.dyeableBlock).setColor(ColorUtils.LIGHT_BLUE));
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
        public SignalComponentType getType() {
            return SignalComponentType.INPUT;
        }
        
    }
    
}
