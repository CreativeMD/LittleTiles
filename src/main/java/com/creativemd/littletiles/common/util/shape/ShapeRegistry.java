package com.creativemd.littletiles.common.util.shape;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.creativemd.littletiles.common.util.shape.type.LittleShapeBox;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeConnected;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeCurve;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeCurveWall;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeCylinder;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeInnerCorner;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeOuterCorner;
import com.creativemd.littletiles.common.util.shape.type.LittleShapePillar;
import com.creativemd.littletiles.common.util.shape.type.LittleShapePyramid;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeSlice;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeSphere;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeTile;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeType;
import com.creativemd.littletiles.common.util.shape.type.LittleShapeWall;

public class ShapeRegistry {
    
    private static LinkedHashMap<String, LittleShape> shapes = new LinkedHashMap<>();
    private static LittleShape defaultShape;
    public static LittleShape tileShape;
    
    public static Collection<LittleShape> shapes() {
        return shapes.values();
    }
    
    public static Set<String> shapeNames() {
        return shapes.keySet();
    }
    
    public static LittleShape registerShape(String id, LittleShape shape) {
        shapes.put(id, shape);
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
        tileShape = registerShape("tile", new LittleShapeTile());
        registerShape("type", new LittleShapeType());
        defaultShape = registerShape("box", new LittleShapeBox());
        registerShape("connected", new LittleShapeConnected());
        
        registerShape("slice", new LittleShapeSlice());
        registerShape("inner_corner", new LittleShapeInnerCorner());
        registerShape("outer_corner", new LittleShapeOuterCorner());
        
        registerShape("wall", new LittleShapeWall());
        registerShape("pillar", new LittleShapePillar());
        registerShape("curve", new LittleShapeCurve());
        registerShape("curvewall", new LittleShapeCurveWall());
        
        registerShape("cylinder", new LittleShapeCylinder());
        registerShape("sphere", new LittleShapeSphere());
        registerShape("pyramid", new LittleShapePyramid());
        
    }
    
}
