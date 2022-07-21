package team.creative.littletiles.client.action;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.item.tooltip.ActionMessage;
import team.creative.littletiles.common.item.tooltip.ActionMessage.ActionMessageObjectType;

public class CompiledActionMessage {
    
    public int height;
    public int width;
    public final List<ActionLine> lines;
    public final long timestamp;
    
    public CompiledActionMessage(ActionMessage message) {
        this(message.text, message.objects);
    }
    
    public CompiledActionMessage(String text, Object... objects) {
        lines = new ArrayList<>();
        
        List<Object> lineObjects = new ArrayList<>();
        
        int tempWidth = 0;
        int first = 0;
        
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '\n')
                if (first == i)
                    first++;
                else {
                    lineObjects.add(text.substring(first, i));
                    ActionLine line = new ActionLine(new ArrayList<>(lineObjects));
                    tempWidth = Math.max(tempWidth, line.width);
                    lines.add(line);
                    lineObjects.clear();
                    first = i + 1;
                }
            else if (text.charAt(i) == '{') {
                for (int j = i + 1; j < text.length(); j++) {
                    if (Character.isDigit(text.charAt(j)))
                        continue;
                    else if (text.charAt(j) == '}') {
                        if (first != i)
                            lineObjects.add(text.substring(first, i));
                        lineObjects.add(objects[Integer.parseInt(text.substring(i + 1, j))]);
                        first = j + 1;
                        i = j;
                        break;
                    } else
                        break;
                }
            }
            i++;
        }
        
        if (first != i)
            lineObjects.add(text.substring(first, i));
        
        if (!lineObjects.isEmpty()) {
            ActionLine line = new ActionLine(new ArrayList<>(lineObjects));
            tempWidth = Math.max(tempWidth, line.width);
            lines.add(line);
        }
        
        this.width = tempWidth;
        this.height = (GuiRenderHelper.getFont().lineHeight + 3) * lines.size();
        this.timestamp = System.currentTimeMillis();
    }
    
    public void render(PoseStack pose, float alpha) {
        Font font = GuiRenderHelper.getFont();
        pose.pushPose();
        int color = ColorUtils.rgba(255, 255, 255, (int) (alpha * 255));
        for (int i = 0; i < lines.size(); i++) {
            ActionLine line = lines.get(i);
            pose.translate(-line.width / 2, 0, 0);
            for (int j = 0; j < line.objects.size(); j++) {
                Object obj = line.objects.get(j);
                if (obj.getClass() == String.class) {
                    font.draw(pose, (String) obj, 0, 0, color);
                    pose.translate(font.width((String) obj), 0, 0);
                } else {
                    ActionMessageObjectType type = ActionMessage.getType(obj);
                    type.render(pose, obj, color, alpha);
                    pose.translate(type.width(obj), 0, 0);
                }
            }
            pose.translate(0, font.lineHeight + 3, 0);
        }
        pose.popPose();
    }
    
    public class ActionLine {
        
        public final List<Object> objects;
        public final int width;
        
        public ActionLine(List<Object> objects) {
            this.objects = objects;
            int lineWidth = 0;
            for (int i = 0; i < objects.size(); i++) {
                Object obj = objects.get(i);
                if (obj.getClass() == String.class)
                    lineWidth += GuiRenderHelper.getFont().width((String) obj);
                else
                    lineWidth += ActionMessage.getType(obj).width(obj);
            }
            this.width = lineWidth;
        }
        
    }
}