package co.obam.ismooch.smoochat;

import co.obam.ismooch.obamapi.ObamAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class SmooChat extends JavaPlugin implements Listener {

	
	/*
     * ----------------------------------------------------
	 * 					BIG TODO
	 *    Add Private Message support, probably the only
	 * 	  Big functionality still not added in. 
	 * 
	 */

    //declaring serverPrefix variable. Used for distinguishing which server the chat originates from
    public static String serverPrefix;

    //declaring the server name variable. Main purpose is for distinction in logging
    public static String serverName;

    //setting up the string HashMap that determines what channel each player is currently chatting in
    public HashMap<String, String> playerChannel = new HashMap<String, String>();


    public void onEnable() {

        new BungeeMessenger(this);
        //registering event listener
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Badges(), this);
        Badges.badgeDefine();

        //grabbing config values


        //server prefix
        this.saveDefaultConfig();
        serverPrefix = this.getConfig().getString("settings.prefix");
        serverPrefix = ChatColor.translateAlternateColorCodes('&', serverPrefix);

        //getting server name
        serverName = this.getConfig().getString("settings.server");

        //grabbing database values for the ChatLogger class
        //TODO implement chat logging via OBAM API


        //looping players to default their channels on reload
        for (Player player : this.getServer().getOnlinePlayers()) {

            ChatInteract.defaultChannel(player);
            playerChannel.put(player.getName(), "global");
        }


    }

    //Command Handler
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {


            if (cmd.getName().equalsIgnoreCase("badge")) {

                Player player = (Player) sender;

                if (args.length < 1) {

                    if (Badges.Badges.containsKey(player.getUniqueId())) {

                        player.sendRawMessage(ChatColor.GREEN + "Your Current Badge is: " + ChatColor.YELLOW +
                                Badges.Badges.get(player.getUniqueId()));
                        player.sendRawMessage(ChatColor.GREEN + "Your Display is: " +
                                ChatColor.translateAlternateColorCodes('&', Badges.badgeMap.get(Badges.Badges.get(player.getUniqueId()))));
                        return true;

                    } else {

                        player.sendRawMessage(ChatColor.GREEN + "You do not have a badge set!");
                        return true;

                    }

                } else if (args.length < 2 && args[0].equalsIgnoreCase("list")) {

                    player.sendRawMessage(ChatColor.GREEN + "List of selectable Badges: ");
                    player.sendRawMessage(
                            ChatColor.YELLOW + "(" + ChatColor.GREEN + "Green" + ChatColor.YELLOW + "= Unlocked / " +
                                    ChatColor.GRAY + "Grey" + ChatColor.YELLOW + "= Locked)");


                    for (String badge : Badges.badgeList) {

                        if (Badges.hasBadge(player.getUniqueId(), badge)) {

                            player.sendRawMessage(ChatColor.YELLOW + badge + ChatColor.GREEN + ": " +
                                    ChatColor.translateAlternateColorCodes('&', Badges.badgeMap.get(badge)));

                        } else {

                            player.sendRawMessage(ChatColor.GRAY + badge + ChatColor.GREEN + ": " +
                                    ChatColor.translateAlternateColorCodes('&', Badges.badgeMap.get(badge)));
                        }


                    }
                    return true;

                } else if (args.length < 2 && args[0].equalsIgnoreCase("set")) {

                    player.sendRawMessage(ChatColor.RED + "You must specify a badge to set!");
                    return true;

                } else if (args.length < 3 && args[0].equalsIgnoreCase("set")) {

                    String badge = args[1];

                    if (!Badges.badgeList.contains(args[1])) {

                        player.sendRawMessage(ChatColor.RED + "That is not a registered badge!");
                        return true;

                    }
                    if (Badges.hasBadge(player.getUniqueId(), badge)) {

                        Badges.activeBadgeSet(player.getUniqueId(), badge);
                        Badges.updatePlayerBadge(player.getUniqueId());
                        player.sendRawMessage(
                                ChatColor.GREEN + "Your badge has been set to " + ChatColor.YELLOW + badge +
                                        ChatColor.GREEN + " : " +
                                        ChatColor.translateAlternateColorCodes('&', Badges.badgeMap.get(Badges.Badges.get(player.getUniqueId()))));
                        return true;

                    } else {

                        player.sendRawMessage("You do not have this badge unlocked!");
                        return true;

                    }

                } else if (args.length < 4 && args[0].equalsIgnoreCase("admin")) {

                    if (player.hasPermission("obam.smod")) {

                        player.sendRawMessage(
                                ChatColor.RED + "You must specify an action, a badge name, and a player name!");

                    } else {

                        player.sendRawMessage(ChatColor.RED + "You do not have permission to do this!");
                    }

                } else if (args.length < 5 && args[0].equalsIgnoreCase("admin")) {

                    if (!player.hasPermission("obam.smod")) {

                        player.sendRawMessage(
                                ChatColor.RED + "You do not have permission to do this! " + ChatColor.YELLOW +
                                        "/badge admin <action> <badge> <player>");


                    } else {

                        if (!ObamAPI.isOBAMPlayer(args[3])) {

                            player.sendRawMessage(
                                    ChatColor.YELLOW + args[3] + ChatColor.RED + " is not a registered player!");
                            return true;

                        }

                        UUID uuid = ObamAPI.getUUID(args[3]);


                        if (args[1].equalsIgnoreCase("set")) {

                            if (!Badges.badgeList.contains(args[2])) {

                                player.sendRawMessage(
                                        ChatColor.YELLOW + args[2] + ChatColor.RED + " is not a registered badge!");
                                return true;

                            } else if (!Badges.hasBadge(uuid, args[2])) {

                                player.sendRawMessage(
                                        ChatColor.YELLOW + args[3] + ChatColor.RED + " does not have the " +
                                                ChatColor.YELLOW + args[2] + ChatColor.RED + " badge.");
                                return true;

                            } else {

                                Badges.activeBadgeSet(uuid, args[2]);
                                Badges.updatePlayerBadge(uuid);
                                player.sendRawMessage(
                                        ChatColor.YELLOW + args[3] + ChatColor.GREEN + " has been assigned the badge " +
                                                ChatColor.YELLOW + args[2] + ChatColor.GREEN + " : " +
                                                ChatColor.translateAlternateColorCodes('&', Badges.badgeMap.get(args[2])));
                                return true;
                            }

                        }else if (args[1].equalsIgnoreCase("give")) {


                            if (!Badges.badgeList.contains(args[2])) {

                                player.sendRawMessage(
                                        ChatColor.YELLOW + args[2] + ChatColor.RED + " is not a registered badge!");
                                return true;


                            } else {

                                Badges.badgeGive(uuid, args[2]);
                                player.sendRawMessage(
                                        ChatColor.YELLOW + args[3] + ChatColor.GREEN + " now has access to the " +
                                                ChatColor.YELLOW + args[2] + ChatColor.GREEN + " badge.");
                                return true;

                            }

                        }else if(args[1].equalsIgnoreCase("remove")){


                            if(!Badges.badgeList.contains(args[2])){

                                player.sendRawMessage(ChatColor.YELLOW + args[2] + ChatColor.RED + " is not a registered badge!");
                                return true;

                            }else {

                                Badges.badgeRemove(uuid, args[2]);
                                player.sendRawMessage(ChatColor.YELLOW + args[3] + ChatColor.GREEN + " now no longer has access to the " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " badge.");
                                return true;
                            }

                        }

                    }

                }else if(args.length < 2 && args[0].equalsIgnoreCase("reload")){



                    if(!player.hasPermission("obam.smod")){

                        player.sendRawMessage(ChatColor.RED + "You do not have permission to do this!");
                        return true;

                    }
                    Badges.badgeDefine();
                    player.sendRawMessage(ChatColor.GREEN + "Badges have been reloaded!");
                    return true;

                }


            }
            if (cmd.getName().equalsIgnoreCase("tell")) {

                Player player = (Player) sender;
                if (args.length < 1) {

                    sender.sendMessage(ChatColor.RED + "You must specify a player!");
                    return true;

                } else if (Bukkit.getPlayer(args[0]) != null) {

                    if (args.length < 2) {

                        if (!ObamAPI.isOBAMPlayer(args[0])) {

                            player.sendRawMessage(
                                    ChatColor.YELLOW + args[0] + ChatColor.RED + " is not a registered OBAM player!");
                            return true;

                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (Bukkit.getPlayer(args[0]) != null) {
                            ChatInteract.setPrivate(player, target.getName());
                            player.sendMessage(
                                    ChatColor.GREEN + "You are now in a conversation with " + ChatColor.YELLOW +
                                            target.getName());
                            return true;

                        } else {

                            ChatInteract.setPrivate(player, args[0]);
                            player.sendMessage(
                                    ChatColor.GREEN + "You are now in a conversation with " + ChatColor.YELLOW +
                                            args[0]);

                            return true;


                        }

                    } else {

                        String message = StringUtils.join(args, ' ', 1, args.length);
                        Player target = Bukkit.getPlayer(args[0]);
                        String sendMessage = String.valueOf(
                                ChatColor.DARK_PURPLE + "❝ " + ChatColor.LIGHT_PURPLE + sender.getName() + " ➽ " +
                                        args[0] + ": " + ChatColor.GRAY + message + ChatColor.BOLD +
                                        ChatColor.DARK_PURPLE + " ❞");
                        if (Bukkit.getPlayer(args[0]) != null) {

                            sender.sendMessage(
                                    ChatColor.DARK_PURPLE + "❝ " + ChatColor.LIGHT_PURPLE + sender.getName() + " ➽ " +
                                            target.getName() + ": " + ChatColor.GRAY + message + ChatColor.BOLD +
                                            ChatColor.DARK_PURPLE + " ❞");
                            target.sendMessage(
                                    ChatColor.DARK_PURPLE + "❝ " + ChatColor.LIGHT_PURPLE + sender.getName() + " ➽ " +
                                            target.getName() + ": " + ChatColor.GRAY + message + ChatColor.BOLD +
                                            ChatColor.DARK_PURPLE + " ❞");

                            ChatLogger.logPM(player.getUniqueId(), ObamAPI.getUUID(args[0]), serverName, message);
                            return true;
                        } else {


                            try {
                                BungeeMessenger.sendCSPM(args[0], sendMessage);
                                ChatLogger.logPM(player.getUniqueId(), ObamAPI.getUUID(args[0]), serverName, message);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    }


                }


            }
            if (cmd.getName().equalsIgnoreCase("chat")) {


                Player player = (Player) sender;

                if (args.length < 1 || args[0].equalsIgnoreCase("help")) {

                    if (args.length < 2 || args[1].equalsIgnoreCase("1")) {

                        player.sendRawMessage(" ");
                        player.sendMessage(ChatColor.DARK_AQUA + "SmooChat (v 3.0) - Help - Page 1 / 2");
                        player.sendMessage(ChatColor.GRAY + "SmooChat is the global chat system for OBAMCraft");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/chat");
                        player.sendMessage(ChatColor.WHITE + "This command opens this help menu");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/chat help [<number>]");
                        player.sendMessage(
                                ChatColor.WHITE +
                                        "Opens the help menu. If a number is specified, it lists that page.");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/chat join <channel name>");
                        player.sendMessage(
                                ChatColor.WHITE + "Let's you join the specified channel if you have permission");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/chat leave <channel name>");
                        player.sendMessage(
                                ChatColor.WHITE + "Let's you leave the specified channel if you are in it");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/chat <channel name>");
                        player.sendMessage(ChatColor.WHITE + "Sets current active channel to specified channel");
                        player.sendMessage("");


                    } else if (args[1].equalsIgnoreCase("2")) {

                        player.sendRawMessage(" ");
                        player.sendMessage(ChatColor.DARK_AQUA + "SmooChat (v 3.0) - Help - Page 2 / 2");
                        player.sendMessage(ChatColor.GRAY + "SmooChat is the global chat system for OBAMCraft");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/pm <player name>");
                        player.sendMessage(ChatColor.WHITE + "Starts a PM session with the specified player");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/pm <player name> <message>");
                        player.sendMessage(ChatColor.WHITE + "Sends a single PM to the specified player.");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/reply");
                        player.sendMessage(
                                ChatColor.WHITE + "Begins a PM session with the last person to send a PM to you.");
                        player.sendMessage("");
                        player.sendMessage(ChatColor.GOLD + "/reply <message>");
                        player.sendMessage(
                                ChatColor.WHITE + "Sends a single message to the last person to send a PM to you.");
                        player.sendMessage("");


                    } else {

                        player.sendMessage(ChatColor.RED + "There are no other help pages!");

                    }

                } else if (args[0].equalsIgnoreCase("staff") || args[0].equalsIgnoreCase("mod") ||
                        args[0].equalsIgnoreCase("m")) {

                    playerChannel.put(player.getName(), "staff");
                    player.sendMessage(
                            ChatColor.YELLOW + "You are now talking in the " + ChatColor.GREEN + "Moderator" +
                                    ChatColor.YELLOW + " channel.");

                } else if (args[0].equalsIgnoreCase("supporter") || args[0].equalsIgnoreCase("s")) {

                    playerChannel.put(player.getName(), "supporter");
                    player.sendMessage(
                            ChatColor.YELLOW + "You are now talking in the " + ChatColor.BLUE + "Supporter " +
                                    ChatColor.YELLOW + "channel.");

                } else if (args[0].equalsIgnoreCase("mech") || args[0].equalsIgnoreCase("mechanic")) {

                    playerChannel.put(player.getName(), "mechanic");
                    player.sendMessage(
                            ChatColor.YELLOW + "You are now talking in the " + ChatColor.GOLD + "Mechanic " +
                                    ChatColor.YELLOW + "channel.");

                } else if (args[0].equalsIgnoreCase("global") || args[0].equalsIgnoreCase("g")) {

                    playerChannel.put(player.getName(), "global");
                    player.sendMessage(
                            ChatColor.YELLOW + "You are now talking in the " + ChatColor.WHITE + "Global " +
                                    ChatColor.YELLOW + "channel.");

                } else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")) {

                    if (args.length < 2) {

                        player.sendMessage(ChatColor.RED + "You must specify a channel name!");

                    } else {

                        String channel = args[1];

                        if (channel.equalsIgnoreCase("moderator") || channel.equalsIgnoreCase("m") ||
                                channel.equalsIgnoreCase("staff")) {

                            if (!player.hasPermission("obam.mod")) {

                                player.sendMessage(ChatColor.RED + "You do not have permission to do this!");

                            } else {

                                ChatInteract.addToChannel("staff", player);
                                player.sendMessage(
                                        ChatColor.YELLOW + "You have joined the " + ChatColor.GREEN + "Moderator " +
                                                ChatColor.YELLOW + "channel.");
                                playerChannel.put(player.getName(), "staff");

                            }
                        } else if (channel.equalsIgnoreCase("supporter") || channel.equalsIgnoreCase("s")) {

                            if (player.hasPermission("obam.ult4") || player.hasPermission("obam.plus2") ||
                                    player.hasPermission("obam.supp1") || player.hasPermission("obam.supporter")) {

                                ChatInteract.addToChannel("supporter", player);
                                player.sendMessage(
                                        ChatColor.YELLOW + "You have joined the " + ChatColor.BLUE + "Supporter " +
                                                ChatColor.YELLOW + "channel");
                                playerChannel.put(player.getName(), "supporter");

                            } else {

                                player.sendMessage(ChatColor.RED + "You do not have permission to do this!");

                            }
                        } else if (channel.equalsIgnoreCase("mechanic") || channel.equalsIgnoreCase("mech")) {

                            if (player.hasPermission("obam.mechanic")) {

                                ChatInteract.addToChannel("mechanic", player);
                                player.sendMessage(
                                        ChatColor.YELLOW + "You have joined the " + ChatColor.GOLD + "Mechanic " +
                                                ChatColor.YELLOW + "channel.");
                                playerChannel.put(player.getName(), "mechanic");

                            } else {

                                player.sendMessage(ChatColor.RED + "You do not have permission to do this!");

                            }

                        } else if (channel.equalsIgnoreCase("global") || channel.equalsIgnoreCase("g")) {

                            ChatInteract.addToChannel("global", player);
                            player.sendMessage(
                                    ChatColor.YELLOW + "You have joined the " + ChatColor.WHITE + "Global " +
                                            ChatColor.YELLOW + "channel.");
                            playerChannel.put(player.getName(), "global");

                        } else {

                            player.sendMessage(
                                    ChatColor.RED + "The channel " + ChatColor.YELLOW + channel + ChatColor.RED +
                                            " is not a valid channel name!");

                        }
                    }
                } else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("l")) {


                    if (args.length < 2) {

                        player.sendMessage(ChatColor.RED + "You must specify a channel name!");

                    } else {

                        String channel = args[1];

                        if (channel.equalsIgnoreCase("moderator") || channel.equalsIgnoreCase("m") ||
                                channel.equalsIgnoreCase("staff")) {

                            if (!player.hasPermission("obam.mod")) {

                                player.sendMessage(ChatColor.RED + "You do not have permission to do this!");

                            } else {

                                player.sendMessage(ChatColor.RED + "You can not leave the staff channel silly!");

                            }
                        } else if (channel.equalsIgnoreCase("supporter") || channel.equalsIgnoreCase("s")) {

                            if (player.hasPermission("obam.ult4") || player.hasPermission("obam.plus2") ||
                                    player.hasPermission("obam.supp1") || player.hasPermission("obam.supporter")) {

                                ChatInteract.removeFromChannel("supporter", player);
                                player.sendMessage(
                                        ChatColor.YELLOW + "You have left the " + ChatColor.BLUE + "Supporter " +
                                                ChatColor.YELLOW + "channel");
                                playerChannel.put(player.getName(), "global");

                            } else {

                                player.sendMessage(ChatColor.RED + "You do not have permission to do this!");

                            }
                        } else if (channel.equalsIgnoreCase("mechanic") || channel.equalsIgnoreCase("mech")) {

                            if (player.hasPermission("obam.mechanic")) {

                                ChatInteract.removeFromChannel("mechanic", player);
                                player.sendMessage(
                                        ChatColor.YELLOW + "You have left the " + ChatColor.GOLD + "Mechanic " +
                                                ChatColor.YELLOW + "channel.");
                                playerChannel.put(player.getName(), "global");

                            } else {

                                player.sendMessage(ChatColor.RED + "You do not have permission to do this!");

                            }

                        } else if (channel.equalsIgnoreCase("global") || channel.equalsIgnoreCase("g")) {

                            ChatInteract.removeFromChannel("global", player);
                            player.sendMessage(
                                    ChatColor.YELLOW + "You have left the " + ChatColor.WHITE + "Global " +
                                            ChatColor.YELLOW + "channel.");
                            playerChannel.put(player.getName(), "none");

                        } else {

                            player.sendMessage(
                                    ChatColor.RED + "The channel " + ChatColor.YELLOW + channel + ChatColor.RED +
                                            " is not a valid channel name!");

                        }
                    }

                } else if (args[0].equalsIgnoreCase("config")) {

                    if (args.length < 2) {

                        if (player.hasPermission("obam.mod")) {
                            player.sendMessage(ChatColor.GOLD + "SmooChat Configuration");
                            player.sendMessage(ChatColor.GOLD + "Server Prefix: " + serverPrefix);
                            player.sendMessage(ChatColor.GOLD + "Server Name: " + ChatColor.YELLOW + serverName);


                        } else {

                            player.sendMessage(ChatColor.RED + "You do not have permission to do this!");

                        }

                    } else if (args[1].equalsIgnoreCase("reload")) {


                        if (player.hasPermission("obam.admin")) {
                            this.reloadConfig();

                            serverPrefix = this.getConfig().getString("settings.prefix");
                            serverPrefix = ChatColor.translateAlternateColorCodes('&', serverPrefix);


                            serverName = this.getConfig().getString("settings.server");


                            player.sendMessage(ChatColor.GREEN + "SmooChat Configuration reloaded");
                        }
                    } else {

                        return false;
                    }

                } else if (args[0].equalsIgnoreCase("off")) {

                    ChatInteract.chatOffPlayer(player);
                    player.sendMessage(ChatColor.RED + "You have turned off chat, to turn back on just use the " +
                            ChatColor.YELLOW + "/chat on " + ChatColor.RED + "command.");


                } else if (args[0].equalsIgnoreCase("on")) {

                    ChatInteract.chatOnPlayer(player);
                    player.sendMessage(
                            ChatColor.GREEN + "You have turned chat on! To turn it back off just use the " +
                                    ChatColor.YELLOW + "/chat off " + ChatColor.GREEN + "command.");

                } else {

                    return false;
                }

            } else {

                sender.sendMessage(ChatColor.RED + "You must be a player to execute these commands!");

            }
        }
        /*
         * TODO Add command values
		 * 
		 * TODO Add bungee checks for available players in other servers, use this for checking if a 
		 * Private message can be found it's receiver on another server
		 */

        sender.sendMessage("Only a player can execute that command!");
        return true;

    }


    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {

        //adds player to channels their permissions permit on joining
        //check out ChatInteract class for more detail
        ChatInteract.defaultChannel(e.getPlayer());

        //sets player default channel to global
        playerChannel.put(e.getPlayer().getName(), "global");

    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {

        //pulling player out of all channels on disconnect to prevent duplicate entries on login and sending to non-existent players
        //check out ChatInteract class for more detail
        ChatInteract.removeFromAll(e.getPlayer());

        //removes player from the playerChannel HashMap to prevent duplicate entries on login
        playerChannel.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent e) throws IOException {

        //player casting for the chat event
        Player player = e.getPlayer();

        //getting the message value
        String message = e.getMessage();

        //grabbing the channel for easy reference
        String channel = playerChannel.get(player.getName());

		/*
		  Canceling the event to prevent duplicate messages
		  I choose to cancel the event instead of formatting the message so I can have a little bit more control
		  over the display
		*/
        e.setCancelled(true);

        //Channel Checking
        //See appropriate ChatSend method in the ChatSend class for more detail
        //The channel names and handling are pretty straight forward
        //Checking on PM's first because.. I just will
        if (ChatInteract.checkPrivate(player)) {

            ChatSend.sendPM(player, message);

        }
        if (channel.equalsIgnoreCase("global")) {

            ChatSend.sendGlobal(player, message);

        } else if (channel.equalsIgnoreCase("staff")) {

            ChatSend.sendStaff(player, message);

        } else if (channel.equalsIgnoreCase("supporter")) {

            ChatSend.sendSupporter(player, message);

        } else if (channel.equalsIgnoreCase("mechanic")) {

            ChatSend.sendMechanic(player, message);

        } else {

            player.sendMessage(ChatColor.RED + "It seems that you are not in a channel.");
            player.sendMessage(ChatColor.RED + "Use the " + ChatColor.YELLOW + "/chat join <channel>" + ChatColor.RED +
                    " command to join one");

        }


    }
}
