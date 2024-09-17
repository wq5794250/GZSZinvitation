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
    private FileConfiguration inviteConfig; // 文件配置对象

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

        // 初始化邀请记录配置文件
        this.inviteConfig = YamlConfiguration.loadConfiguration(this.inviteFile);

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
            if (!(sender instanceof Player)) {
                sender.sendMessage("此命令只能由玩家执行!");
                return true;
            }
            Player player = (Player)sender;

            if (args.length != 1) {
                player.sendMessage("/invite <Username>");
                return false;
            }

            String targetPlayerName = args[0];
            if (invitedPlayers.containsKey(targetPlayerName)) {
                player.sendMessage(targetPlayerName + " 已经被邀请过了！");
                return true;
            }

            invitedPlayers.put(targetPlayerName, player.getName());
            player.sendMessage("你已经成功邀请了 " + targetPlayerName);
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
                sender.sendMessage(targetPlayerName + " 尚未被邀请！");
            } else {
                sender.sendMessage(targetPlayerName + " 是由 " + inviter + " 邀请的！");
            }
            return true;
        }
    }

    /**
     * /reload 命令处理器
     */
    private class ReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length > 0) {
                sender.sendMessage("使用 /reload 即可，无需参数。");
                return false;
            }

            // 清空现有邀请记录
            invitedPlayers.clear();

            // 重新加载邀请记录配置文件
            inviteConfig = YamlConfiguration.loadConfiguration(inviteFile);

            // 再次加载邀请记录
            loadInvitedPlayers();

            sender.sendMessage("邀请记录已重新加载。");
            return true;
        }
    }

    /**
     * 监听玩家加入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!invitedPlayers.containsKey(player.getName())) {
            event.setJoinMessage(null); // 取消默认的加入消息
            player.kickPlayer("您尚未被邀请加入此服务器。"); // 踢出玩家
        } else {
            player.sendMessage("欢迎 " + player.getName() + " 加入服务器！");
        }
    }
}