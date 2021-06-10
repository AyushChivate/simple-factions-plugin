package io.ayushchivate.github.factionsplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimpleFactionsCommand implements CommandExecutor {

    private FactionsPlugin plugin;

    public SimpleFactionsCommand(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    /* contains players who have faction chat enabled */
    private static Set<UUID> factionChatEnabled = new HashSet<>();

    public static Set<UUID> getFactionChatEnabled() {
        return factionChatEnabled;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        /* make sure sender is of type Player */
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        /* make sure the player has specified arguments in the command */
        if (args.length == 0) {
            return false;
        }

        /* identify and run the command that was executed */
        switch (args[0]) {
            case "create":
                createCommand(player, args);
                break;
            case "join":
                joinCommand(player, args);
                break;
            case "setspawn":
                setspawnCommand(player);
                break;
            case "chat":
                chatCommand(player);
                break;
            case "leave":
                leaveCommand(player);
                break;
            case "help":
                helpCommand(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "That is not a valid command.");
                return false;
        }
        return true;
    }

    private void createCommand(Player player, String[] args) {

        /* usage check */
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Incorrect usage. Please use /sf create [faction name].");
            return;
        }

        /* make sure this player is not already in another faction */
        if (FactionMap.getPlayerFaction(player) != null) {
            player.sendMessage(ChatColor.RED + "You are already in a faction!");
            return;
        }

        /* store the player's faction */
        String factionName = args[1];

        /* prevent duplicate factions */
        if (FactionMap.getFaction(factionName) != null) {
            player.sendMessage(ChatColor.RED + "There is already a faction with this name!");
            return;
        }

        /* create a player and add them to the faction */
        UUID uuid = player.getUniqueId();
        Faction faction = new Faction(factionName, uuid);
        faction.addPlayer(player);

        /* add a new faction to the map */
        FactionMap.addFaction(factionName, faction);

        /* send the player a success message */
        player.sendMessage(ChatColor.GREEN + "The faction was successfully created!");
    }

    private void joinCommand(Player player, String[] args) {

        /* usage check */
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Incorrect usage. Please use /sf join [faction name].");
            return;
        }

        /* make sure this player is not already in another faction */
        if (FactionMap.getPlayerFaction(player) != null) {
            player.sendMessage(ChatColor.RED + "You are already in a faction!");
            return;
        }

        /* get the faction object */
        String factionName = args[1];
        Faction faction = FactionMap.getFaction(factionName);

        /* make sure faction exists */
        if (faction == null) {
            player.sendMessage(ChatColor.RED + "No such faction exists!");
            return;
        }

        /* add the player to the faction */
        faction.addPlayer(player);

        /* teleport the player to the faction's spawn point */
        faction.teleportPlayer(player);

        Team factionTeam = plugin.getFactionsScoreboard().getTeam(faction.getName());
        if(factionTeam != null){
            factionTeam.addPlayer(player);
            plugin.updateScoreboards();
        }
        /* send player and owner success messages */
        player.sendMessage(ChatColor.GREEN + "You have joined the " + factionName + " faction.");
        Player owner = Bukkit.getPlayer(faction.getOwner());
        if (owner != null) {
            owner.sendMessage(ChatColor.YELLOW + player.getDisplayName() + " has joined your faction.");
        }


    }

    private void setspawnCommand(Player player) {

        /* get the faction object */
        Faction faction = FactionMap.getPlayerFaction(player);

        /* make sure the faction exists */
        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction!");
            return;
        }

        /* check if the player is the owner */
        if (!faction.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the faction owner can do this!");
            return;
        }

        /* get the player's location */
        Location location = player.getLocation();

        /* set the faction's spawn point */
        faction.setSpawnPoint(location);

        /* send the player a success message */
        player.sendMessage(ChatColor.GREEN + "Spawn point set.");
    }

    private void chatCommand(Player player) {

        /* don't let the player use this command if they are not in a faction */
        Faction faction = FactionMap.getPlayerFaction(player);
        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to do this!");
            return;
        }

        /* enable or disable faction chat and send a success message */
        if (factionChatEnabled.contains(player.getUniqueId())) {
            factionChatEnabled.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Faction chat disabled.");
        } else {
            factionChatEnabled.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Faction chat enabled.");
        }
    }

    private void leaveCommand(Player player) {

        /* don't let the player use this command if they are not in a faction */
        Faction faction = FactionMap.getPlayerFaction(player);
        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to do this!");
            return;
        }

        Scoreboard scoreboard = plugin.getFactionsScoreboard();
        Team factionTeam = scoreboard.getTeam(faction.getName());

        /* check if the player is the owner */
        if (faction.getOwner().equals(player.getUniqueId())) {

            /* notify all players that the faction has been deleted */
            for (UUID uuid : faction.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                /* make sure the player is online and isn't the owner */
                if (p != null && !uuid.equals(faction.getOwner())) {
                    p.sendMessage(ChatColor.YELLOW + "The faction you were in has been deleted.");
                }
            }
            /* send the owner a success message */
            player.sendMessage(ChatColor.GREEN + "Your faction has been deleted.");

            /* delete faction */
            FactionMap.deleteFaction(faction);

            /* delete faction file */
            File factionData = new File(plugin.getDataFolder() + "/faction-data", faction.getName() + ".yml");
            factionData.delete();

            if(factionTeam != null){
                factionTeam.unregister();
            }

            plugin.updateScoreboards();

            /* if the player is not an owner */
        } else {
            /* remove the player and send them a success message */
            faction.removePlayer(player);

            if(factionTeam != null){
                factionTeam.removePlayer(player);
                plugin.getNonFactionTeam().addPlayer(player);
                plugin.updateScoreboards();
            }

            player.sendMessage(ChatColor.GREEN + "You have left the faction.");
        }
    }

    public void helpCommand(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=============================================");
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Simple Factions Commands:");
        player.sendMessage("");

        player.sendMessage(ChatColor.LIGHT_PURPLE + "  ○ " + ChatColor.YELLOW + "/sf create [faction name]");
        player.sendMessage(ChatColor.WHITE + "        Creates a faction with the specified name and sets the");
        player.sendMessage(ChatColor.WHITE + "        player who used this command as the owner. A player");
        player.sendMessage(ChatColor.WHITE + "        can only be the owner of one faction at a time. Duplicate");
        player.sendMessage(ChatColor.WHITE + "        factions are not allowed.");

        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "  ○ " + ChatColor.YELLOW + "-/sf join [faction name]");
        player.sendMessage(ChatColor.WHITE + "        Allows the player to join the specified faction and");
        player.sendMessage(ChatColor.WHITE + "        notifies the faction owner. Upon using this command, the");
        player.sendMessage(ChatColor.WHITE + "        player is teleported to the faction's spawn point if one");
        player.sendMessage(ChatColor.WHITE + "        is set. A player can only join one faction at a time.");
        player.sendMessage(ChatColor.WHITE + "        Faction owners cannot join other factions.");

        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "  ○ " + ChatColor.YELLOW + "/sf setspawn");
        player.sendMessage(ChatColor.WHITE + "        Sets the spawn point of the faction. When a player dies,");
        player.sendMessage(ChatColor.WHITE + "        they will respawn at this location, if they do not have a");
        player.sendMessage(ChatColor.WHITE + "        bed. Players will also be teleported to this location");
        player.sendMessage(ChatColor.WHITE + "        when they join the faction. Only faction owners can use");
        player.sendMessage(ChatColor.WHITE + "        this command.");

        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "  ○ " + ChatColor.YELLOW + "/sf chat");
        player.sendMessage(ChatColor.WHITE + "        Toggles faction chat on and off. When toggled on, the");
        player.sendMessage(ChatColor.WHITE + "        player will only be able to chat with others members of");
        player.sendMessage(ChatColor.WHITE + "        their faction. When disabled, the player will be able to");
        player.sendMessage(ChatColor.WHITE + "        chat with all players, regardless of their faction. The");
        player.sendMessage(ChatColor.WHITE + "        player will be able to see both, global chat and faction");
        player.sendMessage(ChatColor.WHITE + "        chat at all times.");

        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "  ○ " + ChatColor.YELLOW + "/sf leave");
        player.sendMessage(ChatColor.WHITE + "        Allows the player to leave their current faction and");
        player.sendMessage(ChatColor.WHITE + "        notifies the faction owner. If a faction owner uses this");
        player.sendMessage(ChatColor.WHITE + "        command, then the faction will be deleted, and all");
        player.sendMessage(ChatColor.WHITE + "        members will be notified.");

        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "  ○ " + ChatColor.YELLOW + "/sf help");
        player.sendMessage(ChatColor.WHITE + "        Lists and describes all of the commands in this plugin.");

        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "=============================================");
    }
}
