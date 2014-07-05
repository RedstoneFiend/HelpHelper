/* 
 * HelpHelper - Help system control plugin for Bukkit
 * Copyright (C) 2014 Chris Courson http://www.github.com/Chrisbotcom
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/gpl-3.0.html.
 */

package io.github.chrisbotcom.helphelper;

import java.util.logging.Level;
import me.bigteddy98.packetapi.PacketAPI;
import me.bigteddy98.packetapi.api.PacketHandler;
import me.bigteddy98.packetapi.api.PacketListener;
import me.bigteddy98.packetapi.api.PacketRecieveEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author chrisbot
 */
public final class HelpHelper extends JavaPlugin implements Listener, PacketListener {

    FileConfiguration config;
    boolean tabComplete, slashTab, slashHelp, slashQuestion, convertHelp;

    @Override
    public void onEnable() {
        // Load config
        this.config = this.getConfig();
        this.config.options().copyDefaults(true);
        this.saveConfig();
        tabComplete = config.getBoolean("tab-complete-enabled");
        slashTab = config.getBoolean("slash-tab-enabled");
        slashHelp = config.getBoolean("slash-help-enabled");
        slashQuestion = config.getBoolean("slash-question-mark-enabled");
        convertHelp = config.getBoolean("convert-question-mark-to-help");

        getLogger().log(Level.INFO, "tabComplete: {0} slashTab: {1}", new Object[]{tabComplete, slashTab});

        // Load PacketAPI
        Plugin plugin = getServer().getPluginManager().getPlugin("PacketAPI");
        if (plugin != null) {
            PacketAPI.getInstance().addListener(this);
            getServer().getPluginManager().registerEvents(this, this);
        } else {
            getLogger().log(Level.SEVERE, "Cannot enable HelpHelper because dependency (PacketAPI) is not installed.");
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("HelpHelper verion " +this.getDescription().getVersion());
            return true;
        }
        
        return false;
    }

    @SuppressWarnings("deprecation")
    @PacketHandler
    public void onReceive(PacketRecieveEvent event) {
        if (event.getPacket().getName().equals("PacketPlayInTabComplete")) {
            if (getServer().getPlayer(event.getRecieverName()).isOp()) {
                return;
            }
            if (!tabComplete) {
                event.setCancelled(true);
            } else {
                String message = ((String) event.getPacket().getValue("a"));
                if (message.startsWith("/?") || (message.startsWith("/") && !message.contains(" "))) {
                    if (!slashTab) {
                        event.setCancelled(true);
                    }
                }
            }
            return;
        }

        if (event.getPacket().getName().equals("PacketPlayInChat")) {

            String message = ((String) event.getPacket().getValue("message")).toLowerCase();

            if (message.startsWith("/?")) {
                if (!slashQuestion) {
                    Player player = getServer().getPlayer(event.getRecieverName());
                    if (player != null) {
                        player.sendMessage(ChatColor.RED + config.getString("no-help-message"));
                    }
                    event.setCancelled(true);
                } else {
                    if (convertHelp) {
                        event.getPacket().setValue("message", message.replace("/?", "/help"));
                    }
                }
            } else if (message.startsWith("/help") && !slashHelp) {
                Player player = getServer().getPlayer(event.getRecieverName());
                if (player != null) {
                    player.sendMessage(ChatColor.RED + config.getString("no-help-message"));
                }
                event.setCancelled(true);
            }
        }
    }
}
