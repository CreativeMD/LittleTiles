package team.creative.littletiles.common.math.vec;

public class LittleRay {
    
    public LittleVec origin = new LittleVec(0, 0, 0);
    public LittleVec direction = new LittleVec(0, 0, 0);
    
    public LittleRay(LittleVec start, LittleVec end) {
        set(start, end);
    }
    
    public boolean noDirection() {
        return direction.x == 0 && direction.y == 0 && direction.z == 0;
    }
    
    public void set(LittleVec start, LittleVec end) {
        origin.set(start.x, start.y, start.z);
        direction.x = end.x - start.x;
        direction.y = end.y - start.y;
        direction.z = end.z - start.z;
    }
    
    public boolean isCoordinateOnLine(LittleVec vec) {
        double factor;
        if (direction.x != 0)
            factor = (vec.x - origin.x) / (double) direction.x;
        else if (direction.y != 0)
            factor = (vec.y - origin.y) / (double) direction.y;
        else
            factor = (vec.z - origin.z) / (double) direction.z;
        if (Double.isInfinite(factor))
            return false;
        return origin.x + direction.x * factor == vec.x && origin.y + direction.y * factor == vec.y && origin.z + direction.z * factor == vec.z;
    }
    
    public boolean same(LittleRay other) {
        return parallel(other) && isCoordinateOnLine(other.origin);
    }
    
    public boolean parallel(LittleRay other) {
        int scale;
        int scaleOther;
        if ((direction.x > 0) != (other.direction.x > 0) || (direction.y > 0) != (other.direction.y > 0) || (direction.z > 0) != (other.direction.z > 0))
            return false;
        if (direction.x != 0) {
            scale = other.direction.x;
            scaleOther = direction.x;
        } else if (direction.y != 0) {
            scale = other.direction.y;
            scaleOther = direction.y;
        } else {
            scale = other.direction.z;
            scaleOther = direction.z;
        }
        
        return direction.x * scale == other.direction.x * scaleOther && direction.y * scale == other.direction.y * scaleOther && direction.z * scale == other.direction.z * scaleOther;
    }
    
    @Override
    public String toString() {
        return "o:" + origin + ",d:" + direction;
    }
}
