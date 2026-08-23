package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import me.x3r0day.xutil.client.module.Module;
import me.x3r0day.xutil.client.module.ModuleManager;
import net.minecraft.client.Minecraft;

public final class ToggleModuleTask extends MacroTask {

    public enum Action {
        TOGGLE("Toggle"),
        ENABLE("Enable"),
        DISABLE("Disable");

        private final String label;

        Action(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public Action next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private String moduleName;
    private Action action;

    public ToggleModuleTask(String moduleName, Action action) {
        this.moduleName = moduleName;
        this.action = action;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String type() {
        return "module";
    }

    @Override
    public String description() {
        return "Toggle module: " + (moduleName.isEmpty() ? "<none>" : moduleName)
            + " (" + action.label().toLowerCase() + ")";
    }

    @Override
    public boolean tick(Minecraft mc) {
        for (Module module : ModuleManager.getModules()) {
            if (module.getName().equals(moduleName)) {
                switch (action) {
                    case TOGGLE -> module.toggle();
                    case ENABLE -> module.setEnabled(true);
                    case DISABLE -> module.setEnabled(false);
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("module", moduleName);
        json.addProperty("action", action.name());
    }
}
