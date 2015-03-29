package co.obam.ismooch.smoochat;

import co.obam.ismooch.obamapi.ObamAPI;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class ChatLogger {


    public static void logChat(UUID sender, String channel, String server, String message) {

        ObamAPI.openConnection();

        try {

            PreparedStatement sql =
                    ObamAPI.connection.prepareStatement("INSERT INTO Chat_General (UUID, Message, Server, Channel, Time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)");
            sql.setString(1, sender.toString());
            sql.setString(2, message);
            sql.setString(3, server);
            sql.setString(4, channel);
            sql.executeUpdate();
        } catch (SQLException e) {

            e.printStackTrace();

        } finally {

            ObamAPI.closeConnection();
        }

    }

    public static void logPM(UUID sender, String receiver, String server, String message) {

        ObamAPI.openConnection();

        try {
            System.out.println(sender + " " + receiver + " " + server);
            PreparedStatement sql =
                    ObamAPI.connection.prepareStatement("INSERT INTO Chat_PM (`UUID` , `To` , `Message` , `Server` , `Time`) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)");
            sql.setString(1, sender.toString());
            sql.setString(2, receiver);
            sql.setString(3, message);
            sql.setString(4, server);
            sql.executeUpdate();
        } catch (SQLException e) {

            e.printStackTrace();
        } finally {

            ObamAPI.closeConnection();
        }

    }

}
