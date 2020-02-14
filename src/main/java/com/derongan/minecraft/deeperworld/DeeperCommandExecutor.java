package com.derongan.minecraft.deeperworld;

import com.derongan.minecraft.deeperworld.player.PlayerManager;
import com.derongan.minecraft.deeperworld.world.WorldManager;
import com.derongan.minecraft.deeperworld.world.section.Section;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class DeeperCommandExecutor implements CommandExecutor, TabCompleter {
    private PlayerManager playerManager;
    private WorldManager worldManager;

    public DeeperCommandExecutor(PlayerManager playerManager, WorldManager worldManager) {
        this.playerManager = playerManager;
        this.worldManager = worldManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;

        if(sender.hasPermission(Permissions.CHANGE_SECTION_PERMISSION))

        if (args.length > 0) {
            player = Bukkit.getPlayer(args[0]);
        } else if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (player != null) {
            if (command.getName().equalsIgnoreCase("sectionoff")) {
                playerManager.setPlayerCanTeleport(player, false);
                sender.sendMessage(String.format("Automatic TP disabled for %s", player.getName()));
                return true;
            } else if (command.getName().equalsIgnoreCase("sectionon")) {
                playerManager.setPlayerCanTeleport(player, true);
                sender.sendMessage(String.format("Automatic TP enabled for %s", player.getName()));
                return true;
            } else if(command.getName().equalsIgnoreCase("linfo")){
                Section section = worldManager.getSectionFor(player.getLocation());

                if(section == null){
                    sender.sendMessage(String.format("%s is not in a managed section", (player.getName())));
                } else {
                    sender.sendMessage(String.format("%s is in section %s", player.getName(), section.getKey()));
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 1)
            return Collections.emptyList();
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(player -> player.getName())
                .filter(name -> name.startsWith(args[0]))
                .collect(toList());
    }
}
