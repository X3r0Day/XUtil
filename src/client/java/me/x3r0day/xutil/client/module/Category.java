package me.x3r0day.xutil.client.module;

public final class Category {

    private final String displayName;

    public Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static final Category WORLD = new Category("World");
    public static final Category RENDER = new Category("Render");
    public static final Category MISC = new Category("Misc");

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Category other && displayName.equals(other.displayName);
    }

    @Override
    public int hashCode() {
        return displayName.hashCode();
    }
}
