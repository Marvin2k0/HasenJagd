package de.marvin2k0.hasenjagd.listener;

import de.marvin2k0.hasenjagd.HasenJagd;
import de.marvin2k0.hasenjagd.game.Game;
import de.marvin2k0.hasenjagd.game.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener
{
    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        if (Game.inGame(event.getPlayer()))
        {
            GamePlayer gp = HasenJagd.gameplayers.get(event.getPlayer());
            gp.getGame().leave(gp);
        }
    }
}
