package com.creativemd.littletiles.common.structure.attribute;

public class LittleStructureAttribute {
    
    public static final int NONE = 0;
    
    // passive types (no notifcations)
    
    public static final int LADDER = 0b00000000_00000000_00000000_00000001;
    public static final int NOCOLLISION = 0b00000000_00000000_00000000_00000010;
    public static final int PREMADE = 0b00000000_00000000_00000000_00000100;
    
    // active types (only notify when main block)
    
    public static final int EXTRA_COLLSION = 0b00000000_00000000_00000001_00000000;
    public static final int EXTRA_RENDERING = 0b00000000_00000000_00000010_00000000;
    public static final int TICKING = 0b00000000_00000000_00000100_00000000;
    public static final int TICK_RENDERING = 0b00000000_00000000_00001000_00000000;
    
    // listener types (notify every time)
    
    public static final int NEIGHBOR_LISTENER = 0b00000000_00000001_00000000_00000000;
    public static final int COLLISION_LISTENER = 0b00000000_00000010_00000000_00000000;
    public static final int LIGHT_EMITTER = 0b00000000_00000100_00000000_00000000;
    
    public static final int ACTIVE_MASK = 0b00000000_00000000_11111111_00000000;
    public static final int LISTENER_MASK = 0b00000000_11111111_00000000_00000000;
    public static final int NON_ACTIVE_MASK = 0b00000000_11111111_00000000_11111111;
    
    public static boolean ladder(int attribute) {
        return (attribute & LADDER) != 0;
    }
    
    public static boolean noCollision(int attribute) {
        return (attribute & NOCOLLISION) != 0;
    }
    
    public static boolean premade(int attribute) {
        return (attribute & PREMADE) != 0;
    }
    
    public static boolean extraCollision(int attribute) {
        return (attribute & EXTRA_COLLSION) != 0;
    }
    
    public static boolean extraRendering(int attribute) {
        return (attribute & EXTRA_RENDERING) != 0;
    }
    
    public static boolean ticking(int attribute) {
        return (attribute & TICKING) != 0;
    }
    
    public static boolean tickRendering(int attribute) {
        return (attribute & TICK_RENDERING) != 0;
    }
    
    public static boolean neighborListener(int attribute) {
        return (attribute & NEIGHBOR_LISTENER) != 0;
    }
    
    public static boolean collisionListener(int attribute) {
        return (attribute & COLLISION_LISTENER) != 0;
    }
    
    public static boolean lightEmitter(int attribute) {
        return (attribute & LIGHT_EMITTER) != 0;
    }
    
    public static boolean active(int attribute) {
        return (attribute & ACTIVE_MASK) != 0;
    }
    
    public static boolean listener(int attribute) {
        return (attribute & LISTENER_MASK) != 0;
    }
    
    public static int loadOld(int ordinal) {
        switch (ordinal) {
        case 1:
            return NOCOLLISION;
        case 2:
            return PREMADE;
        default:
            return NONE;
        }
    }
    
}
