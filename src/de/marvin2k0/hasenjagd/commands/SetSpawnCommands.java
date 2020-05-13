package de.marvin2k0.hasenjagd.commands;

import de.marvin2k0.hasenjagd.HasenJagd;
import de.marvin2k0.hasenjagd.game.Game;
import de.marvin2k0.hasenjagd.utils.Locations;
import de.marvin2k0.hasenjagd.utils.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetSpawnCommands implements CommandExecutor
{
    private FileConfiguration config = HasenJagd.plugin.getConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Text.get("noplayer"));
            return true;
        }

        Player player  = (Player) sender;

        if (args.length != 1)
        {
            player.sendMessage("§cUsage: /" + label + " <game>");
            return true;
        }

        String game = args[0];
        String spawnName = label.substring(3).toLowerCase();

        if (!config.isSet(game))
            Game.createGame(game);

        Locations.setLocation("games." + game + "." + spawnName, player.getLocation());

        player.sendMessage(Text.get("spawnset").replace("%game%", game));

        return true;
    }
}
