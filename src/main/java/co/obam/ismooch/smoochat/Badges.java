package co.obam.ismooch.smoochat;

import co.obam.ismooch.obamapi.ObamAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by troyj_000 on 3/27/2015.
 */
public class Badges implements Listener {


    public static Map<UUID, String> Badges = new HashMap<UUID, String>();
    public static Map<String, String> badgeMap = new HashMap<String, String>();
    public static List<String> badgeList = new ArrayList<String>();

    @EventHandler
    public static void onPlayerConnect(PlayerJoinEvent e) {


        UUID uuid = e.getPlayer().getUniqueId();

        ObamAPI.openConnection();
        try {
            PreparedStatement sql = ObamAPI.connection.prepareStatement("SELECT * FROM Badges WHERE UUID = ?");

            String uuidString = uuid.toString();
            sql.setString(1, uuidString);
            ResultSet rs = sql.executeQuery();
            if (rs.next()) {

                if (!Badges.containsKey(e.getPlayer().getUniqueId())) {

                    Badges.put(e.getPlayer().getUniqueId(), rs.getString("Active"));

                } else {

                    Badges.remove(e.getPlayer().getUniqueId());
                    Badges.put(e.getPlayer().getUniqueId(), rs.getString("Active"));

                }

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            ObamAPI.closeConnection();

        }


    }

    @EventHandler
    public static void onPlayerDisconnect(PlayerQuitEvent e) {

        UUID uuid = e.getPlayer().getUniqueId();

        if (Badges.containsKey(uuid)) {

            Badges.remove(uuid);

        }

    }

    public static void badgeDefine() {

        ObamAPI.openConnection();

        try {

            PreparedStatement sql = ObamAPI.connection.prepareStatement("SELECT * FROM BadgeList");
            ResultSet rs = sql.executeQuery();
            if (rs.next()) {

                badgeMap.put(rs.getString("BadgeName"), rs.getString("BadgeValue"));
                badgeList.add(rs.getString("BadgeName"));

            }
        } catch (SQLException e) {

            e.printStackTrace();

        } finally {

            ObamAPI.closeConnection();

        }

    }

    public static void activeBadgeSet(UUID uuid, String badge) {

        ObamAPI.openConnection();


        try {

            PreparedStatement sql = ObamAPI.connection.prepareStatement("UPDATE Badges SET Active = ? WHERE UUID = ?");
            sql.setString(1, badge);
            sql.setString(2, uuid.toString());
            sql.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        } finally {

            ObamAPI.closeConnection();
        }

    }

    public static void badgeGive(UUID uuid, String badge) {

        ObamAPI.openConnection();

        try {

            PreparedStatement sql =
                    ObamAPI.connection.prepareStatement("UPDATE Badges SET " + badge + " = true WHERE UUID = ?");
            sql.setString(1, uuid.toString());
            sql.executeUpdate();
        } catch (SQLException e) {

            e.printStackTrace();
        } finally {

            ObamAPI.closeConnection();
        }


    }

    public static void badgeRemove(UUID uuid, String badge) {

        ObamAPI.openConnection();

        try {

            PreparedStatement sql =
                    ObamAPI.connection.prepareStatement("UPDATE Badges SET " + badge + " = false WHERE UUID = ?");
            sql.setString(1, uuid.toString());
            sql.executeUpdate();
        } catch (SQLException e) {

            e.printStackTrace();
        } finally {
            ObamAPI.closeConnection();
        }

    }

    public static boolean hasBadge(UUID uuid, String badge) {

        ObamAPI.openConnection();

        try {

            PreparedStatement sql = ObamAPI.connection.prepareStatement("SELECT * FROM Badges WHERE UUID = ?");
            sql.setString(1, uuid.toString());
            ResultSet rs = sql.executeQuery();

            return rs.next() && (rs.getBoolean(badge));


        } catch (SQLException e) {


            e.printStackTrace();
            return false;

        } finally {

            ObamAPI.closeConnection();
        }

    }

    public static void updatePlayerBadge(UUID uuid) {


        ObamAPI.openConnection();
        try {
            PreparedStatement sql = ObamAPI.connection.prepareStatement("SELECT * FROM Badges WHERE UUID = ?");

            String uuidString = uuid.toString();
            sql.setString(1, uuidString);
            ResultSet rs = sql.executeQuery();
            if (rs.next()) {

                if (!Badges.containsKey(uuid)) {

                    Badges.put(uuid, rs.getString("Active"));

                } else {

                    Badges.remove(uuid);
                    Badges.put(uuid, rs.getString("Active"));

                }

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            ObamAPI.closeConnection();

        }

    }


}
