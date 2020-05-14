package de.marvin2k0.hasenjagd.game;

import de.marvin2k0.hasenjagd.HasenJagd;
import de.marvin2k0.hasenjagd.heads.Main;
import de.marvin2k0.hasenjagd.utils.CountdownTimer;
import de.marvin2k0.hasenjagd.utils.Locations;
import de.marvin2k0.hasenjagd.utils.Text;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Game
{
    public static ArrayList<Game> games = new ArrayList<>();
    public static ArrayList<GamePlayer> gameplayers = new ArrayList<>();
    private static ArrayList<Player> players = new ArrayList<>();
    private static FileConfiguration config = HasenJagd.plugin.getConfig();
    private static Random random = new Random();
    private static int MIN_PLAYERS = Integer.valueOf(Text.get("minplayers", false));
    private static int MAX_PLAYERS = Integer.valueOf(Text.get("maxplayers", false));

    private HashMap<Player, Scoreboard> scoreboards = new HashMap<>();
    private ArrayList<GamePlayer> hasen;
    private String name;
    private boolean hasStarted;
    private boolean inGame;
    public GamePlayer hunter;
    private CountdownTimer timer;
    private String last;

    private Game(String name)
    {
        this.hasen = new ArrayList<>();
        this.name = name;
        this.hasStarted = false;
        this.inGame = false;
    }

    public void join(Player player)
    {
        if (inGame)
        {
            player.sendMessage(Text.get("alreadystarted"));
            return;
        }

        if (players.size() >= MAX_PLAYERS)
        {
            player.sendMessage(Text.get("gamefull"));
            return;
        }

        if (HasenJagd.gameplayers.containsKey(player))
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

        GamePlayer gamePlayer = new GamePlayer(this, player, player.getLocation(), player.getInventory().getContents());
        gamePlayer.setGame(this);
        gameplayers.add(gamePlayer);
        gamePlayer.inLobby = true;

        sendMessage(Text.get("joinmessage").replace("%player%", player.getName()));
        player.teleport(Locations.get("games." + getName() + ".lobby"));
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setHealth(player.getHealthScale());

        setLobbyScoreboard(player);
        updateLobbyScoreboard(true);

        if (players.size() >= MIN_PLAYERS && !hasStarted)
        {
            start();
        }
    }

    private void setLobbyScoreboard(Player player)
    {
        Scoreboard lobbyScoreboard;

        if (scoreboards.get(player) == null)
            scoreboards.put(player, Bukkit.getScoreboardManager().getNewScoreboard());

        lobbyScoreboard = scoreboards.get(player);

        Objective objective = lobbyScoreboard.registerNewObjective("lobby", "", "§6Hasenjagd");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("").setScore(8);
        objective.getScore("§7Ausgewählt").setScore(7);
        objective.getScore("§7Zufällig").setScore(6);
        objective.getScore(" ").setScore(5);
        objective.getScore("§7Online:").setScore(4);
        objective.getScore("§6" + (players.size()) + "§7/" + MAX_PLAYERS).setScore(3);
        objective.getScore("  ").setScore(2);

        player.setScoreboard(lobbyScoreboard);
    }

    public void updateLobbyScoreboard(boolean join)
    {
        System.out.println("before " + (players.size() + (join ? -1 : +1)));


        for (Player player : players)
        {
            GamePlayer gp = HasenJagd.gameplayers.get(player);

            Scoreboard lobbyScoreboard = scoreboards.get(player);
            Objective objective = lobbyScoreboard.getObjective("lobby");

            lobbyScoreboard.resetScores("§6" + (players.size() + (join ? -1 : +1)) + "§7/" + MAX_PLAYERS);
            lobbyScoreboard.resetScores("§7Zufällig");

            objective.getScore("§6" + (players.size()) + "§7/" + MAX_PLAYERS).setScore(3);
            objective.getScore("§7" + gp.getRole()).setScore(6);
        }
    }

    public void reset()
    {
        for (GamePlayer gp : gameplayers)
        {
            leave(gp, false);
        }

        Location spawn = Locations.get("games." + getName() + ".lobby");

        for (Entity e : spawn.getWorld().getNearbyEntities(spawn, 50, 50, 50))
        {
            if (e instanceof Arrow)
                e.remove();
        }

        gameplayers.clear();
        players.clear();
        hunter = null;
        hasen.clear();
        scoreboards.clear();
        hasStarted = false;
        inGame = false;

        if (last != null)
            ingameScoreboard.resetScores(last);

        if (timer != null)
            Bukkit.getScheduler().cancelTask(timer.getAssignedTaskId());

        last = null;
    }

    private void check()
    {
        if (checkHunterWin())
        {
            hunter.addHunter();

            for (GamePlayer gp : gameplayers)
            {
                gp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                gp.getPlayer().sendTitle(Text.get("hunterwintitle", false), Text.get("hunterwinsubtitle", false).replace("%hunter%", hunter.getName()), 20, 100, 20);
            }

            sendMessage(Text.get("restart"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(HasenJagd.plugin, () -> reset(), 200);
        }
        else if (checkHaseWin())
        {
            for (GamePlayer gp : hasen)
            {
                gp.addHase();
            }

            for (GamePlayer gp : gameplayers)
            {
                gp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                gp.getPlayer().sendTitle(Text.get("hasewintitle", false), "", 20, 100, 20);
            }

            sendMessage(Text.get("restart"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(HasenJagd.plugin, () -> reset(), 200);
        }
        else if (gameplayers.size() <= 1)
        {
            sendMessage(Text.get("restart"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(HasenJagd.plugin, () -> reset(), 200);
        }
    }

    private boolean checkHunterWin()
    {
        if (hunter == null)
        {
            return false;
        }
        else
        {
            if ((players.size() < 2))
            {
                if (hunter.getPlayer().getName().equals(players.get(0).getName()))
                    return true;
            }
        }
        return false;
    }

    private boolean checkHaseWin()
    {
        return hunter == null;
    }

    public void die(GamePlayer gp, Player player)
    {
        System.out.println(players.contains(player));

        if (!players.contains(player))
            sendMessage(Text.get("dead").replace("%player%", gp.getName()));
        else
            sendMessage(Text.get("murdered").replace("%player%", gp.getName()).replace("%hunter%", player.getName()));

        if (hunter.getName().equals(gp.getName()))
            hunter = null;

        players.remove(gp.getPlayer());

        check();
    }

    private void startGame()
    {
        if (gameplayers.size() <= 1)
        {
            reset();
            return;
        }

        assignRoles();
        inGame = true;

        for (GamePlayer gp : gameplayers)
        {
            gp.inLobby = false;

            if (!hunter.getPlayer().getName().equals(gp.getPlayer().getName()))
            {
                gp.getPlayer().teleport(Locations.get("games." + getName() + ".hase"));
            }
            else
            {
                gp.getPlayer().teleport(Locations.get("games." + getName() + ".hunter"));
            }

            setIngameScoreboard(gp.getPlayer());
        }

        hasenImSpiel = hasen.size();

        timer = new CountdownTimer(HasenJagd.plugin, 15 * 60,
                () -> {
                },
                () -> {
                } /*win()*/,
                (t) -> updateIngameScoreboard(t.getSecondsLeft()));

        timer.scheduleTimer();
    }

    Scoreboard ingameScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    Objective objective = ingameScoreboard.registerNewObjective("ingame", "", "§6Hasenjagd");
    Team countdown = ingameScoreboard.registerNewTeam("countdown");


    private void setIngameScoreboard(Player player)
    {
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore(" ").setScore(6);
        objective.getScore("§7Lebende Hasen:").setScore(5);
        objective.getScore("§7" + hasen.size() + "/" + (players.size() - 1)).setScore(4);
        objective.getScore("  ").setScore(3);
        objective.getScore("§7Rest Zeit").setScore(2);
        objective.getScore("§7").setScore(1);

        countdown.addEntry("§7");
        countdown.setSuffix("15:00");

        player.setScoreboard(ingameScoreboard);
    }

    int hasenImSpiel;

    private void updateIngameScorebard()
    {
        System.out.println("updated");
        ingameScoreboard.resetScores("§7" + (hasen.size() + 1) + "/" + (hasenImSpiel));

        objective.getScore("§7" + hasen.size() + "/" + (hasenImSpiel)).setScore(4);
        last = "§7" + hasen.size() + "/" + (hasenImSpiel);
    }

    private void updateIngameScoreboard(int left)
    {
        countdown.setSuffix(left / 60 + ":" + (((left % 60) < 10) ? 0 + "" + (left % 60) : left % 60));
    }

    private void start()
    {
        hasStarted = true;

        CountdownTimer timer = new CountdownTimer(HasenJagd.plugin, 60,
                () -> {
                },
                () -> startGame(),
                (t) -> countdown(t.getSecondsLeft()));

        timer.scheduleTimer();
    }

    private void countdown(int seconds)
    {
        if (seconds <= 5)
            sendMessage(Text.get("countdown").replace("%seconds%", seconds + ""));
        else if (seconds % 5 == 0)
            sendMessage(Text.get("countdown").replace("%seconds%", seconds + ""));
    }

    private void assignRoles()
    {
        if (hunter == null)
        {
            int i = random.nextInt(gameplayers.size());

            hunter = gameplayers.get(i);
        }

        hunter.sendMessage(Text.get("hunter"));
        setHunterItems(hunter);

        for (GamePlayer gp : gameplayers)
        {
            if (!gp.getName().equals(hunter.getName()))
            {
                setHasenItems(gp);
                hasen.add(gp);
            }
        }
    }

    private void setHasenItems(GamePlayer gp)
    {
        Player player = gp.getPlayer();

        ItemStack head = null;

        try
        {
            head = Main.getHead("rabbit");
        }
        catch (Exception e)
        {
        }

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();
        meta.setColor(Color.fromRGB(0x737775));
        chest.setItemMeta(meta);

        ItemStack leggins = new ItemStack(Material.LEATHER_LEGGINGS);
        meta = (LeatherArmorMeta) leggins.getItemMeta();
        meta.setColor(Color.fromRGB(0x737775));
        leggins.setItemMeta(meta);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(Color.fromRGB(0x737775));
        boots.setItemMeta(meta);

        ItemStack carrots = new ItemStack(Material.CARROT);
        carrots.setAmount(15);

        player.getInventory().setHelmet(head);
        player.getInventory().setChestplate(chest);
        player.getInventory().setLeggings(leggins);
        player.getInventory().setBoots(boots);
        player.getInventory().addItem(carrots);
    }

    private void setHunterItems(GamePlayer gp)
    {
        Player player = gp.getPlayer();

        ItemStack head = Main.getHead("hunter");

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();
        meta.setColor(Color.fromRGB(0xBDB82C));
        chest.setItemMeta(meta);

        ItemStack leggins = new ItemStack(Material.LEATHER_LEGGINGS);
        meta = (LeatherArmorMeta) leggins.getItemMeta();
        meta.setColor(Color.fromRGB(0xBDB82C));
        leggins.setItemMeta(meta);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(Color.fromRGB(0xBDB82C));
        boots.setItemMeta(meta);

        ItemStack schwert = new ItemStack(Material.STONE_SWORD);
        schwert.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
        schwert.addUnsafeEnchantment(Enchantment.DURABILITY, 3);

        ItemStack bogen = new ItemStack(Material.BOW);
        bogen.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        bogen.addUnsafeEnchantment(Enchantment.DURABILITY, 3);


        player.getInventory().setHelmet(head);
        player.getInventory().setChestplate(chest);
        player.getInventory().setLeggings(leggins);
        player.getInventory().setBoots(boots);
        player.getInventory().setItem(0, schwert);
        player.getInventory().setItem(1, bogen);
        player.getInventory().setItem(8, new ItemStack(Material.ARROW));

        player.updateInventory();
    }

    public void leave(GamePlayer gp)
    {
        gameplayers.remove(gp);
        players.remove(gp.getPlayer());

        leave(gp, true);
    }

    public void leaveCommand(GamePlayer gp)
    {
        gameplayers.remove(gp);
        players.remove(gp.getPlayer());

        leave(gp, true);
    }

    public void leave(GamePlayer gp, boolean check)
    {
        Player player = gp.getPlayer();

        if (hunter != null && hunter.getGame().equals(player.getName()))
            hunter = null;

        if (hasen.contains(gp))
            hasen.remove(gp);

        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        gp.teleportBack();
        HasenJagd.gameplayers.remove(player);

        if (!inGame)
            updateLobbyScoreboard(false);
        else
            updateIngameScorebard();


        if (check)
            check();
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

    public static boolean inGame(Player player)
    {
        return HasenJagd.gameplayers.containsKey(player);
    }
}
