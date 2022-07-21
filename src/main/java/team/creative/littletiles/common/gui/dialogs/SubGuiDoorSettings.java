package team.creative.littletiles.common.gui.dialogs;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;

import net.minecraft.nbt.NBTTagCompound;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;

public class SubGuiDoorSettings extends GuiLayer {
    
    public GuiDoorSettingsButton button;
    
    public SubGuiDoorSettings(GuiDoorSettingsButton button) {
        this.button = button;
    }
    
    @Override
    public void create() {
        controls.add(new GuiCheckBox("stayAnimated", CoreControl.translate("gui.door.stayAnimated"), 0, 0, button.stayAnimated)
                .setCustomTooltip(CoreControl.translate("gui.door.stayAnimatedTooltip")).setEnabled(button.stayAnimatedPossible));
        controls.add(new GuiCheckBox("rightclick", CoreControl.translate("gui.door.rightclick"), 0, 15, button.disableRightClick));
        controls.add(new GuiCheckBox("noClip", CoreControl.translate("gui.door.noClip"), 0, 45, button.noClip));
        controls.add(new GuiCheckBox("playPlaceSounds", CoreControl.translate("gui.door.playPlaceSounds"), 0, 65, button.playPlaceSounds));
        
        controls.add(new GuiButton("Close", 0, 143) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                closeGui();
            }
        });
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
        GuiCheckBox stayAnimated = (GuiCheckBox) get("stayAnimated");
        GuiCheckBox rightclick = (GuiCheckBox) get("rightclick");
        GuiCheckBox noClip = (GuiCheckBox) get("noClip");
        GuiCheckBox playPlaceSounds = (GuiCheckBox) get("playPlaceSounds");
        button.stayAnimated = stayAnimated.value;
        button.disableRightClick = rightclick.value;
        button.noClip = noClip.value;
        button.playPlaceSounds = playPlaceSounds.value;
    }
    
    public static class GuiDoorSettingsButton extends GuiButton {
        
        public SubGuiDoorSettings gui;
        public boolean stayAnimated;
        public boolean disableRightClick;
        public boolean noClip;
        public boolean playPlaceSounds;
        
        public boolean stayAnimatedPossible = true;
        
        public GuiDoorSettingsButton(String name, int x, int y, boolean stayAnimated, boolean disableRightClick, boolean noClip, boolean playPlaceSounds) {
            super(name, translate("gui.door.settings"), x, y, 40, 7);
            this.stayAnimated = stayAnimated;
            this.disableRightClick = disableRightClick;
            this.noClip = noClip;
            this.playPlaceSounds = playPlaceSounds;
        }
        
        @Override
        public void onClicked(int x, int y, int button) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("dialog", true);
            SubGuiDoorSettings dialog = new SubGuiDoorSettings(this);
            dialog.gui = getParent().getOrigin().gui;
            PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, dialog.gui.getLayers().size() - 1, false));
            dialog.container = new SubContainerEmpty(getPlayer());
            dialog.gui.addLayer(dialog);
            dialog.onOpened();
        }
    }
    
}
