package team.creative.littletiles.common.gui.signal;

import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

public abstract class GuiSignalNodeAnchor<T extends GuiSignalNode> {
    
    public static final GuiSignalNodeAnchor LEFT = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return node.button.rect.getX();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return node.button.rect.centerY();
        }
        
    };
    public static final GuiSignalNodeAnchor TOP = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return node.button.rect.centerX();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return node.button.rect.getY();
        }
        
    };
    public static final GuiSignalNodeAnchor RIGHT = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return node.button.rect.getRight();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return node.button.rect.centerY();
        }
        
    };
    public static final GuiSignalNodeAnchor BOTTOM = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return node.button.rect.centerX();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return node.button.rect.getBottom();
        }
        
    };
    
    public abstract float x(T node);
    
    public abstract float y(T node);
    
}