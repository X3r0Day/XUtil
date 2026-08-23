package me.x3r0day.xutil.client.ui;

import java.util.function.BooleanSupplier;

public record OptionToggle(String label, BooleanSupplier isOn, Runnable toggle) {
}
