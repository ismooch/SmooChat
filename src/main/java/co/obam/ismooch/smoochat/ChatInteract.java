package co.obam.ismooch.smoochat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

public class ChatInteract {

    //declaring HashSets for the channels
    /*
     * TODO Find a more dynamic way of handling channels as to be able to create
	 * channels with appropriate permission checks for admin use
	 */
    public static HashSet<Player> globalChannel = new HashSet<Player>();
    public static HashSet<Player> supporterChannel = new HashSet<Player>();
    public static HashSet<Player> staffChannel = new HashSet<Player>();
    public static HashSet<Player> mechanicChannel = new HashSet<Player>();
    public static HashSet<Player> chatOffPlayers = new HashSet<Player>();
    public static HashMap<Player, String> privateChannel = new HashMap<Player, String>();


    //This method adds a specified player to all channels their permissions allow
    public static void defaultChannel(Player player) {

        //staff channel for Mod and OP's
        if (player.hasPermission("obam.mod")) {

            staffChannel.add(player);

        }

        //multiple supporter permission checks for the supporter channel
        if (player.hasPermission("obam.ult4") || player.hasPermission("obam.plus2") ||
                player.hasPermission("obam.supp1") || player.hasPermission("obam.supporter")) {

            supporterChannel.add(player);
        }

        //player has mechanic permission so add them to the mechanic channel...
        /*
         *TODO ?? Redundancy check on obam.mechanic vs. obam.supporter.. may not need one or the other
		 */
        if (player.hasPermission("obam.mechanic")) {

            mechanicChannel.add(player);
        }

        //all players will be added to global channel by default
        globalChannel.add(player);

		/*
		 * TODO Add a 'Game' channel for use in game instances that does not go cross server
		 * and handles additional arguments of which game they are in so only the appropriate users
		 * see them
		 */


    }

    //This method adds a specified player to the specified channel with permission checks
	/*
	 * TODO ?? If use seems needed, add a method that adds a player to a channel regardless of permission for 
	 * admin pushing/pulling ??
	 */
    public static void addToChannel(String channel, Player player) {

        if (channel.equalsIgnoreCase("staff") && player.hasPermission("obam.mod")) {

            staffChannel.add(player);


            //Broke up the if statement into a nested statement for supporter channel for easier to read formatting purposes
        } else if (channel.equalsIgnoreCase("supporter")) {

            if (player.hasPermission("obam.ult4") || player.hasPermission("obam.plus2") ||
                    player.hasPermission("obam.supp1") || player.hasPermission("obam.supporter")) {

                supporterChannel.add(player);
            }
        } else if (channel.equalsIgnoreCase("mechanic") && player.hasPermission("obam.mechanic")) {

            mechanicChannel.add(player);

        } else if (channel.equalsIgnoreCase("global")) {

            globalChannel.add(player);
        }
    }

    //This method removes a specified player from a specified channel if the player instance is indeed in the channel
    public static void removeFromChannel(String channel, Player player) {

        if (channel.equalsIgnoreCase("staff") && staffChannel.contains(player)) {

            staffChannel.remove(player);

        } else if (channel.equalsIgnoreCase("supporter") && supporterChannel.contains(player)) {

            supporterChannel.remove(player);

        } else if (channel.equalsIgnoreCase("mechanic") && mechanicChannel.contains(player)) {

            mechanicChannel.remove(player);

        } else if (channel.equalsIgnoreCase("global") && globalChannel.contains(player)) {

            globalChannel.remove(player);

        }


    }

    //This method crash removes a player from all channels if they are in them
    public static void removeFromAll(Player player) {

        if (staffChannel.contains(player)) {

            staffChannel.remove(player);

        }

        if (supporterChannel.contains(player)) {

            supporterChannel.remove(player);

        }

        if (mechanicChannel.contains(player)) {

            mechanicChannel.remove(player);

        }

        if (globalChannel.contains(player)) {

            globalChannel.remove(player);
        }

    }

    public static void chatOffPlayer(Player player) {

        chatOffPlayers.add(player);

    }

    public static void chatOnPlayer(Player player) {

        if (chatOffPlayers.contains(player)) {

            chatOffPlayers.remove(player);

        }
    }


    public static void setPrivate(Player sender, String target) {

        privateChannel.put(sender, target);

    }

    public static void removePrivate(Player player) {

        if (privateChannel.containsKey(player)) {

            privateChannel.remove(player);

        }

    }

    public static boolean checkPrivate(Player player) {

        if (privateChannel.containsKey(player)) {

            return true;
        } else {

            return false;

        }

    }

    public static String getPrivate(Player player) {

        if (privateChannel.containsKey(player)) {

            String target = privateChannel.get(player);
            return target;

        } else {

            return null;
        }

    }
}
