package team.creative.littletiles.client.render.overlay;

import java.util.List;

import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.integration.IGuiIntegratedParent;
import team.creative.creativecore.common.gui.integration.ScreenEventListener;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.list.TupleList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.common.gui.controls.GuiActionDisplay;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class OverlayRenderer implements IGuiIntegratedParent, LevelAwareHandler {
    
    private static final Minecraft MC = Minecraft.getInstance();
    private final GuiActionDisplay actionDisplay = new GuiActionDisplay("action").setMessageCount(1);
    private final GuiLayer transparentLayer = new GuiLayer("overlay") {
        
        private TupleList<GuiChildControl, OverlayPosition> positions = new TupleList<>();
        
        @Override
        public void create() {
            addOverlayControl(actionDisplay, OverlayPosition.ACTION_BAR);
        }
        
        public void addOverlayControl(GuiControl control, OverlayPosition position) {
            positions.add(super.add(control), position);
        }
        
        @Override
        public ControlFormatting getControlFormatting() {
            return ControlFormatting.TRANSPARENT;
        }
        
        @Override
        public boolean hasGrayBackground() {
            return false;
        }
        
        @Override
        public void flowY(int width, int height, int preferred) {
            super.flowY(width, height, preferred);
            for (Tuple<GuiChildControl, OverlayPosition> tuple : positions)
                tuple.value.positionControl(tuple.key, width, height);
        }
        
        @Override
        public boolean isExpandableX() {
            return true;
        }
        
        @Override
        public boolean isExpandableY() {
            return true;
        }
    };
    
    private final SingletonList<GuiLayer> layers = new SingletonList<>(transparentLayer);
    private final Screen screen = new Screen(Component.literal("overlay")) {};
    private final ScreenEventListener listener = new ScreenEventListener(this, screen);
    private int lastWidth = -1;
    private int lastHeight = -1;
    
    public OverlayRenderer() {
        MinecraftForge.EVENT_BUS.addListener(this::renderPost);
        transparentLayer.init();
    }
    
    public void displayActionMessage(List<Component> message) {
        actionDisplay.addMessage(message);
    }
    
    public void renderPost(RenderGuiEvent.Post event) {
        Player player = MC.player;
        Font font = MC.font;
        if (player != null && !MC.options.hideGui) {
            GuiGraphics graphics = event.getGuiGraphics();
            screen.width = MC.getWindow().getGuiScaledWidth();
            screen.height = MC.getWindow().getGuiScaledHeight();
            if (screen.width != lastWidth || screen.height != lastHeight) {
                transparentLayer.reflow();
                lastWidth = screen.width;
                lastHeight = screen.height;
            }
            
            render(graphics, screen, listener, 0, 0);
            
            if (LittleTiles.CONFIG.rendering.showTooltip && player.getMainHandItem().getItem() instanceof IItemTooltip item) {
                ItemStack stack = player.getMainHandItem();
                String tooltipKey = stack.getItem().builtInRegistryHolder().key().location().getNamespace() + "." + stack.getItem().builtInRegistryHolder().key().location()
                        .getPath() + ".tooltip";
                tooltipKey = item.tooltipTranslateKey(stack, tooltipKey);
                if (LanguageUtils.can(tooltipKey)) {
                    String[] lines = Component.translatable(tooltipKey, item.tooltipData(stack)).getString().split("\\n");
                    
                    int y = MC.getWindow().getGuiScaledHeight() - 2;
                    for (int i = lines.length - 1; i >= 0; i--) {
                        String s = lines[i];
                        
                        if (!Strings.isNullOrEmpty(s)) {
                            y -= font.lineHeight;
                            int k = font.width(s);
                            int i1 = 2 + y;
                            graphics.fill(1, i1 - 1, 2 + k + 1, i1 + font.lineHeight - 1, -1873784752);
                            graphics.drawString(font, s, 2, i1, 14737632);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public boolean isContainer() {
        return false;
    }
    
    @Override
    public boolean isClient() {
        return true;
    }
    
    @Override
    public Player getPlayer() {
        return MC.player;
    }
    
    @Override
    public void closeTopLayer() {}
    
    @Override
    public void closeLayer(GuiLayer layer) {}
    
    @Override
    public List<GuiLayer> getLayers() {
        return layers;
    }
    
    @Override
    public GuiLayer getTopLayer() {
        return transparentLayer;
    }
    
    @Override
    public void openLayer(GuiLayer layer) {}
    
    @Override
    public void closeLayer(int layer) {}
    
    @Override
    public void send(CreativePacket message) {}
    
    @Override
    public void unload() {
        actionDisplay.clearMessages();
    }
    
    public static enum OverlayPosition {
        
        CENTER {
            @Override
            protected void positionControl(GuiChildControl control, int width, int height) {
                control.setX(width / 2 - control.getWidth() / 2);
                control.setY(height / 2 - control.getHeight() / 2);
            }
        },
        ACTION_BAR {
            
            @Override
            protected void positionControl(GuiChildControl control, int width, int height) {
                control.setX(width / 2 - control.getWidth() / 2);
                control.setY(height - control.getHeight() - 30);
            }
        };
        
        protected abstract void positionControl(GuiChildControl control, int width, int height);
    }
    
}
