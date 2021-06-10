package io.ayushchivate.github.factionsplugin;

import net.dohaw.corelib.CoreLib;
import net.dohaw.corelib.JPUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FactionsPlugin extends JavaPlugin {

    private Scoreboard factionsScoreboard;

    @Override
    public void onEnable() {

        CoreLib.setInstance(this);
        this.factionsScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        HashMap<String, Object> folderInfo = new HashMap<>();
        folderInfo.put("faction-data", getDataFolder());
        JPUtils.validateFilesOrFolders(folderInfo, true);
        JPUtils.registerCommand("simplefactions", new SimpleFactionsCommand(this));
        JPUtils.registerEvents(new PlayerWatcher());

        /* Load data */
        /*
         * get the faction-data folder
         * loop through all the files in the folder
         */

        /* make a file object to represent the folder in which the faction data is stored in */
        File factionData = new File(getDataFolder(), "faction-data");

        for (File file : factionData.listFiles()) {
            FactionSaveData factionSaveData = new FactionSaveData(file);
            String fileName = file.getName().replaceAll(".yml", "");
            FactionMap.addFaction(fileName, factionSaveData.loadData());
        }

        createScoreboardTeams();

    }

    @Override
    public void onDisable() {

        /* save data*/
        /* loop through the map */
        /* see if the faction has a file made for it, if not, make one */
        /* make a new instance of FactionConfig */
        /* use saveData() */

        for (Map.Entry<String, Faction> entry : FactionMap.getFactions().entrySet()) {

            /* create a file object with the path of where you want to store it, or access it from */
            File file = new File(getDataFolder() + "/faction-data", entry.getKey() + ".yml");

            /* create the file if it doesn't exist */
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /* create a FactionConfig to save and retrieve data */
            FactionSaveData factionConfig = new FactionSaveData(file, entry.getValue());

            /* save the data */
            factionConfig.saveData();

        }

    }

    private void createScoreboardTeams() {

        Team nonFactionTeam = factionsScoreboard.registerNewTeam("nonFactionTeam");
        nonFactionTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (FactionMap.getPlayerFaction(player) == null) {
                nonFactionTeam.addPlayer(player);
            }
        }

        Collection<Faction> factions = FactionMap.getFactions().values();
        for (Faction faction : factions) {

            if (factionsScoreboard.getTeam(faction.getName()) == null) {
                Team team = factionsScoreboard.registerNewTeam(faction.getName());
                for (UUID playerUUID : faction.getPlayers()) {
                    OfflinePlayer factionMemberOP = Bukkit.getOfflinePlayer(playerUUID);
                    team.addPlayer(factionMemberOP);
                }
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            }

        }

        updateScoreboards();

    }

    public void updateScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(factionsScoreboard);
        }
    }

    public Team getNonFactionTeam() {
        return factionsScoreboard.getTeam("nonFactionTeam");
    }

    public Scoreboard getFactionsScoreboard() {
        return factionsScoreboard;
    }

}
