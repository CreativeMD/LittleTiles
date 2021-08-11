package com.creativemd.littletiles.client.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.structure.connection.IStructureConnection;
import team.creative.littletiles.common.structure.connection.StructureChildConnection;

public class SubGuiStructureOverview extends SubGui {
    
    public IStructureTileList list;
    
    public SubGuiStructureOverview(IStructureTileList list) {
        super(300, 300);
        this.list = list;
    }
    
    @Override
    public void createControls() {
        try {
            controls.add(new GuiLabel(printStructureTitle((IStructureConnection) list), 0, 3));
            LittleStructure structure = list.getStructure();
            
            if (structure.getParent() == null)
                controls.add(new GuiLabel(ChatFormatting.WHITE + "no parent", 0, 20));
            else {
                controls.add(new GuiLabel(ChatFormatting.WHITE + "parent: " + printStructureTitle(structure.getParent()), 0, 20));
                try {
                    structure.getParent().checkConnection();
                    controls.add(new GuiButton("open", 260, 17) {
                        
                        @Override
                        public void onClicked(int x, int y, int button) {
                            try {
                                LittleStructureGuiHandler.openGui("structureoverview2", new NBTTagCompound(), getPlayer(), structure.getParent().getStructure());
                            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                    controls.add(new GuiButton("connect", 260, 17) {
                        
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
            controls.add(new GuiLabel(ChatFormatting.WHITE + "" + structure.countChildren() + " child" + (structure.countChildren() != 1 ? "ren" : ""), 0, 40));
            GuiScrollBox scrollBox = new GuiScrollBox(name, 3, 55, 290, 230);
            scrollBox.scaleFactor = 1F;
            int i = 0;
            for (StructureChildConnection child : structure.getChildren()) {
                scrollBox.addControl(new GuiLabel("id:" + child.getChildId() + "," + printStructureTitle(child), 0, 5 + 20 * i));
                try {
                    child.checkConnection();
                    scrollBox.addControl(new GuiButton("open", 248, 2 + 20 * i) {
                        
                        @Override
                        public void onClicked(int x, int y, int button) {
                            try {
                                LittleStructureGuiHandler.openGui("structureoverview2", new NBTTagCompound(), getPlayer(), child.getStructure());
                            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                    scrollBox.addControl(new GuiButton("connect", 235, 2 + 20 * i) {
                        
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
            controls.add(new GuiLabel(ChatFormatting.WHITE + "status: " + ChatFormatting.YELLOW + e.getLocalizedMessage(), 0, 23));
            controls.add(new GuiButton("connect", 200, 0) {
                
                @Override
                public void onClicked(int x, int y, int button) {
                    try {
                        ((IStructureConnection) list).checkConnection();
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
            return ChatFormatting.WHITE + "i:" + structure
                .getIndex() + "," + ChatFormatting.WHITE + ChatFormatting.DARK_GREEN + (structure.name != null ? structure.name : structure.type.id) + ChatFormatting.WHITE + ", " + structure
                    .count() + " tile(s)";
        } catch (CorruptedConnectionException e) {
            return ChatFormatting.WHITE + "i:" + connection.getIndex() + "-" + connection.getAttribute() + "," + ChatFormatting.RED + " broken";
        } catch (NotYetConnectedException e) {
            return ChatFormatting.WHITE + "i:" + connection.getIndex() + "-" + connection.getAttribute() + "," + ChatFormatting.YELLOW + " pending";
        }
    }
    
}
