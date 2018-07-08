package com.derongan.minecraft.deeperworld;

import com.derongan.minecraft.deeperworld.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class DeeperCommandExecutor implements CommandExecutor, TabCompleter {
    private PlayerManager playerManager;

    public DeeperCommandExecutor(PlayerManager playerManager) {
        this.playerManager = playerManager;
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
            if (command.getName().equals("sectionoff")) {
                playerManager.setPlayerCanTeleport(player, false);
                player.sendMessage(String.format("Automatic TP disabled for %s", player.getName()));
                return true;
            } else if (command.getName().equals("sectionon")) {
                playerManager.setPlayerCanTeleport(player, true);
                player.sendMessage(String.format("Automatic TP enabled for %s", player.getName()));
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
                .map(a->((Player) a).getName())
                .filter(a -> a.startsWith(args[0]))
                .collect(toList());
    }
}
