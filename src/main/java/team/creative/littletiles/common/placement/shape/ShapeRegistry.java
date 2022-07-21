package team.creative.littletiles.common.placement.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import team.creative.creativecore.common.util.type.HashMapList;
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
    
    private static LinkedHashMap<String, LittleShape> shapes = new LinkedHashMap<>();
    
    private static HashMapList<ShapeType, String> shapeTypeLists = new HashMapList<>();
    private static List<String> noTileList = new ArrayList<>();
    private static List<String> placingList = new ArrayList<>();
    
    private static LittleShape defaultShape;
    public static LittleShape tileShape;
    
    public static Collection<LittleShape> shapes() {
        return shapes.values();
    }
    
    public static Collection<String> allShapeNames() {
        return shapes.keySet();
    }
    
    public static Collection<String> notTileShapeNames() {
        return noTileList;
    }
    
    public static Collection<String> placingShapeNames() {
        return placingList;
    }
    
    public static LittleShape registerShape(String id, LittleShape shape, ShapeType type) {
        shapes.put(id, shape);
        shapeTypeLists.add(type, id);
        if (type != ShapeType.DEFAULT_SELECTOR)
            noTileList.add(id);
        placingList.clear();
        placingList.addAll(shapeTypeLists.tryGet(ShapeType.SHAPE));
        placingList.addAll(shapeTypeLists.tryGet(ShapeType.DEFAULT_SELECTOR));
        placingList.addAll(shapeTypeLists.tryGet(ShapeType.SELECTOR));
        return shape;
    }
    
    public static LittleShape getShape(String name) {
        return shapes.getOrDefault(name, defaultShape);
    }
    
    public static String getShapeName(LittleShape shape) {
        for (Entry<String, LittleShape> entry : shapes.entrySet())
            if (entry.getValue() == shape)
                return entry.getKey();
        return null;
    }
    
    static {
        tileShape = registerShape("tile", new LittleShapeTile(), ShapeType.DEFAULT_SELECTOR);
        registerShape("type", new LittleShapeType(), ShapeType.SELECTOR);
        defaultShape = registerShape("box", new LittleShapeBox(), ShapeType.SHAPE);
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
