package team.creative.littletiles.client.render.overlay;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent.OverlayType;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiControlRect;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.integration.IGuiIntegratedParent;
import team.creative.creativecore.common.gui.integration.ScreenEventListener;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.list.TupleList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.control.GuiActionDisplay;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class OverlayRenderer implements IGuiIntegratedParent, LevelAwareHandler {
    
    private static final Minecraft MC = Minecraft.getInstance();
    
    private boolean doneInit = false;
    private final OverlayGuiLayer transparentLayer = new OverlayGuiLayer("overlay");
    
    private final SingletonList<GuiLayer> layers = new SingletonList<>(transparentLayer);
    private final Screen screen = new Screen(Component.literal("overlay")) {};
    private final ScreenEventListener listener = new ScreenEventListener(this, screen);
    private int lastWidth = -1;
    private int lastHeight = -1;
    
    public OverlayRenderer() {
        NeoForge.EVENT_BUS.addListener(this::renderPost);
        NeoForge.EVENT_BUS.addListener(this::renderBlockOverlay);
        transparentLayer.setParent(this);
    }
    
    public OverlayGuiLayer gui() {
        return this.transparentLayer;
    }
    
    public void displayActionMessage(List<Component> message) {
        transparentLayer.addMessage(message);
    }
    
    public void renderBlockOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getBlockState().getBlock() instanceof BlockTile && event.getOverlayType() == OverlayType.BLOCK)
            event.setCanceled(true);
    }
    
    public void renderPost(RenderGuiEvent.Post event) {
        Player player = MC.player;
        Font font = MC.font;
        if (player != null && !MC.options.hideGui) {
            if (!doneInit) {
                transparentLayer.style = GuiStyle.getStyle(transparentLayer.name);
                transparentLayer.init();
                doneInit = true;
            }
            
            GuiGraphics graphics = event.getGuiGraphics();
            screen.width = MC.getWindow().getGuiScaledWidth();
            screen.height = MC.getWindow().getGuiScaledHeight();
            if (screen.width != lastWidth || screen.height != lastHeight) {
                transparentLayer.reflow();
                lastWidth = screen.width;
                lastHeight = screen.height;
            }
            
            render(graphics, screen, listener, 0, 0);
            
            Component tooltip = null;
            if (LittleTilesClient.PREVIEW_RENDERER.tool() != null)
                tooltip = LittleTilesClient.PREVIEW_RENDERER.tool().tooltip();
            
            if (tooltip == null && LittleTiles.CONFIG.rendering.showTooltip && player.getMainHandItem().getItem() instanceof IItemTooltip item) {
                ItemStack stack = player.getMainHandItem();
                String tooltipKey = stack.getItem().builtInRegistryHolder().key().location().getNamespace() + "." + stack.getItem().builtInRegistryHolder().key().location()
                        .getPath() + ".tooltip";
                tooltipKey = item.tooltipTranslateKey(stack, tooltipKey);
                if (LanguageUtils.can(tooltipKey))
                    tooltip = Component.translatable(tooltipKey, item.tooltipData(stack));
            }
            
            if (tooltip != null) {
                String[] lines = tooltip.getString().split("\\n");
                
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
        transparentLayer.clearMessages();
    }
    
    @Override
    public Provider provider() {
        return MC.level.registryAccess();
    }
    
    public static class OverlayGuiLayer extends GuiLayer {
        
        private final GuiActionDisplay actionDisplay = new GuiActionDisplay("action").setMessageCount(1);
        
        public OverlayGuiLayer(String name) {
            super(name);
        }
        
        private TupleList<GuiControl, OverlayPosition> positions = new TupleList<>();
        
        @Override
        public void create() {
            addOverlayControl(actionDisplay, OverlayPosition.ACTION_BAR);
        }
        
        public void addOverlayControl(GuiControl control, OverlayPosition position) {
            super.add(control);
            positions.add(control, position);
        }
        
        @Override
        public boolean hasMinimumOuterSpacing() {
            return false;
        }
        
        @Override
        @Deprecated
        public GuiParent add(boolean conditional, Supplier<GuiControl> controlSupplier) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        @Deprecated
        public GuiParent add(GuiControl control) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        @Deprecated
        public GuiParent add(GuiControl... controls) {
            throw new UnsupportedOperationException();
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
            for (Tuple<GuiControl, OverlayPosition> tuple : positions)
                tuple.value.positionControl(tuple.key.rect, width, height);
        }
        
        @Override
        public boolean isExpandableX() {
            return true;
        }
        
        @Override
        public boolean isExpandableY() {
            return true;
        }
        
        public void clearMessages() {
            actionDisplay.clearMessages();
        }
        
        public void addMessage(List<Component> message) {
            actionDisplay.addMessage(message);
        }
    }
    
    public static enum OverlayPosition {
        
        TOP_STRETCH {
            @Override
            protected void positionControl(GuiControlRect control, int width, int height) {
                control.setX(0);
                control.setWidth(width, 0);
                control.setY(0);
            }
        },
        
        CENTER {
            @Override
            protected void positionControl(GuiControlRect control, int width, int height) {
                control.setX(width / 2 - control.getWidth() / 2);
                control.setY(height / 2 - control.getHeight() / 2);
            }
        },
        ACTION_BAR {
            
            @Override
            protected void positionControl(GuiControlRect control, int width, int height) {
                control.setX(width / 2 - control.getWidth() / 2);
                control.setY(height - control.getHeight() - 30);
            }
        };
        
        protected abstract void positionControl(GuiControlRect control, int width, int height);
    }
    
}
