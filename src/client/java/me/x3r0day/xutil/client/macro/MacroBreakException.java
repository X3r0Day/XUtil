package me.x3r0day.xutil.client.macro;

// BreakTask throws this to bail out of the whole chain
public final class MacroBreakException extends RuntimeException {

    public MacroBreakException() {
        super(null, null, false, false);
    }
}
