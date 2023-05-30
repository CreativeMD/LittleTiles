package team.creative.littletiles.client.render.mc;

import java.util.List;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage;

public class VertexFormatUtils {
    
    private static int vertexFormatSize;
    private static int positionOffset;
    
    static {
        update();
    }
    
    public static void update() {
        VertexFormat format = DefaultVertexFormat.BLOCK;
        vertexFormatSize = format.getVertexSize();
        List<VertexFormatElement> elements = format.getElements();
        for (int i = 0; i < elements.size(); i++)
            if (elements.get(i).getUsage() == Usage.POSITION) {
                positionOffset = format.getOffset(i);
                break;
            }
    }
    
    public static int blockFormatSize() {
        return vertexFormatSize;
    }
    
    public static int blockPositionOffset() {
        return positionOffset;
    }
    
}
