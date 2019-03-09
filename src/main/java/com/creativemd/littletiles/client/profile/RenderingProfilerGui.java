package com.creativemd.littletiles.client.profile;

import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.render.RenderingThread;

public class RenderingProfilerGui extends SubGui {
	
	@Override
	public void createControls() {
		
	}
	
	@Override
	protected void renderForeground(GuiRenderHelper helper, Style style) {
		super.renderForeground(helper, style);
		
		synchronized (RenderingThread.chunks) {
			helper.font.drawStringWithShadow("Thread count: " + RenderingThread.threads.size(), 2, 2, ColorUtils.WHITE);
			helper.font.drawStringWithShadow("Chunks: " + RenderingThread.chunks.size(), 2, 12, ColorUtils.WHITE);
			
			int i = 0;
			for (RenderingThread thread : RenderingThread.threads) {
				helper.font.drawStringWithShadow("Thread - " + thread.getThreadIndex() + ": " + thread.updateCoords.size(), 2, 22 + i * 10, ColorUtils.WHITE);
				i++;
			}
		}
	}
	
}
