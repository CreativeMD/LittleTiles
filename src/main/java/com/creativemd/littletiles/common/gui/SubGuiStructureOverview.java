package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.littletiles.common.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.IStructureChildConnector;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiStructureOverview extends SubGui {
	
	public LittleTile tile;
	
	public SubGuiStructureOverview(LittleTile tile) {
		this.tile = tile;
	}
	
	@Override
	public void createControls() {
		if (!tile.isChildOfStructure())
			return;
		LittleStructure structure = tile.connection.getStructureWithoutLoading();
		
		if (structure == null) {
			controls.add(new GuiLabel(ChatFormatting.WHITE + "status: " + ChatFormatting.YELLOW + "pending", 0, 3));
			controls.add(new GuiButton("connect", 90, 0) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					tile.isConnectedToStructure();
					controls.clear();
					createControls();
					refreshControls();
				}
			});
		} else {
			controls.add(new GuiLabel(ChatFormatting.WHITE + "status: " + ChatFormatting.DARK_GREEN + (structure.name != null ? structure.name : structure.type.id) + ChatFormatting.WHITE + ", " + structure.countTiles() + " tile(s)", 0, 3));
			
			if (structure.parent == null)
				controls.add(new GuiLabel(ChatFormatting.WHITE + "no parent", 0, 20));
			else {
				controls.add(new GuiLabel(ChatFormatting.WHITE + "parent: " + (structure.parent.getStructureWithoutLoading() == null ? ChatFormatting.YELLOW + "pending" : ChatFormatting.DARK_GREEN + (structure.parent.getStructureWithoutLoading().name != null ? structure.parent.getStructureWithoutLoading().name : structure.parent.getStructureWithoutLoading().type.id) + ChatFormatting.WHITE + ", " + structure.parent.getStructureWithoutLoading().countTiles() + " tile(s)"), 0, 20));
				
				if (structure.parent.getStructureWithoutLoading() == null)
					controls.add(new GuiButton("connect", 130, 17) {
						
						@Override
						public void onClicked(int x, int y, int button) {
							structure.parent.isConnected(tile.te.getWorld());
							controls.clear();
							createControls();
							refreshControls();
						}
					});
				else
					controls.add(new GuiButton("open", 140, 17) {
						
						@Override
						public void onClicked(int x, int y, int button) {
							LittleGuiHandler.openGui("structureoverview", new NBTTagCompound(), getPlayer(), structure.parent.getStructureWithoutLoading().getMainTile());
						}
					});
			}
			controls.add(new GuiLabel(ChatFormatting.WHITE + "" + structure.children.size() + " child" + (structure.children.size() != 1 ? "ren" : ""), 0, 40));
			GuiScrollBox scrollBox = new GuiScrollBox(name, 3, 55, 165, 100);
			scrollBox.scaleFactor = 0.9F;
			int i = 0;
			for (IStructureChildConnector child : structure.children) {
				scrollBox.addControl(new GuiLabel("id: " + child.getChildID() + ", " + (child.getStructureWithoutLoading() == null ? ChatFormatting.YELLOW + "pending" : ChatFormatting.DARK_GREEN + (child.getStructureWithoutLoading().name != null ? child.getStructureWithoutLoading().name : child.getStructureWithoutLoading().type.id) + ChatFormatting.WHITE + ", " + child.getStructureWithoutLoading().countTiles() + " tile(s)"), 0, 5 + 20 * i));
				if (child.getStructureWithoutLoading() == null)
					scrollBox.addControl(new GuiButton("connect", 115, 2 + 20 * i) {
						
						@Override
						public void onClicked(int x, int y, int button) {
							child.isConnected(tile.te.getWorld());
							controls.clear();
							createControls();
							refreshControls();
						}
					});
				else
					scrollBox.addControl(new GuiButton("open", 128, 2 + 20 * i) {
						
						@Override
						public void onClicked(int x, int y, int button) {
							//closeGui();
							LittleGuiHandler.openGui("structureoverview", new NBTTagCompound(), getPlayer(), child.getStructureWithoutLoading().getMainTile());
						}
					});
				
				i++;
			}
			controls.add(scrollBox);
		}
	}
	
}
