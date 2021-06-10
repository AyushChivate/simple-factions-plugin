package io.ayushchivate.github.factionsplugin;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionMap {
    private static HashMap<String, Faction> factions = new HashMap<>();

    public static HashMap<String, Faction> getFactions() {
        return factions;
    }

    public static void addFaction(String name, Faction faction) {
        factions.put(name, faction);
    }

    public static Faction getFaction(String name) {
        return factions.get(name);
    }

    public static Faction getPlayerFaction(Player player) {
        for (Faction faction : factions.values()) {
            if (faction.isPlayerInFaction(player)) {
                return faction;
            }
        }
        return null;
    }

    public static void deleteFaction(Faction faction) {
        factions.remove(faction.getName());
    }
}
