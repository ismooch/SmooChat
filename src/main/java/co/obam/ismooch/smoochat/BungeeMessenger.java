package co.obam.ismooch.smoochat;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.List;

/**
 * Created by iSmooch
 */
public class BungeeMessenger implements PluginMessageListener {

    public static Plugin plugin;
    public static List<String> players;

    public BungeeMessenger(Plugin p) {

        p.getServer().getMessenger().registerOutgoingPluginChannel(p, "BungeeCord");
        p.getServer().getMessenger().registerIncomingPluginChannel(p, "BungeeCord", this);
        plugin = p;
    }


    public static void sendCSChat(String group, String message) throws IOException {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ONLINE");
        out.writeUTF("Chat");


        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        msgout.writeUTF(group);
        msgout.writeUTF(message);


        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());
        if (player != null) {
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        }


    }


    public static void sendCSPM(String person, String message) throws IOException {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ONLINE");
        out.writeUTF("pm");


        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        msgout.writeUTF(person);
        msgout.writeUTF(message);


        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());
        if (player != null) {
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        }


    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("BungeeCord")) {

            return;

        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();

        if (subChannel.equals("Chat")) {

            short len = in.readShort();
            byte[] msgbytes = new byte[len];
            in.readFully(msgbytes);

            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            String group = null;
            try {
                group = msgin.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String sendMessage = null;
            try {
                sendMessage = msgin.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (group != null && group.equals("staff")) {

                for (Player get : ChatInteract.staffChannel) {
                    get.sendRawMessage(sendMessage);
                }

            } else if (group != null && group.equals("global")) {

                for (Player get : ChatInteract.globalChannel) {

                    get.sendRawMessage(sendMessage);

                }
            } else if (group != null && group.equals("mechanic")) {

                for (Player get : ChatInteract.mechanicChannel) {

                    get.sendRawMessage(sendMessage);

                }

            } else if (group != null && group.equals("supporter")) {

                for (Player get : ChatInteract.supporterChannel) {

                    get.sendRawMessage(sendMessage);

                }

            }


        }


        if (subChannel.equals("pm")) {

            short len = in.readShort();
            byte[] msgbytes = new byte[len];
            in.readFully(msgbytes);

            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            String person = null;
            try {
                person = msgin.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String sendMessage = null;
            try {
                sendMessage = msgin.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }


            for (Player get : Bukkit.getServer().getOnlinePlayers()) {

                if (get.getName().equalsIgnoreCase(person)) {

                    get.sendRawMessage(sendMessage);

                }

            }

        }


    }
}
