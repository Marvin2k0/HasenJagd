package de.marvin2k0.hasenjagd;

import de.marvin2k0.hasenjagd.commands.SetSpawnCommands;
import de.marvin2k0.hasenjagd.game.Game;
import de.marvin2k0.hasenjagd.game.GamePlayer;
import de.marvin2k0.hasenjagd.listener.SignListener;
import de.marvin2k0.hasenjagd.utils.Locations;
import de.marvin2k0.hasenjagd.utils.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class HasenJagd extends JavaPlugin
{
    public static HashMap<Player, GamePlayer> gameplayers = new HashMap<>();
    public static HasenJagd plugin;

    private PluginDescriptionFile descriptionFile;

    @Override
    public void onEnable()
    {
        Locations.setUp(this);
        Text.setUp(this);

        plugin = this;
        descriptionFile = this.getDescription();

        buildConfig();
        addGames();

        getCommand("sethase").setExecutor(new SetSpawnCommands());
        getCommand("sethunter").setExecutor(new SetSpawnCommands());
        getCommand("setlobby").setExecutor(new SetSpawnCommands());
        getCommand("hasenjagd").setExecutor(this);

        getServer().getPluginManager().registerEvents(new SignListener(), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(" ");
            sender.sendMessage(Text.get("prefix") + " §7Plugin by §9" + descriptionFile.getAuthors().get(0) + "§7, §9version " + descriptionFile.getVersion() + " §7enabled!");
            sender.sendMessage(" ");
            return true;
        }

        return true;
    }

    private void addGames()
    {
        if (!getConfig().isSet("games"))
            return;

        Map<String, Object> section = getConfig().getConfigurationSection("games").getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            Game.createGame(entry.getKey());
            System.out.println("Loaded game " + entry.getKey());
        }
    }

    private void buildConfig()
    {
        getConfig().options().header("Plugin von Marvin2k0, erstellt im Auftrag von 'yttobyo'");
        saveConfig();
    }
}
