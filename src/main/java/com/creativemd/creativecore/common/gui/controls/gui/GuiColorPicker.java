package com.creativemd.creativecore.common.gui.controls.gui;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.mc.ColorUtils.ColorPart;

public class GuiColorPicker extends GuiParent {
	
	public Color color;
	
	public GuiColoredSteppedSlider sliderR;
	public GuiColoredSteppedSlider sliderG;
	public GuiColoredSteppedSlider sliderB;
	public GuiColoredSteppedSlider sliderS;
	private double oldShaderValue = 0;
	
	/**
	 * @param name
	 * @param x
	 * @param y
	 * @param color
	 * @param hasAlpha
	 * @param alphaMin
	 */
	public GuiColorPicker(String name, int x, int y, Color color, boolean hasAlpha, int alphaMin) {
		super(name, x, y, 140, 50);
		marginWidth = 0;
		this.color = color;
		setStyle(Style.emptyStyle);
		
		addControl(new GuiButtonHold("r-", "<", 0, 0, 1, 5) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onColorChanged();
				sliderR.setValue(sliderR.value - 1);
				updateShadeSlider();
			}
			
		});
		addControl(new GuiButtonHold("r+", ">", 98, 0, 1, 5) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onColorChanged();
				sliderR.setValue(sliderR.value + 1);
				updateShadeSlider();
			}
			
		});
		
		addControl(new GuiButtonHold("g-", "<", 0, 10, 1, 5) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onColorChanged();
				sliderG.setValue(sliderG.value - 1);
				updateShadeSlider();
			}
			
		});
		addControl(new GuiButtonHold("g+", ">", 98, 10, 1, 5) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onColorChanged();
				sliderG.setValue(sliderG.value + 1);
				updateShadeSlider();
			}
			
		});
		
		addControl(new GuiButtonHold("b-", "<", 0, 20, 1, 5) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onColorChanged();
				sliderB.setValue(sliderB.value - 1);
				updateShadeSlider();
			}
			
		});
		addControl(new GuiButtonHold("b+", ">", 98, 20, 1, 5) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onColorChanged();
				sliderB.setValue(sliderB.value + 1);
				updateShadeSlider();
			}
			
		});
		
		addControl(new GuiButtonHold("s-", "<", 0, 40, 1, 5) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onColorChanged();
				if(!isMin(sliderR.value, sliderG.value, sliderB.value)) {
					sliderR.setValue(sliderR.value - 1);
					sliderG.setValue(sliderG.value - 1);
					sliderB.setValue(sliderB.value - 1);
					sliderS.setValue(sliderS.value - 1);
					updateShadeSlider();			
				}
			}
			
		});
		addControl(new GuiButtonHold("s+", ">", 98, 40, 1, 5) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onColorChanged();
				if(!isMax(sliderR.value, sliderG.value, sliderB.value)) {
					sliderR.setValue(sliderR.value + 1);
					sliderG.setValue(sliderG.value + 1);
					sliderB.setValue(sliderB.value + 1);
					sliderS.setValue(sliderS.value + 1);
					updateShadeSlider();
				}
			}
			
		});
		
		if (hasAlpha) {
			
			addControl(new GuiButtonHold("a-", "<", 0, 30, 1, 5) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					onColorChanged();
					GuiColoredSteppedSlider slider = (GuiColoredSteppedSlider) get("a");
					slider.setValue(slider.value - 1);
				}
				
			});
			addControl(new GuiButtonHold("a+", ">", 98, 30, 1, 5) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					onColorChanged();
					GuiColoredSteppedSlider slider = (GuiColoredSteppedSlider) get("a");
					slider.setValue(slider.value + 1);
				}
				
			});
		} else
			color.setAlpha(255);
		
		//Red Slider
		sliderR = (new GuiColoredSteppedSlider("r", 8, 0, 84, 5, this, ColorPart.RED) {
			@Override
			public void mouseMove(int posX, int posY, int button) {
				super.mouseMove(posX, posY, button);
				if(grabbedSlider) {
					updateShadeSlider();
				}
			}
			
			@Override
			public boolean mouseScrolled(int x, int y, int scrolled) {
				super.mouseScrolled(x, y, scrolled);
				updateShadeSlider();
				return true;
			}
		});
		addControl(sliderR.setStyle(defaultStyle));
		
		//Green Slider
		sliderG = (new GuiColoredSteppedSlider("g", 8, 10, 84, 5, this, ColorPart.GREEN) {
			@Override
			public void mouseMove(int posX, int posY, int button) {
				super.mouseMove(posX, posY, button);
				if(grabbedSlider) {
					updateShadeSlider();
				}
			}
			
			@Override
			public boolean mouseScrolled(int x, int y, int scrolled) {
				super.mouseScrolled(x, y, scrolled);
				updateShadeSlider();
				return true;
			}
		});
		addControl(sliderG.setStyle(defaultStyle));
		
		//Blue Slider
		sliderB = (new GuiColoredSteppedSlider("b", 8, 20, 84, 5, this, ColorPart.BLUE) {
			@Override
			public void mouseMove(int posX, int posY, int button) {
				super.mouseMove(posX, posY, button);
				if(grabbedSlider) {
					updateShadeSlider();
				}
			}
			
			@Override
			public boolean mouseScrolled(int x, int y, int scrolled) {
				super.mouseScrolled(x, y, scrolled);
				updateShadeSlider();
				return true;
			}
		});
		addControl(sliderB.setStyle(defaultStyle));
		
		//Alpha Slider
		if (hasAlpha) {
			GuiColoredSteppedSlider alpha = new GuiColoredSteppedSlider("a", 8, 30, 84, 5, this, ColorPart.ALPHA);
			alpha.minValue = alphaMin;
			addControl(alpha.setStyle(defaultStyle));
		}
		
		//Shader Slider
		sliderS = (new GuiColoredSteppedSlider("s", 8, 40, 84, 5, this, ColorPart.SHADE) {
			@Override
			public void mouseMove(int posX, int posY, int button) {
				oldShaderValue = this.value;
				super.mouseMove(posX, posY, button);
				
				if(grabbedSlider) {
					double difference = this.value - oldShaderValue;
					sliderR.setValue(sliderR.value + difference);
					sliderG.setValue(sliderG.value + difference);
					sliderB.setValue(sliderB.value + difference);
					oldShaderValue = value;
				}
			}
			@Override
			public boolean mouseScrolled(int x, int y, int scrolled) {
				oldShaderValue = this.value;
				super.mouseScrolled(x, y, scrolled);
				double difference = this.value - oldShaderValue;
				
				sliderR.setValue(sliderR.value + difference);
				sliderG.setValue(sliderG.value + difference);
				sliderB.setValue(sliderB.value + difference);
				oldShaderValue = value;
				
				return true;
			}
		});
		updateShadeSlider();
		addControl(sliderS.setStyle(defaultStyle));
		
		addControl(new GuiColorPlate("plate", 107, 2, 20, 20, color).setStyle(defaultStyle));
	}
	
	public double getShadeLimit(double r, double g, double b) {
		List<Double> rgb = new ArrayList<Double>();
		rgb.add(r);
		rgb.add(g);
		rgb.add(b);
		
		double min = Collections.min(rgb);
		double max = Collections.max(rgb);
		double a = 255-max;

		return min+a;
	}
	
	public double getMinColor(double r, double g, double b) {
		List<Double> rgb = new ArrayList<Double>();
		rgb.add(r);
		rgb.add(g);
		rgb.add(b);
		return Collections.min(rgb);
	}
	
	public double getMaxColor(double r, double g, double b) {
		List<Double> rgb = new ArrayList<Double>();
		rgb.add(r);
		rgb.add(g);
		rgb.add(b);
		return Collections.max(rgb);
	}
	
	public boolean isMin(double r, double g, double b) {
		List<Double> rgb = new ArrayList<Double>();
		rgb.add(r);
		rgb.add(g);
		rgb.add(b);
		double min = Collections.min(rgb);
		if(min == 0) {
			return true;
		}
		return false;
	}
	
	public boolean isMax(double r, double g, double b) {
		List<Double> rgb = new ArrayList<Double>();
		rgb.add(r);
		rgb.add(g);
		rgb.add(b);
		double max = Collections.max(rgb);
		if(max == 255) {
			return true;
		}
		return false;
	}
	
	public void updateShadeSlider() {
		sliderS.maxValue = getShadeLimit(sliderR.value, sliderG.value, sliderB.value);
		sliderS.value = getMinColor(sliderR.value, sliderG.value, sliderB.value);
		oldShaderValue = sliderS.value;
	}
	
	public void onColorChanged() {
		raiseEvent(new GuiControlChangedEvent(this));
	}
	
}
