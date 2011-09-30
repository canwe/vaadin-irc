package com.vk.vIRC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.UUID;

/**
 * @author victor.konopelko
 *         Date: 26.09.11
 */
public class Properties {

    /** log. */
	private final Logger log = LoggerFactory.getLogger(Properties.class);

    private final UUID   uuid            = UUID.randomUUID();

    private final String TABLE_NAME      = "properties" + uuid.toString();

    private final String DATABASE_NAME   = "server.db";

    private final String CONNECTION      = "jdbc:sqlite:" + DATABASE_NAME;

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

    public boolean isChecked(String prop) {
        Boolean value = booleanValue(prop);
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
            Field[] predefinedFields = PredefinedProperties.class.getDeclaredFields();
            int l = predefinedFields.length;

            if (!tableExist) {
                stat.executeUpdate("drop table if exists '" + TABLE_NAME +"';");
                StringBuilder s = new StringBuilder();
                for (int i = 1; i <= l; s = i < l ? s.append(predefinedFields[i - 1].getName()).append(", ") : s.append(predefinedFields[i - 1].getName()), i++) {}
                stat.executeUpdate("create table '" + TABLE_NAME + "' (" + s + ");");
            }

            StringBuilder s1 = new StringBuilder();
            for (int i = 1; i <= l; s1 = i < l ? s1.append("?, ") : s1.append("?"), i++) {}
            PreparedStatement prep = conn.prepareStatement("insert into '" + TABLE_NAME + "' values (" + s1 + ");");

            final PredefinedProperties pp = new PredefinedProperties();
            final Class<? extends PredefinedProperties> ppClass = pp.getClass();
            for (int i = 1; i <= l; i++) {
                Field f = predefinedFields[i - 1];
                if (f.getType().equals(Boolean.TYPE) || f.getType().equals(Boolean.class)) {
                    setProperty(prep, i, ppClass.getDeclaredMethod("is" + f.getName()).invoke(pp), f.getType());
                } else {
                    setProperty(prep, i, ppClass.getDeclaredMethod("get" + f.getName()).invoke(pp), f.getType());
                }
            }

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
                if (exist) break;
            }
        } catch (SQLException sqlEx) {
            System.out.println(sqlEx);
            exist = false;
        } finally {
            rs.close();
            conn.close();
            stat.close();
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
        stat.close();
        rs.close();
        conn.close();
    }

    private <T> void  setProperty(PreparedStatement prep,
                                  int index,
                                  Object value,
                                  Class<T> valueType) throws SQLException {

        if (value != null) {
            if (valueType.isPrimitive()) {
                if (valueType.equals(Integer.TYPE)) {
                    prep.setInt(index, (Integer) value);
                } else if (valueType.equals(Long.TYPE)) {
                    prep.setLong(index, (Long) value);
                } else if (valueType.equals(Short.TYPE)) {
                    prep.setShort(index, (Short) value);
                } else if (valueType.equals(Character.TYPE)) {
                    prep.setByte(index, (Byte) value);
                } else if (valueType.equals(Byte.TYPE)) {
                    prep.setByte(index, (Byte) value);
                } else if (valueType.equals(Float.TYPE)) {
                    prep.setFloat(index, (Float) value);
                } else if (valueType.equals(Double.TYPE)) {
                    prep.setDouble(index, (Double) value);
                } else if (valueType.equals(Boolean.TYPE)) {
                    prep.setBoolean(index, (Boolean) value);
                } else {
                    throw new RuntimeException("Could not set element with unknown type " + valueType);
                }
            } else {
                if (valueType.isAssignableFrom(java.sql.Array.class)) {
                    prep.setArray(index, (java.sql.Array) value);
                } else {
                    prep.setObject(index, value);
                }
            }
        }
    }

    /**
     * Java bean holds all predefined options
     */
    public static class PredefinedProperties {

        private boolean VIEW_TOPIC_BAR           = true;
        private boolean VIEW_USER_LIST           = true;
        private boolean VIEW_USER_LIST_BUTTONS   = false;
        private boolean VIEW_MODE_BUTTONS        = false;

        public boolean isVIEW_TOPIC_BAR() {
            return VIEW_TOPIC_BAR;
        }

        public boolean isVIEW_USER_LIST() {
            return VIEW_USER_LIST;
        }

        public boolean isVIEW_USER_LIST_BUTTONS() {
            return VIEW_USER_LIST_BUTTONS;
        }

        public boolean isVIEW_MODE_BUTTONS() {
            return VIEW_MODE_BUTTONS;
        }
    }

}
