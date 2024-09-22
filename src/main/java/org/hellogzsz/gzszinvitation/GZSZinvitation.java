package org.hellogzsz.gzszinvitation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class GZSZinvitation extends JavaPlugin implements Listener {

    private File dataFolder; // 数据文件夹
    private File inviteFile; // 邀请记录文件
    private File configFile; // 配置文件
    private FileConfiguration inviteConfig; // 邀请记录配置对象
    private FileConfiguration config; // 主配置对象

    private Map<String, String> invitedPlayers = new HashMap<>(); // 存储已邀请玩家的数据

    @Override
    public void onEnable() {
        // 初始化数据文件夹
        this.dataFolder = getDataFolder();
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs(); // 如果不存在则创建目录
        }

        // 初始化邀请记录文件
        this.inviteFile = new File(this.dataFolder, "invites.yml");
        if (!this.inviteFile.exists()) {
            try {
                this.inviteFile.createNewFile(); // 如果文件不存在则创建新文件
            } catch (IOException e) {
                getLogger().severe("无法创建邀请记录文件: " + e.getMessage());
                return;
            }
        }

        // 初始化配置文件
        this.configFile = new File(this.dataFolder, "config.yml");
        if (!this.configFile.exists()) {
            saveDefaultConfig(); // 如果配置文件不存在，则保存默认配置
        }

        // 初始化邀请记录配置文件
        this.inviteConfig = YamlConfiguration.loadConfiguration(this.inviteFile);

        // 初始化主配置文件
        this.config = getConfig();

        // 加载现有的邀请记录
        loadInvitedPlayers();

        // 注册命令处理器
        getCommand("invite").setExecutor(new InviteCommand());
        getCommand("whoinvited").setExecutor(new WhoInvitedCommand());
        getCommand("ireload").setExecutor(new ReloadCommand());

        // 注册监听器
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // 在插件关闭前保存数据
        saveInvitedPlayers();
    }

    /**
     * 加载邀请记录
     */
    private void loadInvitedPlayers() {
        // 从配置文件中读取邀请记录并存入内存
        for (String invited : inviteConfig.getKeys(false)) {
            invitedPlayers.put(invited, inviteConfig.getString(invited));
        }
    }

    /**
     * 保存邀请记录
     */
    private void saveInvitedPlayers() {
        // 将内存中的邀请记录写入配置文件
        for (Map.Entry<String, String> entry : invitedPlayers.entrySet()) {
            inviteConfig.set(entry.getKey(), entry.getValue());
        }
        try {
            inviteConfig.save(inviteFile); // 保存配置文件
        } catch (IOException e) {
            getLogger().severe("无法保存邀请记录文件: " + e.getMessage());
        }
    }

    /**
     * /invite <Username> 命令处理器
     */
    private class InviteCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length != 1) {
                sender.sendMessage("/invite <Username>");
                return false;
            }

            String targetPlayerName = args[0];

            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (invitedPlayers.containsKey(targetPlayerName)) {
                    sender.sendMessage(config.getString("invite-messages.already-invited").replace("%player%", targetPlayerName));
                    return true;
                }

                invitedPlayers.put(targetPlayerName, player.getName());
                player.sendMessage(config.getString("invite-messages.invite-success").replace("%player%", targetPlayerName));
            } else { // 控制台发送命令的情况
                if (invitedPlayers.containsKey(targetPlayerName)) {
                    sender.sendMessage(config.getString("invite-messages.already-invited").replace("%player%", targetPlayerName));
                    return true;
                }

                invitedPlayers.put(targetPlayerName, "Console");
                sender.sendMessage(config.getString("invite-messages.invite-success-by-console").replace("%player%", targetPlayerName));
            }
            return true;
        }
    }

    /**
     * /whoinvited <Username> 命令处理器
     */
    private class WhoInvitedCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length != 1) {
                sender.sendMessage("/whoinvited <Username>");
                return false;
            }

            String targetPlayerName = args[0];
            String inviter = invitedPlayers.get(targetPlayerName);
            if (inviter == null) {
                sender.sendMessage(config.getString("invite-messages.not-invited").replace("%player%", targetPlayerName));
            } else {
                sender.sendMessage(config.getString("invite-messages.invited-by")
                        .replace("%player%", targetPlayerName)
                        .replace("%inviter%", inviter));
            }
            return true;
        }
    }

    /**
     * /ireload 命令处理器
     */
    private class ReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length > 0) {
                sender.sendMessage("使用 /ireload 即可，无需参数。");
                return false;
            }

            // 清空现有邀请记录
            invitedPlayers.clear();

            // 重新加载邀请记录配置文件
            inviteConfig = YamlConfiguration.loadConfiguration(inviteFile);

            // 再次加载邀请记录
            loadInvitedPlayers();

            sender.sendMessage(config.getString("invite-messages.reload-message"));
            return true;
        }
    }

    /**
     * 监听玩家加入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否为OP
        if (player.isOp()) {
            if (config.getBoolean("bypass-invite.enabled")) {
                player.sendMessage(config.getString("bypass-invite.message"));
                return;
            }
        }

        if (!invitedPlayers.containsKey(player.getName())) {
            event.setJoinMessage(null); // 取消默认的加入消息
            player.kickPlayer(config.getString("invite-messages.kick-message").replace("%player%", player.getName())); // 踢出玩家
        }
    }
}