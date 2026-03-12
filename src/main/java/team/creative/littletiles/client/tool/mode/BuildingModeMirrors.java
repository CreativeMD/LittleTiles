package team.creative.littletiles.client.tool.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.type.tree.NamedTree;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeRules.BuildingModeRule;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public class BuildingModeMirrors extends BuildingModeFeature implements BuildingModeRule {
    
    private List<MirrorPlane> mirrors = new ArrayList<>();
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void populate(NamedTree<BooleanSupplier> tree) {
        // TODO Auto-generated method stub
        tree.add("mirror.add", null);
    }
    
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }
    
    private static class MirrorPlane {
        
        private LittleBoxAbsolute origin;
        private Axis axis;
        private int color;
        
    }
    
}
