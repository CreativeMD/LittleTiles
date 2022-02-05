package team.creative.littletiles.common.placement.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.common.placement.shape.type.LittleShapeBox;
import team.creative.littletiles.common.placement.shape.type.LittleShapeConnected;
import team.creative.littletiles.common.placement.shape.type.LittleShapeCurve;
import team.creative.littletiles.common.placement.shape.type.LittleShapeCurveWall;
import team.creative.littletiles.common.placement.shape.type.LittleShapeCylinder;
import team.creative.littletiles.common.placement.shape.type.LittleShapeInnerCorner;
import team.creative.littletiles.common.placement.shape.type.LittleShapeOuterCorner;
import team.creative.littletiles.common.placement.shape.type.LittleShapePillar;
import team.creative.littletiles.common.placement.shape.type.LittleShapePolygon;
import team.creative.littletiles.common.placement.shape.type.LittleShapePyramid;
import team.creative.littletiles.common.placement.shape.type.LittleShapeSlice;
import team.creative.littletiles.common.placement.shape.type.LittleShapeSphere;
import team.creative.littletiles.common.placement.shape.type.LittleShapeTile;
import team.creative.littletiles.common.placement.shape.type.LittleShapeType;
import team.creative.littletiles.common.placement.shape.type.LittleShapeWall;

public class ShapeRegistry {
    
    public static final LittleShape TILE_SHAPE = new LittleShapeTile();
    public static final LittleShape DEFAULT_SHAPE = new LittleShapeBox();
    public static final NamedHandlerRegistry<LittleShape> REGISTRY = new NamedHandlerRegistry<LittleShape>(TILE_SHAPE);
    private static final HashMapList<ShapeType, String> SHAPE_TYPES = new HashMapList<>();
    private static final List<LittleShape> NO_TILE_LIST = new ArrayList<>();
    private static final List<LittleShape> PLACING_LIST = new ArrayList<>();
    
    public static Collection<LittleShape> notTileShapes() {
        return NO_TILE_LIST;
    }
    
    public static Collection<LittleShape> placingShapes() {
        return PLACING_LIST;
    }
    
    public static LittleShape registerShape(String id, LittleShape shape, ShapeType type) {
        REGISTRY.register(id, shape);
        SHAPE_TYPES.add(type, id);
        if (type != ShapeType.DEFAULT_SELECTOR)
            NO_TILE_LIST.add(shape);
        if (type == ShapeType.SELECTOR || type == ShapeType.SHAPE || type == ShapeType.DEFAULT_SELECTOR)
            PLACING_LIST.add(shape);
        return shape;
    }
    
    public static LittleShape get(String name) {
        return REGISTRY.get(name);
    }
    
    static {
        registerShape("tile", TILE_SHAPE, ShapeType.DEFAULT_SELECTOR);
        registerShape("type", new LittleShapeType(), ShapeType.SELECTOR);
        registerShape("box", DEFAULT_SHAPE, ShapeType.SHAPE);
        registerShape("connected", new LittleShapeConnected(), ShapeType.SELECTOR);
        
        registerShape("slice", new LittleShapeSlice(), ShapeType.SHAPE);
        registerShape("inner_corner", new LittleShapeInnerCorner(), ShapeType.SHAPE);
        registerShape("outer_corner", new LittleShapeOuterCorner(), ShapeType.SHAPE);
        
        registerShape("polygon", new LittleShapePolygon(), ShapeType.SHAPE);
        
        registerShape("wall", new LittleShapeWall(), ShapeType.SHAPE);
        registerShape("pillar", new LittleShapePillar(), ShapeType.SHAPE);
        registerShape("curve", new LittleShapeCurve(), ShapeType.SHAPE);
        registerShape("curvewall", new LittleShapeCurveWall(), ShapeType.SHAPE);
        
        registerShape("cylinder", new LittleShapeCylinder(), ShapeType.SHAPE);
        registerShape("sphere", new LittleShapeSphere(), ShapeType.SHAPE);
        registerShape("pyramid", new LittleShapePyramid(), ShapeType.SHAPE);
        
    }
    
    public static enum ShapeType {
        
        DEFAULT_SELECTOR,
        SELECTOR,
        SHAPE
        
    }
    
}
