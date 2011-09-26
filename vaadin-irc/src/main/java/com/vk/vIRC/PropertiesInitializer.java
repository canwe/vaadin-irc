package com.vk.vIRC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @author victor.konopelko
 *         Date: 26.09.11
 */
public class PropertiesInitializer {

    /** log. */
	private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

    public static void initialize() {

        Connection conn = null;

        try {
            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection("jdbc:sqlite:server.db");
            conn.setAutoCommit(false);

            Statement stat = conn.createStatement();
            stat.executeUpdate("drop table if exists properties;");

            stat.executeUpdate("create table properties (" +
                               "  VIEW_TOPIC_BAR, " +
                               "  VIEW_USER_LIST," +
                               "  VIEW_USER_LIST_BUTTONS," +
                               "  VIEW_MODE_BUTTONS" +
                               ");");
            PreparedStatement prep = conn.prepareStatement("insert into properties values (" +
                                                           "  ?, " +
                                                           "  ?," +
                                                           "  ?," +
                                                           "  ?" +
                                                           ");");

            prep.setBoolean(1, true);
            prep.setBoolean(2, true);
            prep.setBoolean(3, false);
            prep.setBoolean(4, false);
            prep.addBatch();
            prep.executeBatch();

            conn.setAutoCommit(true);
        } catch (Exception e) {
            //;
        } finally {
            try {
                if (null != conn) {
                    conn.close();
                }
                check();
            } catch (Exception e) {
                //;
            }
        }
    }

    private static void check() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:server.db");
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from properties;");
        while (rs.next()) {
            System.out.println("VIEW_TOPIC_BAR = " + rs.getBoolean("VIEW_TOPIC_BAR"));
            System.out.println("VIEW_USER_LIST = " + rs.getBoolean("VIEW_USER_LIST"));
            System.out.println("VIEW_USER_LIST_BUTTONS = " + rs.getBoolean("VIEW_USER_LIST_BUTTONS"));
            System.out.println("VIEW_MODE_BUTTONS = " + rs.getBoolean("VIEW_MODE_BUTTONS"));
        }
        rs.close();
        conn.close();
    }

}
