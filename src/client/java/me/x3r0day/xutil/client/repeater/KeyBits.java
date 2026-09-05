package me.x3r0day.xutil.client.repeater;

import net.minecraft.client.Minecraft;

public final class KeyBits {

    public static final int FORWARD = 1;
    public static final int BACK = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int JUMP = 16;
    public static final int SNEAK = 32;
    public static final int SPRINT = 64;
    public static final int USE = 128;
    public static final int ATTACK = 256;

    private KeyBits() {
    }

    public static int capture(Minecraft mc) {
        int k = 0;
        if (mc.options.keyUp.isDown()) k |= FORWARD;
        if (mc.options.keyDown.isDown()) k |= BACK;
        if (mc.options.keyLeft.isDown()) k |= LEFT;
        if (mc.options.keyRight.isDown()) k |= RIGHT;
        if (mc.options.keyJump.isDown()) k |= JUMP;
        if (mc.options.keyShift.isDown()) k |= SNEAK;
        if (mc.options.keySprint.isDown()) k |= SPRINT;
        if (mc.options.keyUse.isDown()) k |= USE;
        if (mc.options.keyAttack.isDown()) k |= ATTACK;
        return k;
    }

    public static void apply(Minecraft mc, int k) {
        mc.options.keyUp.setDown((k & FORWARD) != 0);
        mc.options.keyDown.setDown((k & BACK) != 0);
        mc.options.keyLeft.setDown((k & LEFT) != 0);
        mc.options.keyRight.setDown((k & RIGHT) != 0);
        mc.options.keyJump.setDown((k & JUMP) != 0);
        mc.options.keyShift.setDown((k & SNEAK) != 0);
        mc.options.keySprint.setDown((k & SPRINT) != 0);
        mc.options.keyUse.setDown((k & USE) != 0);
        mc.options.keyAttack.setDown((k & ATTACK) != 0);
    }

    public static void releaseAll(Minecraft mc) {
        apply(mc, 0);
    }
}
