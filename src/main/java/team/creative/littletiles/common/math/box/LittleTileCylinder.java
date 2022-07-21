package team.creative.littletiles.common.math.box;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;

public class LittleTileCylinder extends LittleBox {
    
    /** relative to min vec It is two times larger than ordinary grid to support in
     * between positions. In order to get the right position it has to be divided by
     * 2 */
    public int centerOne;
    /** relative to min vec It is two times larger than ordinary grid to support in
     * between positions. In order to get the right position it has to be divided by
     * 2 */
    public int centerTwo;
    public Axis axis;
    /** It is two times larger than ordinary grid to support in between positions. In
     * order to get the right position it has to be divided by 2 */
    public int radius;
    
    public LittleTileCylinder(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Axis axis, int centerOne, int centerTwo, int radius) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.axis = axis;
        this.centerOne = centerOne;
        this.centerTwo = centerTwo;
        this.radius = radius;
    }
    
    public LittleTileCylinder(LittleBox box, Axis axis, int centerOne, int centerTwo, int radius) {
        this(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, axis, centerOne, centerTwo, radius);
    }
    
    // ================Cylinder================
    
    public int getCenter(Axis axis) {
        if (axis == this.axis.one())
            return centerOne;
        return centerTwo;
    }
    
    public boolean isSolid(Facing facing) {
        if (facing.axis == axis)
            return false;
        return false;
    }
}
