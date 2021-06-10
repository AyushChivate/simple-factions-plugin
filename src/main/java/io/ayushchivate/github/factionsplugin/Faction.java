package io.ayushchivate.github.factionsplugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class Faction {
    private String name;
    private UUID owner;
    private Location spawnPoint;
    private ArrayList<UUID> players;

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Faction(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.spawnPoint = null;
        this.players = new ArrayList<>();
    }

    public Faction(String name, UUID owner, Location spawnPoint, ArrayList<UUID> players) {
        this.name = name;
        this.owner = owner;
        this.spawnPoint = spawnPoint;
        this.players = players;
    }

    public void addPlayer(Player player) {

        /* get the player's UUID */
        UUID uuid = player.getUniqueId();

        /* add the player to the list */
        this.players.add(uuid);
    }

    public ArrayList<UUID> getPlayers() {
        return players;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public void teleportPlayer(Player player) {

        /* teleport the player to the faction's spawn point if there is one */
        if (spawnPoint != null) {
            player.teleport(this.spawnPoint);
        }
    }

    public boolean isPlayerInFaction(Player player) {
        for (UUID uuid : this.players) {
            if (player.getUniqueId().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

}
