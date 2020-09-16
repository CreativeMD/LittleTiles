package com.creativemd.littletiles.client.tooltip;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.gui.controls.GuiActionDisplay;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage.ActionMessageObjectType;

import net.minecraft.client.renderer.GlStateManager;

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
		this.height = (GuiControl.font.FONT_HEIGHT + 3) * lines.size();
		this.timestamp = System.currentTimeMillis();
	}
	
	public void render(GuiRenderHelper helper, float alpha) {
		GlStateManager.pushMatrix();
		int color = ColorUtils.RGBAToInt(255, 255, 255, (int) (alpha * 255));
		for (int i = 0; i < lines.size(); i++) {
			ActionLine line = lines.get(i);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-line.width / 2, 0, 0);
			for (int j = 0; j < line.objects.size(); j++) {
				Object obj = line.objects.get(j);
				if (obj.getClass() == String.class) {
					helper.font.drawString((String) obj, 0, 0, color);
					GlStateManager.translate(helper.font.getStringWidth((String) obj), 0, 0);
				} else {
					ActionMessageObjectType type = ActionMessage.getType(obj);
					type.render(obj, helper, color, alpha);
					GlStateManager.translate(type.width(obj, helper), 0, 0);
				}
			}
			GlStateManager.popMatrix();
			GlStateManager.translate(0, GuiControl.font.FONT_HEIGHT + 3, 0);
		}
		GlStateManager.popMatrix();
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
					lineWidth += GuiRenderHelper.instance.font.getStringWidth((String) obj);
				else
					lineWidth += ActionMessage.getType(obj).width(obj, GuiRenderHelper.instance);
			}
			this.width = lineWidth;
		}
		
	}
}