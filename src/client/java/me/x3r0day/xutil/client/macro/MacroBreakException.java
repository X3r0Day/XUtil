package me.x3r0day.xutil.client.macro;

/**
 * Thrown by {@code BreakTask} to unwind the whole macro chain. Caught by
 * {@code MacroRun.tick} (which stops the run and rethrows) and finally by
 * {@code Macro.tickRun}, which stops the macro itself for repeat triggers.
 */
public final class MacroBreakException extends RuntimeException {

    public MacroBreakException() {
        super(null, null, false, false);
    }
}
