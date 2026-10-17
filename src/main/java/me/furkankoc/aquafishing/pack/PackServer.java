package me.furkankoc.aquafishing.pack;

import com.sun.net.httpserver.HttpServer;
import me.furkankoc.aquafishing.AquaFishing;
import me.furkankoc.aquafishing.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.MessageDigest;

public class PackServer {

    private final AquaFishing plugin;
    private HttpServer server;
    private byte[] sha1Hash;
    private File zipFile;
    private final int port = 25570;

    public PackServer(AquaFishing plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            File target = new File(plugin.getDataFolder(), "AquaFishing-Pack.zip");
            if (!target.exists()) {
                try {
                    plugin.saveResource("AquaFishing-Pack.zip", false);
                } catch (Exception ignored) {}
            }

            if (target.exists()) {
                this.zipFile = target;
            } else {
                String userHome = System.getProperty("user.home");
                File defaultZip = new File(userHome, "AppData/Roaming/.minecraft/resourcepacks/AquaFishing-Pack.zip");
                if (defaultZip.exists()) {
                    this.zipFile = defaultZip;
                } else {
                    this.zipFile = target;
                }
            }

            if (!zipFile.exists()) {
                plugin.getLogger().warning("AquaFishing-Pack.zip bulunamadi: " + zipFile.getAbsolutePath());
                return;
            }

            // SHA-1 Hash hesapla
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (FileInputStream fis = new FileInputStream(zipFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            this.sha1Hash = digest.digest();

            // HTTP Sunucusu Başlat
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/AquaFishing-Pack.zip", exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"AquaFishing-Pack.zip\"");
                exchange.sendResponseHeaders(200, zipFile.length());
                try (FileInputStream fis = new FileInputStream(zipFile);
                     OutputStream os = exchange.getResponseBody()) {
                    fis.transferTo(os);
                }
            });
            server.setExecutor(null);
            server.start();

            plugin.getLogger().info("Dahili Kaynak Paketi Sunucusu http://127.0.0.1:" + port + "/AquaFishing-Pack.zip adresinde baslatildi!");
        } catch (Exception e) {
            plugin.getLogger().warning("Kaynak paketi sunucusu baslatilamadi: " + e.getMessage());
        }
    }

    public void sendPack(Player player) {
        if (server == null || zipFile == null || !zipFile.exists()) return;
        try {
            String url = "http://127.0.0.1:" + port + "/AquaFishing-Pack.zip";
            Component prompt = MessageUtil.parse("<gradient:#00c6ff:#0072ff><b>AquaFishing</b></gradient> <gray>Özel oltalar ve animasyonlar için kaynak paketini yükleyin.");
            player.setResourcePack(url, sha1Hash, prompt, false);
        } catch (Throwable t) {
            try {
                player.setResourcePack("http://127.0.0.1:" + port + "/AquaFishing-Pack.zip");
            } catch (Throwable ignored) {}
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}
