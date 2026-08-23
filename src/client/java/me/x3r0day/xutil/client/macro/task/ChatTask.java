package me.x3r0day.xutil.client.macro.task;

import com.google.gson.JsonObject;
import me.x3r0day.xutil.client.macro.MacroTask;
import net.minecraft.client.Minecraft;

public final class ChatTask extends MacroTask {

    private String message;

    public ChatTask(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String type() {
        return "chat";
    }

    @Override
    public String description() {
        return "Chat: " + (message.isEmpty() ? "<empty>" : message);
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (mc.getConnection() != null && !message.isEmpty()) {
            if (message.startsWith("/")) {
                mc.getConnection().sendCommand(message.substring(1));
            } else {
                mc.getConnection().sendChat(message);
            }
        }
        return true;
    }

    @Override
    public void toJson(JsonObject json) {
        json.addProperty("message", message);
    }
}
