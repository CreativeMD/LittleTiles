package team.creative.littletiles.common.placement.shape;

import java.util.HashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import team.creative.creativecore.common.util.registry.ConfigTypeRegistry;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.creativecore.common.util.type.tree.NamedTree;
import team.creative.littletiles.common.placement.shape.config.AxisHollowThicknessShapeConfig;
import team.creative.littletiles.common.placement.shape.config.AxisShapeConfig;
import team.creative.littletiles.common.placement.shape.config.AxisThicknessShapeConfig;
import team.creative.littletiles.common.placement.shape.config.BrushSizeShapeConfig;
import team.creative.littletiles.common.placement.shape.config.CornerShapeConfig;
import team.creative.littletiles.common.placement.shape.config.FacingShapeConfig;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig;
import team.creative.littletiles.common.placement.shape.config.InterpolationAxisThicknessConfig;
import team.creative.littletiles.common.placement.shape.config.InterpolationThicknessConfig;
import team.creative.littletiles.common.placement.shape.config.LittleShapeConfig;
import team.creative.littletiles.common.placement.shape.config.MatrixShapeConfig;
import team.creative.littletiles.common.placement.shape.config.PillarShapeConfig;
import team.creative.littletiles.common.placement.shape.type.LittleShapeBlob;
import team.creative.littletiles.common.placement.shape.type.LittleShapeConnected;
import team.creative.littletiles.common.placement.shape.type.LittleShapeCube;
import team.creative.littletiles.common.placement.shape.type.LittleShapeCurve;
import team.creative.littletiles.common.placement.shape.type.LittleShapeCurveWall;
import team.creative.littletiles.common.placement.shape.type.LittleShapeCylinder;
import team.creative.littletiles.common.placement.shape.type.LittleShapeDragBox;
import team.creative.littletiles.common.placement.shape.type.LittleShapeInnerCorner;
import team.creative.littletiles.common.placement.shape.type.LittleShapeOuterCorner;
import team.creative.littletiles.common.placement.shape.type.LittleShapePillar;
import team.creative.littletiles.common.placement.shape.type.LittleShapePixel;
import team.creative.littletiles.common.placement.shape.type.LittleShapePolygon;
import team.creative.littletiles.common.placement.shape.type.LittleShapePyramid;
import team.creative.littletiles.common.placement.shape.type.LittleShapeSlope;
import team.creative.littletiles.common.placement.shape.type.LittleShapeSphere;
import team.creative.littletiles.common.placement.shape.type.LittleShapeTile;
import team.creative.littletiles.common.placement.shape.type.LittleShapeType;
import team.creative.littletiles.common.placement.shape.type.LittleShapeWall;

public class ShapeRegistry {
    
    public static final LittleShapeTile TILE_SHAPE = new LittleShapeTile();
    public static final LittleShapeDragBox DRAG_BOX = new LittleShapeDragBox();
    public static final LittleShapeSphere DRAG_SPHERE = new LittleShapeSphere();
    public static final NamedHandlerRegistry<LittleShape> REGISTRY = new NamedHandlerRegistry<LittleShape>(DRAG_BOX);
    public static final ConfigTypeRegistry<LittleShapeConfig> SHAPE_CONFIG_REGISTRY = new ConfigTypeRegistry();
    public static final NamedTree<LittleShape> SHAPE_TREE = new NamedTree<>();
    private static final HashMap<String, String> SHAPE_TO_CONFIG = new HashMap<>();
    
    public static <T extends LittleShapeConfig> void registerConfig(String id, Class<T> clazz, Supplier<T> supplier) {
        SHAPE_CONFIG_REGISTRY.register(id, clazz, supplier.get(), supplier);
    }
    
    private static LittleShape registerShapeInternal(String id, LittleShape shape, String path) {
        REGISTRY.register(id, shape);
        SHAPE_TREE.add(path + "." + id, shape);
        shape.setTranslation("shape." + path + "." + id);
        return shape;
    }
    
    public static LittleShape registerShape(String id, LittleShape<Void> shape, String path) {
        return registerShapeInternal(id, shape, path);
    }
    
    public static <T extends LittleShapeConfig> LittleShape registerShape(String id, LittleShape<T> shape, @Nullable Class<T> configClass, String path) {
        if (configClass != null) {
            if (!SHAPE_CONFIG_REGISTRY.contains(configClass))
                throw new IllegalArgumentException("Config class for shape " + id + " is not registered");
            SHAPE_TO_CONFIG.put(id, SHAPE_CONFIG_REGISTRY.getId(configClass));
        }
        return registerShapeInternal(id, shape, path);
    }
    
    public static LittleShape get(String name) {
        return REGISTRY.get(name);
    }
    
    public static String getConfig(LittleShape shape) {
        return SHAPE_TO_CONFIG.get(REGISTRY.getId(shape));
    }
    
    public static LittleShapeConfig createConfigDefault(LittleShape shape) {
        String id = getConfig(shape);
        if (id != null)
            return SHAPE_CONFIG_REGISTRY.createDefault(id);
        return null;
    }
    
    static {
        registerConfig("axis", AxisShapeConfig.class, AxisShapeConfig::new);
        registerConfig("aht", AxisHollowThicknessShapeConfig.class, AxisHollowThicknessShapeConfig::new);
        registerConfig("at", AxisThicknessShapeConfig.class, AxisThicknessShapeConfig::new);
        registerConfig("corner", CornerShapeConfig.class, CornerShapeConfig::new);
        registerConfig("facing", FacingShapeConfig.class, FacingShapeConfig::new);
        registerConfig("hollow", HollowThicknessConfig.class, HollowThicknessConfig::new);
        registerConfig("iat", InterpolationAxisThicknessConfig.class, InterpolationAxisThicknessConfig::new);
        registerConfig("it", InterpolationThicknessConfig.class, InterpolationThicknessConfig::new);
        registerConfig("matrix", MatrixShapeConfig.class, MatrixShapeConfig::new);
        registerConfig("pillar", PillarShapeConfig.class, PillarShapeConfig::new);
        registerConfig("size", BrushSizeShapeConfig.class, BrushSizeShapeConfig::new);
        
        registerShape("pixel", new LittleShapePixel(), "brush");
        registerShape("blob", new LittleShapeBlob(), BrushSizeShapeConfig.class, "brush");
        registerShape("cube", new LittleShapeCube(), BrushSizeShapeConfig.class, "brush");
        
        registerShape("box", DRAG_BOX, HollowThicknessConfig.class, "drag");
        
        registerShape("wall", new LittleShapeWall(), AxisThicknessShapeConfig.class, "drag");
        registerShape("curve", new LittleShapeCurve(), InterpolationThicknessConfig.class, "drag");
        registerShape("curvewall", new LittleShapeCurveWall(), InterpolationAxisThicknessConfig.class, "drag");
        
        registerShape("polygon", new LittleShapePolygon(), "drag");
        
        registerShape("cylinder", new LittleShapeCylinder(), AxisHollowThicknessShapeConfig.class, "drag");
        registerShape("sphere", DRAG_SPHERE, HollowThicknessConfig.class, "drag");
        
        registerShape("pillar", new LittleShapePillar(), PillarShapeConfig.class, "drag");
        registerShape("pyramid", new LittleShapePyramid(), FacingShapeConfig.class, "drag");
        
        registerShape("slope", new LittleShapeSlope(), MatrixShapeConfig.class, "drag.slope");
        registerShape("inner_corner", new LittleShapeInnerCorner(), CornerShapeConfig.class, "drag.slope");
        registerShape("outer_corner", new LittleShapeOuterCorner(), CornerShapeConfig.class, "drag.slope");
        
        registerShape("tile", TILE_SHAPE, "tiles");
        registerShape("same_type", new LittleShapeType(), "tiles");
        registerShape("connected", new LittleShapeConnected(), "tiles");
        
    }
    
}
