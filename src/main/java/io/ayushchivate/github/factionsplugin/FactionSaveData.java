package io.ayushchivate.github.factionsplugin;

import net.dohaw.corelib.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

public class FactionSaveData extends Config {

    private Faction faction;

    public FactionSaveData(File file) {
        super(file);
    }

    public FactionSaveData(File file, Faction faction) {
        super(file);
        this.faction = faction;
    }

    public void saveData() {
        /* get the value in the field */
        config.set("name", this.faction.getName());
        config.set("owner", this.faction.getOwner().toString());
        config.set("spawn point", this.faction.getSpawnPoint());
        config.set("players", playersUUIDToString(faction.getPlayers()));
        saveConfig();
    }

    public Faction loadData() {

        /* get the value of each field from the data file and store it */
        String factionName = config.getString("name");
        UUID owner = UUID.fromString(config.getString("owner"));
        Location spawnPoint = config.getLocation("spawn point");
        ArrayList<UUID> players = playersStringToUUID((ArrayList<String>) config.getStringList("players"));

        return new Faction(factionName, owner, spawnPoint, players);
    }

    /* converts a list of UUIDs to a list of Strings */
    public ArrayList<String> playersUUIDToString(ArrayList<UUID> players) {

        ArrayList<String> stringPlayers = new ArrayList<>();

        for (UUID uuid : players) {
            stringPlayers.add(uuid.toString());
        }

        return stringPlayers;
    }

    /* converts a list of Strings to a list of UUIDs */
    public ArrayList<UUID> playersStringToUUID(ArrayList<String> players) {

        ArrayList<UUID> uuidPlayers = new ArrayList<>();

        for (String stringPlayers : players) {
            uuidPlayers.add(UUID.fromString(stringPlayers));
        }

        return uuidPlayers;
    }
}
