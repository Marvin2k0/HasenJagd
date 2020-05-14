package de.marvin2k0.hasenjagd.game;

import de.marvin2k0.hasenjagd.HasenJagd;
import de.marvin2k0.hasenjagd.utils.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GamePlayer
{
    private static Economy economy = HasenJagd.plugin.getEconomy();

    private Game game;
    private Player player;
    private Location location;
    private ItemStack[] inventory;
    public boolean inLobby = true;
    private String role;

    public GamePlayer(Game game, Player player, Location location, ItemStack[] inventory)
    {
        this.game = game;
        this.player = player;
        this.location = location;
        this.inventory = inventory;
        this.role = "Zufällig";

        if (!HasenJagd.gameplayers.containsKey(player))
            HasenJagd.gameplayers.put(player, this);
    }

    public void setRole(String role)
    {
        if (role.equalsIgnoreCase("hunter"))
        {
            if (getGame().hunter != null)
            {
                sendMessage("§cDas ist nicht mehr möglich!");
                player.closeInventory();
                return;
            }

            this.role = "Hunter";
            getGame().hunter = this;

            if (economy.getBalance(getPlayer()) >= Double.valueOf(Text.get("passprice", false)))
            {
                economy.withdrawPlayer(getPlayer(), Double.valueOf(Text.get("passprice", false)));
            }
            else
            {
                sendMessage("§cDafür hast du nicht genügend Coins! Du hast " + economy.getBalance(player) + " Coins");
            }

            player.closeInventory();
        }

        else if (role.equalsIgnoreCase("hase"))
        {
            this.role = "Hase";

            if (economy.getBalance(getPlayer()) >= Double.valueOf(Text.get("passprice", false)))
            {
                economy.withdrawPlayer(getPlayer(), Double.valueOf(Text.get("passprice", false)));
            }
            else
            {
                sendMessage("§cDafür hast du nicht genügend Coins! Du hast " + economy.getBalance(player) + " Coins");
            }

            player.closeInventory();
        }
    }

    public String getRole()
    {
        return role;
    }

    public void addHase()
    {
        HasenJagd.plugin.getConfig().set("stats." + player.getUniqueId() + ".hase", HasenJagd.plugin.getConfig().getInt("stats." + player.getUniqueId() + ".hase") + 1);
        HasenJagd.plugin.saveConfig();
    }

    public void addHunter()
    {
        HasenJagd.plugin.getConfig().set("stats." + player.getUniqueId() + ".hunter", HasenJagd.plugin.getConfig().getInt("stats." + player.getUniqueId() + ".hunter") + 1);
        HasenJagd.plugin.saveConfig();
    }

    public void teleportBack()
    {
        player.teleport(location);
        player.getInventory().clear();
        player.getInventory().setContents(inventory);
    }

    public void sendMessage(String msg)
    {
        getPlayer().sendMessage(msg);
    }

    public String getName()
    {
        return getPlayer().getName();
    }

    public Game getGame()
    {
        return game;
    }

    public void setGame(Game game)
    {
        this.game  = game;
    }

    public Player getPlayer()
    {
        return player;
    }
}
