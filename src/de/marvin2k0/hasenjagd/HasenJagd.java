package de.marvin2k0.hasenjagd;

import de.marvin2k0.hasenjagd.commands.SetSpawnCommands;
import de.marvin2k0.hasenjagd.game.Game;
import de.marvin2k0.hasenjagd.game.GamePlayer;
import de.marvin2k0.hasenjagd.heads.Main;
import de.marvin2k0.hasenjagd.listener.GameListener;
import de.marvin2k0.hasenjagd.listener.PlayerQuitListener;
import de.marvin2k0.hasenjagd.listener.SignListener;
import de.marvin2k0.hasenjagd.utils.Locations;
import de.marvin2k0.hasenjagd.utils.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class HasenJagd extends JavaPlugin implements Listener
{
    public static HashMap<Player, GamePlayer> gameplayers = new HashMap<>();
    public static HasenJagd plugin;

    private PluginDescriptionFile descriptionFile;
    private Economy econ;

    @Override
    public void onEnable()
    {
        if (!setupEconomy())
        {
            Bukkit.getConsoleSender().sendMessage(String.format("[%s] - §4Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Locations.setUp(this);
        Text.setUp(this);

        plugin = this;
        descriptionFile = this.getDescription();

        buildConfig();
        addGames();

        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new GameListener(), this);
        getServer().getPluginManager().registerEvents(this, this);

        getCommand("sethase").setExecutor(new SetSpawnCommands());
        getCommand("sethunter").setExecutor(new SetSpawnCommands());
        getCommand("setlobby").setExecutor(new SetSpawnCommands());
        getCommand("leave").setExecutor(this);
        getCommand("hasenjagd").setExecutor(this);
        getCommand("stats").setExecutor(this);
        getCommand("pass").setExecutor(this);
    }

    @Override
    public void onDisable()
    {
        for (Game game : Game.games)
        {
            game.reset();
        }
    }

    public Economy getEconomy()
    {
        return econ;
    }

    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null)
        {
            return false;
        }
        econ = rsp.getProvider();

        return econ != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (label.equalsIgnoreCase("leave"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage(Text.get("noplayer"));
                return true;
            }

            Player player = (Player) sender;

            if (Game.inGame(player))
            {
                GamePlayer gp = gameplayers.get(player);
                gp.getGame().leaveCommand(gp);
                return true;
            }
        }

        else if (label.equalsIgnoreCase("stats"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage(Text.get("noplayer"));
                return true;
            }

            Player player = (Player) sender;

            if (!getConfig().isSet("stats." + player.getUniqueId().toString()))
            {
                player.sendMessage(Text.get("notplayed"));
                return true;
            }

            int hase = getConfig().getInt("stats." + player.getUniqueId() + ".hase");
            int hunter = getConfig().getInt("stats." + player.getUniqueId() + ".hunter");

            player.sendMessage("§7Gewonnen als Hase: §9" + hase);
            player.sendMessage("§7Gewonnen als Hunter: §9" + hunter);
            return true;
        }

        else if (label.equalsIgnoreCase("pass"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage(Text.get("noplayer"));
                return true;
            }

            Player player = (Player) sender;

            if (!Game.inGame(player))
            {
                player.sendMessage(Text.get("notingame"));
                return true;
            }

            openInv(player);
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage(" ");
            sender.sendMessage(Text.get("prefix") + " §7Plugin by §9" + descriptionFile.getAuthors().get(0) + "§7, §9version " + descriptionFile.getVersion() + " §7enabled!");
            sender.sendMessage(" ");
            return true;
        }

        return true;
    }

    private void openInv(Player player)
    {
        Inventory inv = Bukkit.createInventory(null, 27, "Pass kaufen");

        ItemStack hase = Main.getHead("rabbit");
        ItemMeta haseMeta = hase.getItemMeta();
        haseMeta.setDisplayName("§7Hase");
        hase.setItemMeta(haseMeta);

        ItemStack hunter = Main.getHead("hunter");
        ItemMeta hunterMeta = hunter.getItemMeta();
        hunterMeta.setDisplayName("§eHunter");
        hunter.setItemMeta(hunterMeta);

        inv.setItem(12, hunter);
        inv.setItem(14, hase);

        player.openInventory(inv);
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
