package com.creativemd.littletiles.common.util.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeBox;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeConnected;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeCurve;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeCurveWall;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeCylinder;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeInnerCorner;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeOuterCorner;
import com.creativemd.littletiles.common.util.shape.type.LittleShapePillar;
import com.creativemd.littletiles.common.util.shape.type.LittleShapePolygon;
import com.creativemd.littletiles.common.util.shape.type.LittleShapePyramid;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeSlice;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeSphere;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeTile;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeType;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeWall;

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
        placingList.addAll(shapeTypeLists.getValuesOrEmpty(ShapeType.SHAPE));
        placingList.addAll(shapeTypeLists.getValuesOrEmpty(ShapeType.DEFAULT_SELECTOR));
        placingList.addAll(shapeTypeLists.getValuesOrEmpty(ShapeType.SELECTOR));
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
