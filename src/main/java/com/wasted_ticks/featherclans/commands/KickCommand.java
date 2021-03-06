package com.wasted_ticks.featherclans.commands;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class KickCommand implements CommandExecutor {

    private final FeatherClans plugin;
    private final FeatherClansMessages messages;

    public KickCommand(FeatherClans plugin) {
        this.plugin = plugin;
        this.messages = plugin.getFeatherClansMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("clan_error_player", null));
            return true;
        }

        if (!sender.hasPermission("feather.clans.kick")) {
            sender.sendMessage(messages.get("clan_error_permission", null));
            return true;
        }

        Player originator = (Player) sender;
        boolean leader = plugin.getClanManager().isOfflinePlayerLeader(originator);
        if (!leader) {
            originator.sendMessage(messages.get("clan_error_leader", null));
            return true;
        }

        if (args.length != 2) {
            originator.sendMessage(messages.get("clan_kick_error_no_player_specified", null));
            return true;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            originator.sendMessage(messages.get("clan_kick_error_unresolved_player", null));
            return true;
        }

        String tag = this.plugin.getClanManager().getClanByOfflinePlayer(originator);
        if (this.plugin.getClanManager().isOfflinePlayerInSpecificClan(player, tag)) {
            originator.sendMessage(messages.get("clan_kick_error_not_in_clan", null));
            return true;
        }

        boolean successful = this.plugin.getClanManager().resignOfflinePlayer(player);
        if (!successful) {
            originator.sendMessage(messages.get("clan_kick_error", null));
            return true;
        }

        originator.sendMessage(messages.get("clan_kick_success", Map.of(
                "player", player.getName()
        )));

        player.sendMessage(messages.get("clan_kick_success_target", Map.of(
                "clan", tag
        )));

        return true;
    }
}