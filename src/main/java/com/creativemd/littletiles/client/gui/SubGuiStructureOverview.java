package com.creativemd.littletiles.client.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.IStructureConnection;
import com.creativemd.littletiles.common.structure.connection.StructureChildConnection;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.parent.StructureTileList;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiStructureOverview extends SubGui {
	
	public StructureTileList list;
	
	public SubGuiStructureOverview(StructureTileList list) {
		this.list = list;
	}
	
	@Override
	public void createControls() {
		try {
			controls.add(new GuiLabel(printStructureTitle(list), 0, 3));
			LittleStructure structure = list.getStructure();
			
			if (structure.getParent() == null)
				controls.add(new GuiLabel(ChatFormatting.WHITE + "no parent", 0, 20));
			else {
				controls.add(new GuiLabel(ChatFormatting.WHITE + "parent: " + printStructureTitle(structure.getParent()), 0, 20));
				try {
					structure.getParent().checkConnection();
					controls.add(new GuiButton("open", 140, 17) {
						
						@Override
						public void onClicked(int x, int y, int button) {
							try {
								LittleStructureGuiHandler.openGui("structureoverview", new NBTTagCompound(), getPlayer(), structure.getParent().getStructure());
							} catch (CorruptedConnectionException | NotYetConnectedException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (CorruptedConnectionException | NotYetConnectedException e) {
					controls.add(new GuiButton("connect", 130, 17) {
						
						@Override
						public void onClicked(int x, int y, int button) {
							try {
								structure.getParent().checkConnection();
							} catch (CorruptedConnectionException | NotYetConnectedException e) {
								e.printStackTrace();
							}
							controls.clear();
							createControls();
							refreshControls();
						}
					});
				}
			}
			controls.add(new GuiLabel(ChatFormatting.WHITE + "" + structure.getChildren().size() + " child" + (structure.getChildren().size() != 1 ? "ren" : ""), 0, 40));
			GuiScrollBox scrollBox = new GuiScrollBox(name, 3, 55, 165, 100);
			scrollBox.scaleFactor = 0.9F;
			int i = 0;
			for (StructureChildConnection child : structure.getChildren()) {
				scrollBox.addControl(new GuiLabel("id: " + child.getChildId() + ", " + printStructureTitle(child), 0, 5 + 20 * i));
				try {
					child.checkConnection();
					scrollBox.addControl(new GuiButton("open", 128, 2 + 20 * i) {
						
						@Override
						public void onClicked(int x, int y, int button) {
							try {
								LittleStructureGuiHandler.openGui("structureoverview", new NBTTagCompound(), getPlayer(), child.getStructure());
							} catch (CorruptedConnectionException | NotYetConnectedException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (CorruptedConnectionException | NotYetConnectedException e) {
					scrollBox.addControl(new GuiButton("connect", 115, 2 + 20 * i) {
						
						@Override
						public void onClicked(int x, int y, int button) {
							try {
								child.checkConnection();
							} catch (CorruptedConnectionException | NotYetConnectedException e) {
								e.printStackTrace();
							}
							controls.clear();
							createControls();
							refreshControls();
						}
					});
				}
				
				i++;
			}
			controls.add(scrollBox);
		} catch (CorruptedConnectionException | NotYetConnectedException e) {
			controls.add(new GuiLabel(ChatFormatting.WHITE + "status: " + ChatFormatting.YELLOW + e.getLocalizedMessage(), 0, 3));
			controls.add(new GuiButton("connect", 90, 0) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					try {
						list.checkConnection();
					} catch (CorruptedConnectionException | NotYetConnectedException e) {
						e.printStackTrace();
					}
					controls.clear();
					createControls();
					refreshControls();
				}
			});
		}
	}
	
	private static String printStructureTitle(IStructureConnection connection) {
		try {
			LittleStructure structure = connection.getStructure();
			return ChatFormatting.WHITE + "" + structure.getIndex() + "," + ChatFormatting.WHITE + "status: " + ChatFormatting.DARK_GREEN + (structure.name != null ? structure.name : structure.type.id) + ChatFormatting.WHITE + ", " + structure.count() + " tile(s)";
		} catch (CorruptedConnectionException e) {
			return ChatFormatting.WHITE + "" + connection.getIndex() + "-" + connection.getAttribute() + " status:" + ChatFormatting.RED + " broken";
		} catch (NotYetConnectedException e) {
			return ChatFormatting.WHITE + "" + connection.getIndex() + "-" + connection.getAttribute() + " status:" + ChatFormatting.YELLOW + " pending";
		}
	}
	
}
