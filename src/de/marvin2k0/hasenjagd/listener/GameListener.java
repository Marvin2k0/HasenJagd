package de.marvin2k0.hasenjagd.listener;

import de.marvin2k0.hasenjagd.HasenJagd;
import de.marvin2k0.hasenjagd.game.Game;
import de.marvin2k0.hasenjagd.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class GameListener implements Listener
{
    private HashMap<GamePlayer, Long> speedCooldown = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!Game.inGame(event.getPlayer()))
        {
            return;
        }

        Player player = event.getPlayer();
        GamePlayer gp = HasenJagd.gameplayers.get(player);

        if (event.hasItem() && event.getItem().getType() == Material.CARROT)
        {
            if (!player.hasPotionEffect(PotionEffectType.SPEED))
            {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1, false, false, false));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();

            if (!Game.inGame(player))
                return;

            GamePlayer gp = HasenJagd.gameplayers.get(player);

            if (gp.inLobby || gp.getGame().win)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();

        if (Game.inGame(player) && event.getInventory() != null)
        {
            event.setCancelled(true);

            GamePlayer gp = HasenJagd.gameplayers.get(player);

            if (event.getView().getTitle().equals("Pass kaufen"))
            {
                if (event.getCurrentItem() != null)
                {
                    ItemStack item = event.getCurrentItem();

                    if (item.getItemMeta().getDisplayName().equals("§eHunter"))
                    {
                        gp.setRole("hunter");
                    }
                    else if (item.getItemMeta().getDisplayName().equals("§7Hase"))
                    {
                        gp.setRole("hase");
                    }

                    gp.sendMessage("§7Du bist jetzt " + gp.getRole());

                    gp.getGame().updateLobbyScoreboard(true);
                    gp.getGame().updateLobbyScoreboard(false);
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();

        if (Game.inGame(player))
            event.setCancelled(true);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event)
    {
        if (Game.inGame(event.getEntity()))
        {
            Player player = event.getEntity();
            GamePlayer gp = HasenJagd.gameplayers.get(player);

            Bukkit.getScheduler().scheduleSyncDelayedTask(HasenJagd.plugin, () -> player.spigot().respawn(), 1L);
            player.setHealth(player.getHealthScale());
            player.setGameMode(GameMode.SPECTATOR);

            gp.getGame().die(gp, player.getKiller());
            event.getDrops().clear();
            event.setDeathMessage(null);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (Game.inGame(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockPlaceEvent event)
    {
        if (Game.inGame(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(PlayerGameModeChangeEvent event)
    {
        if (Game.inGame(event.getPlayer()) && event.getNewGameMode() == GameMode.CREATIVE)
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cDas ist hier nicht erlaubt! ;)");
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;

        if (Game.inGame((Player) event.getEntity()))
        {
            event.setCancelled(true);
        }
    }
}
