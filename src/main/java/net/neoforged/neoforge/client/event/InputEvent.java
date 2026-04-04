package net.neoforged.neoforge.client.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * NeoForge InputEvent hierarchy with Forge wrapper constructors.
 */
public abstract class InputEvent extends Event {

    // ── Key ───────────────────────────────────────────────
    public static class Key extends InputEvent {
        private int key;
        private int scanCode;
        private int action;
        private int modifiers;

        /** Required by Forge's EventListenerHelper */
        public Key() {}

        public Key(int key, int scanCode, int action, int modifiers) {
            this.key = key;
            this.scanCode = scanCode;
            this.action = action;
            this.modifiers = modifiers;
        }

        /** Wrapper constructor */
        public Key(net.minecraftforge.client.event.InputEvent.Key forge) {
            this.key = forge.getKey();
            this.scanCode = forge.getScanCode();
            this.action = forge.getAction();
            this.modifiers = forge.getModifiers();
        }

        public int getKey() { return key; }
        public int getScanCode() { return scanCode; }
        public int getAction() { return action; }
        public int getModifiers() { return modifiers; }
    }

    // ── MouseButton ───────────────────────────────────────
    public static abstract class MouseButton extends InputEvent {
        private int button;
        private int action;
        private int modifiers;

        protected MouseButton() {}

        protected MouseButton(int button, int action, int modifiers) {
            this.button = button;
            this.action = action;
            this.modifiers = modifiers;
        }

        public int getButton() { return button; }
        public int getAction() { return action; }
        public int getModifiers() { return modifiers; }

        public static class Pre extends MouseButton implements ICancellableEvent {
            /** Required by Forge's EventListenerHelper */
            public Pre() { super(); }

            public Pre(int button, int action, int modifiers) {
                super(button, action, modifiers);
            }
            /** Wrapper constructor */
            public Pre(net.minecraftforge.client.event.InputEvent.MouseButton.Pre forge) {
                super(forge.getButton(), forge.getAction(), forge.getModifiers());
            }
        }

        public static class Post extends MouseButton {
            /** Required by Forge's EventListenerHelper */
            public Post() { super(); }

            public Post(int button, int action, int modifiers) {
                super(button, action, modifiers);
            }
            /** Wrapper constructor */
            public Post(net.minecraftforge.client.event.InputEvent.MouseButton.Post forge) {
                super(forge.getButton(), forge.getAction(), forge.getModifiers());
            }
        }
    }

    // ── MouseScrollingEvent ───────────────────────────────
    public static class MouseScrollingEvent extends InputEvent implements ICancellableEvent {
        private double deltaX;
        private double deltaY;
        private double mouseX;
        private double mouseY;
        private boolean leftDown;
        private boolean middleDown;
        private boolean rightDown;

        /** Required by Forge's EventListenerHelper */
        public MouseScrollingEvent() {}

        public MouseScrollingEvent(double deltaX, double deltaY,
                                    boolean leftDown, boolean middleDown, boolean rightDown,
                                    double mouseX, double mouseY) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.leftDown = leftDown;
            this.middleDown = middleDown;
            this.rightDown = rightDown;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        /** Wrapper constructor */
        public MouseScrollingEvent(net.minecraftforge.client.event.InputEvent.MouseScrollingEvent forge) {
            this.deltaX = forge.getDeltaX();
            this.deltaY = forge.getDeltaY();
            this.leftDown = forge.isLeftDown();
            this.middleDown = forge.isMiddleDown();
            this.rightDown = forge.isRightDown();
            this.mouseX = forge.getMouseX();
            this.mouseY = forge.getMouseY();
        }

        public double getDeltaX() { return deltaX; }
        public double getDeltaY() { return deltaY; }
        public double getScrollDeltaX() { return deltaX; }
        public double getScrollDeltaY() { return deltaY; }
        public double getMouseX() { return mouseX; }
        public double getMouseY() { return mouseY; }
        public boolean isLeftDown() { return leftDown; }
        public boolean isMiddleDown() { return middleDown; }
        public boolean isRightDown() { return rightDown; }
    }

    // ── InteractionKeyMappingTriggered ────────────────────
    public static class InteractionKeyMappingTriggered extends InputEvent implements ICancellableEvent {
        private int button;
        private KeyMapping keyMapping;
        private InteractionHand hand;
        private boolean handSwing;

        /** Required by Forge's EventListenerHelper */
        public InteractionKeyMappingTriggered() {}

        public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
            this.button = button;
            this.keyMapping = keyMapping;
            this.hand = hand;
            this.handSwing = true;
        }

        /** Wrapper constructor */
        public InteractionKeyMappingTriggered(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered forge) {
            this.button = 0; // not directly exposed in Forge
            this.keyMapping = forge.getKeyMapping();
            this.hand = forge.getHand();
            this.handSwing = forge.shouldSwingHand();
        }

        public KeyMapping getKeyMapping() { return keyMapping; }
        public InteractionHand getHand() { return hand; }
        public boolean shouldSwingHand() { return handSwing; }
        public void setSwingHand(boolean swing) { this.handSwing = swing; }
        public boolean isAttack() { return keyMapping.getName().equals("key.attack"); }
        public boolean isUseItem() { return keyMapping.getName().equals("key.use"); }
        public boolean isPickBlock() { return keyMapping.getName().equals("key.pickItem"); }
    }
}
