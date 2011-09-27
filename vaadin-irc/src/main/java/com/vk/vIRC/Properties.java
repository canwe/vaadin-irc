package com.vk.vIRC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

/**
 * @author victor.konopelko
 *         Date: 26.09.11
 */
public class Properties {

    /** log. */
	private final Logger log = LoggerFactory.getLogger(Properties.class);

    private final UUID uuid = UUID.randomUUID();

    private final String TABLE_NAME = "properties" + uuid.toString();

    private final String CONNECTION = "jdbc:sqlite:server.db";

    public Boolean booleanValue(String key) {
        Boolean value = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(CONNECTION);
            Statement stat = conn.createStatement();
            rs = stat.executeQuery("SELECT " + key + " " +
                                   "FROM '" + TABLE_NAME + "';");
            while (rs.next()) {
                value = rs.getBoolean(key);
            }
        } catch (SQLException sqlEx) {
            //;
        } finally {
            try {
                if (null != rs) rs.close();
                if (null != conn) conn.close();
            } catch (Exception e) {
                //;
            }
        }
        return value;
    }

    public boolean VIEW_TOPIC_BAR() {
        Boolean value = booleanValue("VIEW_TOPIC_BAR");
        return value != null ? value : false;
    }

    public boolean VIEW_USER_LIST() {
        Boolean value = booleanValue("VIEW_USER_LIST");
        return value != null ? value : false;
    }

    public boolean VIEW_USER_LIST_BUTTONS() {
        Boolean value = booleanValue("VIEW_USER_LIST_BUTTONS");
        return value != null ? value : false;
    }

    public boolean VIEW_MODE_BUTTONS() {
        Boolean value = booleanValue("VIEW_MODE_BUTTONS");
        return value != null ? value : false;
    }

    public Properties initialize() {

        Connection conn = null;

        try {
            Class.forName("org.sqlite.JDBC");

            boolean tableExist = tableExist(TABLE_NAME);

            conn = DriverManager.getConnection(CONNECTION);
            conn.setAutoCommit(false);

            Statement stat = conn.createStatement();
            if (!tableExist) {
                stat.executeUpdate("drop table if exists '" + TABLE_NAME +"';");
                stat.executeUpdate("create table '" + TABLE_NAME + "' (" +
                                   "  VIEW_TOPIC_BAR, " +
                                   "  VIEW_USER_LIST," +
                                   "  VIEW_USER_LIST_BUTTONS," +
                                   "  VIEW_MODE_BUTTONS" +
                                   ");");
            }


            PreparedStatement prep = conn.prepareStatement("insert into '" + TABLE_NAME + "' values (" +
                                                           "  ?, " +
                                                           "  ?, " +
                                                           "  ?, " +
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
                checkIntegrity();
            } catch (Exception e) {
                //;
            }
        }
        return this;
    }

    //http://stackoverflow.com/questions/1601151/how-do-i-check-in-sqlite-whether-a-table-exists
    private boolean tableExist(final String tableName) throws Exception {
        boolean exist = false;
        Connection conn = DriverManager.getConnection(CONNECTION);
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("SELECT name " +
                                         "FROM sqlite_master " +
                                         "WHERE type='table' " +
                                         "  AND name='" + tableName + "';");
        try {
            while (rs.next()) {
                exist = tableName.equals(rs.getString("name"));
                if (exist) break; else continue;
            }
        } catch (SQLException sqlEx) {
            System.out.println(sqlEx);
            exist = false;
        } finally {
            rs.close();
            conn.close();
        }
        return exist;
    }

    private void checkIntegrity() throws Exception {
        System.out.println(TABLE_NAME);
        Connection conn = DriverManager.getConnection(CONNECTION);
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from '" + TABLE_NAME + "';");
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
