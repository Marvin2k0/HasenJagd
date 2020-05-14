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
        config.addDefault("alreadyingame", "&7Du bist schon in einem Spiel!");
        config.addDefault("notingame", "&7Du bist in keinem Spiel!");
        config.addDefault("lobbynotset", "&7Für dieses Spiel wurden noch nicht alle Spawns gesetzt!");
        config.addDefault("joinmessage", "&7[&a+&7] &b%player% &7ist dem Spiel beigetreten.");
        config.addDefault("gamefull", "&7Das Spiel ist voll!");
        config.addDefault("alreadystarted", "&7Das Spiel hat schon angefangen!");
        config.addDefault("hunter", "&7Du bist &eHunter&7! Dein Ziel ist es, die anderen Spieler zu töten.");
        config.addDefault("countdown", "&7Das Spiel startet in &9%seconds% &7Sekunden");
        config.addDefault("hasewintitle", "&6Hasen gewinnen!");
        config.addDefault("hunterwintitle", "&6Der Hunter gewinnt!");
        config.addDefault("hunterwinsubtitle", "&e%hunter% &7konnte alle Hasen erledigen");
        config.addDefault("dead", "&7%player% ist gestorben");
        config.addDefault("murdered", "&8%player% &7wurde von &e%hunter% &7getötet!");
        config.addDefault("restart", "&7Server startet in 10 Sekunden neu!");
        config.addDefault("notplayed", "&7Du hast das Spiel noch nicht gespielt!");
        config.addDefault("passprice", 1000);
        config.addDefault("minplayers", 3);
        config.addDefault("maxplayers", 8);

        saveConfig();
    }

    private static void saveConfig()
    {
        plugin.saveConfig();
    }
}
