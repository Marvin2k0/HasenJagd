package de.marvin2k0.hasenjagd.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Text
{
    static FileConfiguration config;
    static Plugin plugin;

    public static String get(String path)
    {
        return path.equalsIgnoreCase("prefix") ? get(path, false) : get(path, true);
    }

    public static String get(String path, boolean prefix)
    {
        return ChatColor.translateAlternateColorCodes('&', prefix ? config.getString("prefix") + " " + config.getString(path) : config.getString(path));
    }

    public static void setUp(Plugin plugin)
    {
        Text.plugin = plugin;
        Text.config = plugin.getConfig();

        config.options().copyDefaults(true);
        config.addDefault("prefix", "&8[&6HasenJagd&8]&f");
        config.addDefault("noplayer", "&7Dieser Befehl ist nur für Spieler");
        config.addDefault("spawnset", "&7Der Spawn wurde &aerfolgreich &7für Spiel &b%game% &7gesetzt!");
        config.addDefault("alreadyingame", "&7Du bist schon in diesem Spiel!");
        config.addDefault("lobbynotset", "&7Für dieses Spiel wurden noch nicht alle Spawns gesetzt!");
        config.addDefault("joinmessage", "&7[&a+&7] &b%player% &7ist dem Spiel beigetreten.");
        config.addDefault("gamefull", "&7Das Spiel ist voll!");

        saveConfig();
    }

    private static void saveConfig()
    {
        plugin.saveConfig();
    }
}
