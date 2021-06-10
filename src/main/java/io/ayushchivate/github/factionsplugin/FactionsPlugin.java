package io.ayushchivate.github.factionsplugin;

import net.dohaw.corelib.CoreLib;
import net.dohaw.corelib.JPUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class FactionsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        CoreLib.setInstance(this);
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
            System.out.println("File name: " + file.getName());
            FactionMap.addFaction(file.getName(),factionSaveData.loadData());
        }
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
}
