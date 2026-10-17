package me.furkankoc.aquafishing.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private MessageUtil() {}

    public static Component parse(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        // If string contains MiniMessage tags like <gradient...>, always parse with MiniMessage
        if (message.contains("<") && message.contains(">")) {
            return MINI_MESSAGE.deserialize(message);
        }
        // Fallback to legacy color codes (& or §)
        if (message.contains("&") || message.contains("§")) {
            return LEGACY_SERIALIZER.deserialize(message.replace("§", "&"));
        }
        return Component.text(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(parse(message));
        }
    }

    public static String formatWeight(double weight) {
        return String.format(Locale.US, "%.2f", weight);
    }

    public static String formatDuration(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
