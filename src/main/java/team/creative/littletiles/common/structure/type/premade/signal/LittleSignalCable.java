package team.creative.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.network.ISignalStructureTransmitter;
import team.creative.littletiles.common.tile.group.LittleGroup;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;

public class LittleSignalCable extends LittleSignalCableBase implements ISignalStructureTransmitter {
    
    public LittleSignalCable(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean canConnect(Facing facing) {
        return true;
    }
    
    @Override
    public int getIndex(Facing facing) {
        return facing.ordinal();
    }
    
    @Override
    public Facing getFacing(int index) {
        return Facing.values()[index];
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(SurroundingBox box, LittleBox overallBox, List<LittleRenderBox> cubes) {
        super.render(box, overallBox, cubes);
        
        AlignedBox structureBox = new AlignedBox(overallBox.getBox(box.getGrid()));
        LittleRenderBox block = (LittleRenderBox) new LittleRenderBox(structureBox, null, LittleTiles.CLEAN, 0).setColor(color);
        block.allowOverlap = true;
        cubes.add(block);
    }
    
    public static class LittleStructureTypeCable extends LittleStructureTypeNetwork {
        
        public LittleStructureTypeCable(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid, int bandwidth) {
            super(id, category, structureClass, attribute, modid, bandwidth, 6);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> getRenderingCubes(LittleGroup previews) {
            List<RenderBox> cubes = new ArrayList<>();
            int color = getColor(previews);
            float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F + 0.05) * 1.4);
            cubes = new ArrayList<>();
            cubes.add(new RenderBox(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTiles.CLEAN).setColor(color));
            cubes.add(new RenderBox(0 + size * 2, 0.5F - size * 0.8F, 0.5F - size * 0.8F, 1 - size * 2, 0.5F + size * 0.8F, 0.5F + size * 0.8F, LittleTiles.singleCable)
                    .setColor(color).setKeepUV(true));
            cubes.add(new RenderBox(1 - size * 2, 0.5F - size, 0.5F - size, 1, 0.5F + size, 0.5F + size, LittleTiles.CLEAN).setColor(color));
            return cubes;
        }
        
        @Override
        public int getBandwidth() {
            return bandwidth;
        }
        
        @Override
        public void changed() {}
        
        @Override
        public boolean[] getState() {
            return null;
        }
        
        @Override
        public SignalComponentType getComponentType() {
            return SignalComponentType.TRANSMITTER;
        }
        
    }
    
}
