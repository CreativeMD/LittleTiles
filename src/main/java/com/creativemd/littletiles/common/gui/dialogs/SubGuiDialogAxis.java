package com.creativemd.littletiles.common.gui.dialogs;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlClickEvent;
import com.creativemd.creativecore.common.gui.mc.ContainerSub;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.gui.controls.IAnimationControl;
import com.creativemd.littletiles.common.structure.type.LittleAdvancedDoor;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SubGuiDialogAxis extends SubGui {
	
	public final GuiAxisButton activator;
	
	public SubGuiDialogAxis(GuiAxisButton activator) {
		super(160, 130);
		this.activator = activator;
	}
	
	@Override
	public void createControls() {
		controls.add(activator.viewer);
		controls.add(new GuiIconButton("reset view", 20, 107, 8) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				activator.viewer.offsetX.set(0);
				activator.viewer.offsetY.set(0);
				activator.viewer.scale.set(40);
			}
		}.setCustomTooltip("reset view"));
		controls.add(new GuiIconButton("change view", 40, 107, 7) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				switch (activator.viewer.getAxis()) {
				case X:
					activator.viewer.setViewAxis(EnumFacing.Axis.Y);
					break;
				case Y:
					activator.viewer.setViewAxis(EnumFacing.Axis.Z);
					break;
				case Z:
					activator.viewer.setViewAxis(EnumFacing.Axis.X);
					break;
				default:
					break;
				}
			}
		}.setCustomTooltip("change view"));
		controls.add(new GuiIconButton("flip view", 60, 107, 4) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				activator.viewer.setViewDirection(activator.viewer.getViewDirection().getOpposite());
			}
		}.setCustomTooltip("flip view"));
		
		controls.add(new GuiIconButton("up", 124, 33, 1) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				activator.viewer.moveY(GuiScreen.isCtrlKeyDown() ? activator.viewer.context.size : 1);
			}
		});
		
		controls.add(new GuiIconButton("right", 141, 50, 0) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				activator.viewer.moveX(GuiScreen.isCtrlKeyDown() ? activator.viewer.context.size : 1);
			}
		});
		
		controls.add(new GuiIconButton("left", 107, 50, 2) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				activator.viewer.moveX(-(GuiScreen.isCtrlKeyDown() ? activator.viewer.context.size : 1));
			}
		});
		
		controls.add(new GuiIconButton("down", 124, 50, 3) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				activator.viewer.moveY(-(GuiScreen.isCtrlKeyDown() ? activator.viewer.context.size : 1));
			}
		});
		
		controls.add(new GuiCheckBox("even", 107, 0, activator.viewer.isEven()));
		
		GuiStateButton contextBox = new GuiStateButton("grid", LittleGridContext.getNames().indexOf(activator.viewer.getAxisContext() + ""), 107, 80, 20, 12, LittleGridContext.getNames().toArray(new String[0]));
		controls.add(contextBox);
		
		controls.add(new GuiButton("close", 125, 110) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				onClosed();
				gui.removeLayer(SubGuiDialogAxis.this);
			}
		});
	}
	
	@CustomEventSubscribe
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(GuiControlClickEvent event) {
		GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
		if (event.source.is("even")) {
			viewer.setEven(((GuiCheckBox) event.source).value);
		}
	}
	
	@CustomEventSubscribe
	@SideOnly(Side.CLIENT)
	public void onStateChange(GuiControlChangedEvent event) {
		if (event.source.is("grid")) {
			GuiStateButton contextBox = (GuiStateButton) event.source;
			LittleGridContext context;
			try {
				context = LittleGridContext.get(Integer.parseInt(contextBox.caption));
			} catch (NumberFormatException e) {
				context = LittleGridContext.get();
			}
			
			GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
			LittleTileBox box = viewer.getBox();
			box.convertTo(viewer.getAxisContext(), context);
			
			if (viewer.isEven())
				box.maxX = box.minX + 2;
			else
				box.maxX = box.minX + 1;
			
			if (viewer.isEven())
				box.maxY = box.minY + 2;
			else
				box.maxY = box.minY + 1;
			
			if (viewer.isEven())
				box.maxZ = box.minZ + 2;
			else
				box.maxZ = box.minZ + 1;
			
			viewer.setAxis(box, context);
		}
	}
	
	public static class GuiAxisButton extends GuiButton implements IAnimationControl {
		
		public GuiTileViewer viewer;
		public LittleAdvancedDoor door;
		public LittleGridContext stackContext;
		
		public GuiAxisButton(String name, String caption, int x, int y, int width, int height, LittleGridContext context, LittleAdvancedDoor door) {
			super(name, caption, x, y, width, height);
			setEnabled(false);
			this.stackContext = context;
			this.door = door;
		}
		
		@Override
		public void onClicked(int x, int y, int button) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("dialog", true);
			SubGuiDialogAxis dialog = new SubGuiDialogAxis(this);
			dialog.gui = getParent().getOrigin().gui;
			dialog.gui.addLayer(dialog);
			dialog.container = new SubContainerEmpty(getPlayer());
			((ContainerSub) dialog.gui.inventorySlots).layers.add(dialog.container);
			dialog.onOpened();
			dialog.gui.resize();
		}
		
		@Override
		public void onLoaded(EntityAnimation animation, LittleTileBox entireBox, LittleGridContext context, AxisAlignedBB box, LittlePreviews previews) {
			LittleGridContext axisContext = stackContext;
			
			viewer = new GuiTileViewer("tileviewer", 0, 0, 100, 100, stackContext);
			
			boolean even = false;
			if (door != null) {
				even = door.axisCenter.isEven();
				viewer.setEven(even);
				
				axisContext = door.axisCenter.getContext();
				door.axisCenter.convertToSmallest();
				viewer.setAxis(door.axisCenter.getBox(), axisContext);
				
			} else {
				viewer.setEven(false);
				viewer.setAxis(new LittleTileBox(0, 0, 0, 1, 1, 1), viewer.context);
			}
			viewer.visibleAxis = true;
			viewer.onLoaded(animation, entireBox, axisContext, box, previews);
			setEnabled(true);
		}
		
	}
}
