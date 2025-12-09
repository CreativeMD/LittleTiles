package team.creative.littletiles.common.gui.signal;

import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

public abstract class GuiSignalNodeAnchor {
    
    public static final GuiSignalNodeAnchor LEFT = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return node.rect.getX();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return (node.rect.getY() + node.rect.getBottom()) * 0.5F;
        }
        
    };
    public static final GuiSignalNodeAnchor TOP = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return (node.rect.getX() + node.rect.getRight()) * 0.5F;
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return node.rect.getY();
        }
        
    };
    public static final GuiSignalNodeAnchor RIGHT = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return node.rect.getRight();
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return (node.rect.getY() + node.rect.getBottom()) * 0.5F;
        }
        
    };
    public static final GuiSignalNodeAnchor BOTTOM = new GuiSignalNodeAnchor() {
        
        @Override
        public float x(GuiSignalNode node) {
            return (node.rect.getX() + node.rect.getRight()) * 0.5F;
        }
        
        @Override
        public float y(GuiSignalNode node) {
            return node.rect.getBottom();
        }
        
    };
    
    public abstract float x(GuiSignalNode node);
    
    public abstract float y(GuiSignalNode node);
    
}