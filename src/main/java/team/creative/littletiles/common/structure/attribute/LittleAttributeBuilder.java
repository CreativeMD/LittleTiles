package team.creative.littletiles.common.structure.attribute;

public class LittleAttributeBuilder {
    
    public static final LittleAttributeBuilder NONE = new LittleAttributeBuilder() {
        @Override
        public int build() {
            return 0;
        }
    };
    
    private int attribute = 0;
    
    public int build() {
        return attribute;
    }
    
    public LittleAttributeBuilder ladder() {
        attribute |= LittleStructureAttribute.LADDER;
        return this;
    }
    
    public LittleAttributeBuilder noCollision() {
        attribute |= LittleStructureAttribute.NOCOLLISION;
        return this;
    }
    
    public LittleAttributeBuilder premade() {
        attribute |= LittleStructureAttribute.PREMADE;
        return this;
    }
    
    public LittleAttributeBuilder emissive() {
        attribute |= LittleStructureAttribute.EMISSIVE;
        return this;
    }
    
    public LittleAttributeBuilder extraCollision() {
        attribute |= LittleStructureAttribute.EXTRA_COLLSION;
        return this;
    }
    
    public LittleAttributeBuilder extraRendering() {
        attribute |= LittleStructureAttribute.EXTRA_RENDERING;
        return this;
    }
    
    public LittleAttributeBuilder ticking() {
        attribute |= LittleStructureAttribute.TICKING;
        return this;
    }
    
    public LittleAttributeBuilder tickRendering() {
        attribute |= LittleStructureAttribute.TICK_RENDERING;
        return this;
    }
    
    public LittleAttributeBuilder neighborListener() {
        attribute |= LittleStructureAttribute.NEIGHBOR_LISTENER;
        return this;
    }
    
    public LittleAttributeBuilder collisionListener() {
        attribute |= LittleStructureAttribute.COLLISION_LISTENER;
        return this;
    }
    
    public LittleAttributeBuilder lightEmitter() {
        attribute |= LittleStructureAttribute.LIGHT_EMITTER;
        return this;
    }
}