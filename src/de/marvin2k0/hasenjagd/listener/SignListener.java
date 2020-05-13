package de.marvin2k0.hasenjagd.listener;

import de.marvin2k0.hasenjagd.game.Game;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener
{
    @EventHandler
    public void onSignClick(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null)
            return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType().toString().contains("SIGN"))
        {
            Sign sign = (Sign) block.getState();
            String line1 = sign.getLine(0);
            String line2 = sign.getLine(1).substring(2);

            if (line1.equals("§8[§6HasenJagd§8]") && Game.exists(line2))
            {
                Game game = Game.getGameFromName(line2);
                game.join(player);
                return;
            }
        }
    }

    @EventHandler
    public void onSign(SignChangeEvent event)
    {
        Player player = event.getPlayer();

        if (player.hasPermission("hasenjagd.sign"))
        {
            String line1 = event.getLine(0);
            String line2 = event.getLine(1);

            if (line1.equalsIgnoreCase("[HasenJagd]") && !line2.isEmpty() && Game.exists(line2))
            {
                event.setLine(0, "§8[§6HasenJagd§8]");
                event.setLine(1, "§c" + line2);
            }
        }
    }
}
