package team.creative.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.network.ISignalStructureTransmitter;

public class LittleSignalCable extends LittleSignalCableBase implements ISignalStructureTransmitter {
    
    public LittleSignalCable(LittleStructureTypeCable type, IStructureParentCollection mainBlock) {
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
    public void render(SurroundingBox box, LittleBox overallBox, IndexedCollector<LittleRenderBox> cubes) {
        super.render(box, overallBox, cubes);
        
        LittleRenderBox block = new LittleRenderBox(box.getGrid(), overallBox, LittleTilesRegistry.CLEAN.value().defaultBlockState()).setColor(color);
        block.allowOverlap = true;
        cubes.add(block);
    }
    
    public static class LittleStructureTypeCable extends LittleStructureTypeNetwork {
        
        public <T extends LittleSignalCable> LittleStructureTypeCable(String id, Class<T> structureClass, BiFunction<? extends LittleStructureTypeCable, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid, int bandwidth) {
            super(id, structureClass, factory, attribute, modid, bandwidth, 6);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> getItemPreview(LittleGroup previews, boolean translucent) {
            List<RenderBox> cubes = new ArrayList<>();
            int color = getColor(previews);
            float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F + 0.05) * 1.4);
            cubes = new ArrayList<>();
            cubes.add(new RenderBox(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTilesRegistry.CLEAN.value()).setColor(color));
            cubes.add(new RenderBox(0 + size * 2, 0.5F - size * 0.8F, 0.5F - size * 0.8F, 1 - size * 2, 0.5F + size * 0.8F, 0.5F + size * 0.8F, LittleTilesRegistry.SINGLE_CABLE
                    .value()).setColor(color).setKeepUV(true));
            cubes.add(new RenderBox(1 - size * 2, 0.5F - size, 0.5F - size, 1, 0.5F + size, 0.5F + size, LittleTilesRegistry.CLEAN.value()).setColor(color));
            return cubes;
        }
        
        @Override
        public int getBandwidth() {
            return bandwidth;
        }
        
        @Override
        public void changed() {}
        
        @Override
        public SignalState getState() {
            return null;
        }
        
        @Override
        public void overwriteState(SignalState state) {}
        
        @Override
        public SignalComponentType getComponentType() {
            return SignalComponentType.TRANSMITTER;
        }
        
    }
    
}
