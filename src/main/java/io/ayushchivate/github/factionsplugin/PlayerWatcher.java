package io.ayushchivate.github.factionsplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public class PlayerWatcher implements Listener {


    /* allow players to send messages only to players in their faction */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {

        /* don't do anything if faction chat is disabled */
        if (!SimpleFactionsCommand.getFactionChatEnabled().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        /* cancel the original message */
        e.setCancelled(true);

        /* get the player's faction */
        Faction faction = FactionMap.getPlayerFaction(e.getPlayer());

        /* don't do anything if the player is not in a faction */
        if (faction == null) {
            return;
        }

        /* get the player's message */
        String message = e.getMessage();

        /* send the message to all the players in the faction */
        for (UUID uuid : faction.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[Faction Chat]" + ChatColor.WHITE + " <" + e.getPlayer().getDisplayName() + "> " + message);
            }
        }
    }

    /* respawn the player to their faction's spawn point if they do not have a bed */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {

        System.out.println("PlayerRespawnEvent has fired.");

        /* get the player and their faction */
        Player player = e.getPlayer();
        Faction faction = FactionMap.getPlayerFaction(player);

        System.out.printf("faction: %s, getSpawnPoint(): %s, !isBedSpawn(): %s, getBedSpawnLocation(): %s", faction, faction.getSpawnPoint(), !e.isBedSpawn(), player.getBedSpawnLocation());

        /* respawn the player to the faction's spawn point if they are in a faction, the faction has a spawn point set,
         * and the player does not have a bed */
        if (faction != null && faction.getSpawnPoint() != null && player.getBedSpawnLocation() == null) {
            System.out.println("I have reached the setRespawnLocation() line.");
            e.setRespawnLocation(faction.getSpawnPoint());
        }
    }
}
