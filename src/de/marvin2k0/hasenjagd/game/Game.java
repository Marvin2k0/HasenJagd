package de.marvin2k0.hasenjagd.game;

import de.marvin2k0.hasenjagd.HasenJagd;
import de.marvin2k0.hasenjagd.utils.Locations;
import de.marvin2k0.hasenjagd.utils.Text;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Game
{
    private static ArrayList<Game> games = new ArrayList<>();
    private static ArrayList<GamePlayer> gameplayers = new ArrayList<>();
    private static ArrayList<Player> players = new ArrayList<>();
    private static FileConfiguration config = HasenJagd.plugin.getConfig();

    private String name;
    private int MIN_PLAYERS;
    private int MAX_PLAYERS;

    private Game(String name)
    {
        this.name = name;
    }

    public void join(Player player)
    {
        if (players.size() >= MAX_PLAYERS)
        {
            player.sendMessage(Text.get("gamefull"));
            return;
        }

        if (players.contains(player))
        {
            player.sendMessage(Text.get("alreadyingame"));
            return;
        }

        HasenJagd.plugin.reloadConfig();

        if (!config.isSet("games." + getName() + ".lobby") || !config.isSet("games." + getName() + ".hunter") || !config.isSet("games." + getName() + ".hase"))
        {
            player.sendMessage(Text.get("lobbynotset"));
            return;
        }

        players.add(player);

        GamePlayer gamePlayer = new GamePlayer(this, player);
        sendMessage(Text.get("joinmessage").replace("%player%", player.getName()));
        player.teleport(Locations.get("games." + getName() + ".lobby"));

        if (players.size() >= MIN_PLAYERS)
        {
            start();
        }
    }

    private void start()
    {

    }

    public void leave(GamePlayer gp)
    {
        if (!gameplayers.contains(gp))
            return;

        Player player = gp.getPlayer();

        players.remove(player);


    }

    public void sendMessage(String msg)
    {
        for (Player player : players)
            player.sendMessage(msg);
    }

    public String getName()
    {
        return name;
    }

    public static Game getGameFromName(String name)
    {
        for (Game game : games)
        {
            if (game.getName().equalsIgnoreCase(name))
                return game;
        }

        return null;
    }

    public static void createGame(String name)
    {
        if (!exists(name))
        {
            Game game = new Game(name);
            games.add(game);
        }
    }

    public static boolean exists(String name)
    {
        for (Game game : games)
        {
            if (game.getName().equalsIgnoreCase(name))
                return true;
        }

        return false;
    }
}
