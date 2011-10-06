package com.vk.vIRC;

import com.vk.pool.JDCConnectionDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author victor.konopelko
 *         Date: 26.09.11
 */
public class Properties {

    /** log. */
	private static final Logger log = LoggerFactory.getLogger(Properties.class);

    private final UUID   uuid               = UUID.randomUUID();

    private final String TABLE_NAME         = "properties" + uuid.toString();

    private final String NETWORS_TABLE_NAME = "networks";

    private final String SERVERS_TABLE_NAME = "servers";

    private static final String DATABASE_NAME      = "server.db";

    private static final String CONNECTION         = "jdbc:sqlite:" + DATABASE_NAME;

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION);
    }

    static {
        try {
            new JDCConnectionDriver("org.sqlite.JDBC", CONNECTION, "", "");
            log.info("New pool created successfully.");
        } catch (Exception e) {
            log.error("New pool error", e);
        }
    }


    public Boolean booleanValue(String key) {
        Boolean value = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
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

    public void initializeNetworks() {
        Connection conn = null;

        try {

            boolean tableExist = tableExist(NETWORS_TABLE_NAME);

            conn = getConnection();
            conn.setAutoCommit(false);

            if (!tableExist) {
                Statement stat = conn.createStatement();
                stat.executeUpdate("drop table if exists '" + NETWORS_TABLE_NAME +"';");
                StringBuilder s = new StringBuilder();
                stat.executeUpdate("create table '" + NETWORS_TABLE_NAME +
                                   "' ('name' TEXT(128) NOT NULL, " +
                                   "   'preferred' INTEGER NOT NULL, " +
                                   "   PRIMARY KEY ('name'));");
            }

            PreparedStatement prep = null;

            NetworkList netList = new NetworkList();
            for (String s : netList.get()) {
                prep = conn.prepareStatement("insert into '" + NETWORS_TABLE_NAME + "' values (?, ?);");
                prep.setString(1, s);
                prep.setBoolean(2, false);
                prep.addBatch();
                prep.executeBatch();
            }

            conn.setAutoCommit(true);
        } catch (Exception e) {
            //;
        } finally {
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (Exception e) {
                //;
            }
        }
    }

    public void initializeServers() {
        Connection conn = null;

        try {

            boolean tableExist = tableExist(SERVERS_TABLE_NAME);

            conn = getConnection();
            conn.setAutoCommit(false);

            if (!tableExist) {
                Statement stat = conn.createStatement();
                stat.executeUpdate("drop table if exists '" + SERVERS_TABLE_NAME +"';");
                StringBuilder s = new StringBuilder();
                stat.executeUpdate("create table '" + SERVERS_TABLE_NAME +
                                   "' ('address' TEXT(128) NOT NULL, " +
                                   "   'network' TEXT(128) NOT NULL, " +
                                   "   'preferred' INTEGER NOT NULL);");
                                   //"   PRIMARY KEY ('address'));");
            }

            PreparedStatement prep = null;

            ServerList serverList = new ServerList();
            NetworkList netList = new NetworkList();
            List<String> list = netList.get();
            int i = 0;
            for (String[] ss : serverList.get()) {
                String networkName = list.get(i);
                for (String addr : ss) {
                    prep = conn.prepareStatement("insert into '" + SERVERS_TABLE_NAME + "' values (?, ?, ?);");
                    prep.setString(1, addr);
                    prep.setString(2, networkName);
                    prep.setBoolean(3, false);
                    prep.addBatch();
                    prep.executeBatch();
                }
                i++;
            }

            conn.setAutoCommit(true);
        } catch (Exception e) {
            //;
        } finally {
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (Exception e) {
                //;
            }
        }
    }

    public Properties initialize() {

        Connection conn = null;

        try {

            initializeNetworks();
            initializeServers();

            boolean tableExist = tableExist(TABLE_NAME);

            conn = getConnection();
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
        Connection conn = getConnection();
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
            log.error("", sqlEx);
            exist = false;
        } finally {
            rs.close();
            conn.close();
            stat.close();
        }
        return exist;
    }

    private void checkIntegrity() throws Exception {
        log.info(TABLE_NAME);
        Connection conn = getConnection();
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from '" + TABLE_NAME + "';");
        dumpData(rs, new BufferedWriter(new PrintWriter(System.out)));
        stat.close();
        rs.close();
        conn.close();
    }

    private int dumpData(java.sql.ResultSet rs, java.io.Writer out) throws Exception {
        int rowCount = 0;
        out.write("<P ALIGN='center'><TABLE BORDER=1>\n");
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        // table header
        out.write("<TR>\n");
        for (int i = 0; i < columnCount; i++) {
            out.write("  <TH>" + rsmd.getColumnLabel(i + 1) + "</TH>\n");
        }
        out.write("</TR>\n");
        // the data
        while (rs.next()) {
            rowCount++;
            out.write("<TR>\n");
            for (int i = 0; i < columnCount; i++) {
                out.write("  <TD>" + rs.getString(i + 1) + "</TD>\n");
            }
            out.write("</TR>\n");
        }
        out.write("</TABLE></P>\n");
        out.flush();
        return rowCount;
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

    public static class NetworkList {

        private List<String> list = new ArrayList<String>(76);

        public synchronized List<String> get() {
            list.clear();
            list.add("2600net");
            list.add("AccessIRC");
            list.add("AfterNET");
            list.add("Aitvaras");
            list.add("AmigaNet");
            list.add("ARCNet");
            list.add("AstroLink");
            list.add("AustNet");
            list.add("AzzurraNet");
            list.add("Beirut");
            list.add("ChatJunkies");
            list.add("ChatNet");
            list.add("ChatSociety");
            list.add("ChatSpike");
            list.add("CoolChat");
            list.add("Criten");
            list.add("DALnet");
            list.add("Dark-Tou-Net");
            list.add("DarkMyst");
            list.add("DeepIRC");
            list.add("DeltaAnime");
            list.add("EFnet");
            list.add("EnterTheGame");
            list.add("EUIrc");
            list.add("EuropNet");
            list.add("EU-IRC");
            list.add("FDFNet");
            list.add("FEFNet");
            list.add("FreeNode");
            list.add("GalaxyNet");
            list.add("GamesNET");
            list.add("German-Elite");
            list.add("GimpNet");
            list.add("HabberNet");
            list.add("Hashmark");
            list.add("IdleMonkeys");
            list.add("insiderZ.DE");
            list.add("IrcLink");
            list.add("IRCNet");
            list.add("Irctoo.net");
            list.add("Krstarica");
            list.add("Librenet");
            list.add("LinkNet");
            list.add("MagicStar");
            list.add("Majistic");
            list.add("MindForge");
            list.add("MintIRC");
            list.add("MIXXnet");
            list.add("NeverNET");
            list.add("NixHelpNet");
            list.add("NullusNet");
            list.add("Oceanius");
            list.add("OFTC");
            list.add("OtherNet");
            list.add("OzNet");
            list.add("PTlink");
            list.add("PTNet, ISP's");
            list.add("PTNet, UNI");
            list.add("QuakeNet");
            list.add("RebelChat");
            list.add("RizeNET");
            list.add("RusNet");
            list.add("SceneNet");
            list.add("SlashNET");
            list.add("Sohbet.Net");
            list.add("SolidIRC");
            list.add("SorceryNet");
            list.add("Spidernet");
            list.add("StarChat");
            list.add("TNI3");
            list.add("UnderNet");
            list.add("UniBG");
            list.add("Whiffle");
            list.add("Worldnet");
            list.add("Xentonix.net");
            list.add("XWorld");
            return list;
        }

    }

    public static class ServerList {

        private List<String[]> list = new ArrayList<String[]>(76);

        public synchronized List<String[]> get() {
            list.clear();
            list.add(new String[]{"irc.2600.net"});//list.add("2600net");
            list.add(new String[]{"irc.accessirc.net", "eu.accessirc.net"});//list.add("AccessIRC");
            list.add(new String[]{"irc.afternet.org", "us.afternet.org", "eu.afternet.org"});//list.add("AfterNET");
            list.add(new String[]{"irc6.ktu.lt/+7668", "irc6.ktu.lt/7666", "irc.data.lt/+6668", "irc-ssl.omnitel.net/+6668", "irc-ssl.le.lt/+9999", "irc.data.lt", "irc.omnitel.net", "irc.ktu.lt", "irc.le.lt", "irc.takas.lt", "irc.5ci.net", "irc.kis.lt"});//list.add("Aitvaras");
            list.add(new String[]{"irc.amiganet.org", "us.amiganet.org", "uk.amiganet.org"});//list.add("AmigaNet");
            list.add(new String[]{"se1.arcnet.vapor.com", "us1.arcnet.vapor.com", "us2.arcnet.vapor.com", "us3.arcnet.vapor.com", "ca1.arcnet.vapor.com", "de1.arcnet.vapor.com", "de3.arcnet.vapor.com", "ch1.arcnet.vapor.com", "be1.arcnet.vapor.com", "nl3.arcnet.vapor.com", "uk1.arcnet.vapor.com", "uk2.arcnet.vapor.com", "fr1.arcnet.vapor.com"});//list.add("ARCNet");
            list.add(new String[]{"irc.astrolink.org"});//list.add("AstroLink");
            list.add(new String[]{"au.austnet.org", "us.austnet.org", "ca.austnet.org"});//list.add("AustNet");
            list.add(new String[]{"irc.azzurra.org", "crypto.azzurra.org"});//list.add("AzzurraNet");
            list.add(new String[]{"irc.beirut.com"});//list.add("Beirut");
            list.add(new String[]{"irc.chatjunkies.org", "nl.chatjunkies.org"});//list.add("ChatJunkies");
            list.add(new String[]{"US.ChatNet.Org", "EU.ChatNet.Org"});//list.add("ChatNet");
            list.add(new String[]{"us.chatsociety.net", "eu.chatsociety.net"});//list.add("ChatSociety");
            list.add(new String[]{"irc.chatspike.net"});//list.add("ChatSpike");
            list.add(new String[]{"irc.coolchat.net"});//list.add("CoolChat");
            list.add(new String[]{"irc.criten.net", "irc.eu.criten.net"});//list.add("Criten");
            list.add(new String[]{"irc.dal.net", "irc.eu.dal.net"});//list.add("DALnet");
            list.add(new String[]{"irc.d-t-net.de", "bw.d-t-net.de", "nc.d-t-net.de", "wakka.d-t-net.de"});//list.add("Dark-Tou-Net");
            list.add(new String[]{"irc.darkmyst.org"});//list.add("DarkMyst");
            list.add(new String[]{"irc.deepirc.net"});//list.add("DeepIRC");
            list.add(new String[]{"irc.deltaanime.net"});//list.add("DeltaAnime");
            list.add(new String[]{"irc.blackened.com", "irc.Prison.NET", "irc.Qeast.net", "irc.efnet.pl", "efnet.demon.co.uk", "irc.lightning.net", "irc.mindspring.com", "irc.easynews.com", "irc.servercentral.net"});//list.add("EFnet");
            list.add(new String[]{"IRC.EnterTheGame.Com"});//list.add("EnterTheGame");
            list.add(new String[]{"irc.ham.de.euirc.net", "irc.ber.de.euirc.net", "irc.ffm.de.euirc.net", "irc.bre.de.euirc.net", "irc.hes.de.euirc.net", "irc.vie.at.euirc.net", "irc.inn.at.euirc.net", "irc.bas.ch.euirc.net"});//list.add("EUIrc");
            list.add(new String[]{"irc.europnet.org"});//list.add("EuropNet");
            list.add(new String[]{"irc.eu-irc.net"});//list.add("EU-IRC");
            list.add(new String[]{"irc.eu.fdfnet.net", "irc.fdfnet.net"});//list.add("FDFNet");
            list.add(new String[]{"irc.fef.net", "irc.ggn.net", "irc.vendetta.com"});//list.add("FEFNet");
            list.add(new String[]{"irc.freenode.net"});//list.add("FreeNode");
            list.add(new String[]{"irc.galaxynet.org"});//list.add("GalaxyNet");
            list.add(new String[]{"irc.gamesnet.net", "irc.ca.gamesnet.net", "irc.eu.gamesnet.net"});//list.add("GamesNET");
            list.add(new String[]{"dominion.german-elite.net", "komatu.german-elite.net"});//list.add("German-Elite");
            list.add(new String[]{"irc.gimp.org", "irc.us.gimp.org"});//list.add("GimpNet");
            list.add(new String[]{"irc.habber.net"});//list.add("HabberNet");
            list.add(new String[]{"irc.hashmark.net"});//list.add("Hashmark");
            list.add(new String[]{"irc.idlemonkeys.net"});//list.add("IdleMonkeys");
            list.add(new String[]{"irc.insiderz.de/6667", "irc.insiderz.de/6666"});//list.add("insiderZ.DE");
            list.add(new String[]{"irc.irclink.net", "Alesund.no.eu.irclink.net", "Oslo.no.eu.irclink.net", "frogn.no.eu.irclink.net", "tonsberg.no.eu.irclink.net"});//list.add("IrcLink");
            list.add(new String[]{"irc.ircnet.com", "irc.stealth.net/6668", "ircnet.demon.co.uk", "irc.datacomm.ch", "random.ircd.de", "ircnet.netvision.net.il", "irc.cs.hut.fi"});//list.add("IRCNet");
            list.add(new String[]{"irc.irctoo.net"});//list.add("Irctoo.net");
            list.add(new String[]{"irc.krstarica.com"});//list.add("Krstarica");
            list.add(new String[]{"irc.librenet.net", "ielf.fr.librenet.net"});//list.add("Librenet");
            list.add(new String[]{"irc.link-net.org", "irc.no.link-net.org", "irc.bahnhof.se"});//list.add("LinkNet");
            list.add(new String[]{"irc.magicstar.net"});//list.add("MagicStar");
            list.add(new String[]{"irc.majistic.net"});//list.add("Majistic");
            list.add(new String[]{"irc.mindforge.org"});//list.add("MindForge");
            list.add(new String[]{"irc.mintirc.net"});//list.add("MintIRC");
            list.add(new String[]{"irc.mixxnet.net"});//list.add("MIXXnet");
            list.add(new String[]{"irc.nevernet.net", "imagine.nevernet.net", "dimension.nevernet.net", "universe.nevernet.net", "wayland.nevernet.net", "forte.nevernet.net"});//list.add("NeverNET");
            list.add(new String[]{"irc.nixhelp.org", "us.nixhelp.org", "uk.nixhelp.org", "uk2.nixhelp.org", "uk3.nixhelp.org", "nl.nixhelp.org", "ca.ld.nixhelp.org", "us.co.nixhelp.org", "us.ca.nixhelp.org", "us.pa.nixhelp.org"});//list.add("NixHelpNet");
            list.add(new String[]{"irc.nullus.net"});//list.add("NullusNet");
            list.add(new String[]{"irc.oceanius.com"});//list.add("Oceanius");
            list.add(new String[]{"irc.oftc.net"});//list.add("OFTC");
            list.add(new String[]{"irc.othernet.org"});//list.add("OtherNet");
            list.add(new String[]{"irc.oz.org"});//list.add("OzNet");
            list.add(new String[]{"irc.PTlink.net", "aaia.PTlink.net"});//list.add("PTlink");
            list.add(new String[]{"irc.PTNet.org", "rccn.PTnet.org", "EUnet.PTnet.org", "madinfo.PTnet.org", "netc2.PTnet.org", "netc1.PTnet.org", "telepac1.ptnet.org", "esoterica.PTnet.org", "ip-hub.ptnet.org", "telepac1.ptnet.org", "nortenet.PTnet.org"});//list.add("PTNet, ISP's");
            list.add(new String[]{"irc.PTNet.org", "rccn.PTnet.org", "uevora.PTnet.org", "umoderna.PTnet.org", "ist.PTnet.org", "aaum.PTnet.org", "uc.PTnet.org", "ualg.ptnet.org", "madinfo.PTnet.org", "ua.PTnet.org", "ipg.PTnet.org", "isec.PTnet.org", "utad.PTnet.org", "iscte.PTnet.org", "ubi.PTnet.org"});//list.add("PTNet, UNI");
            list.add(new String[]{"irc.quakenet.org", "irc.se.quakenet.org", "irc.dk.quakenet.org", "irc.no.quakenet.org", "irc.fi.quakenet.org", "irc.be.quakenet.org", "irc.uk.quakenet.org", "irc.de.quakenet.org", "irc.it.quakenet.org"});//list.add("QuakeNet");
            list.add(new String[]{"irc.rebelchat.org"});//list.add("RebelChat");
            list.add(new String[]{"irc.rizenet.org", "omega.rizenet.org", "evelance.rizenet.org", "lisa.rizenet.org", "scott.rizenet.org "});//list.add("RizeNET");
            list.add(new String[]{"irc.tomsk.net", "irc.rinet.ru", "irc.run.net", "irc.ru", "irc.lucky.net"});//list.add("RusNet");
            list.add(new String[]{"irc.scene.org", "irc.eu.scene.org", "irc.us.scene.org"});//list.add("SceneNet");
            list.add(new String[]{"irc.slashnet.org", "area51.slashnet.org", "moo.slashnet.org", "radon.slashnet.org"});//list.add("SlashNET");
            list.add(new String[]{"irc.sohbet.net"});//list.add("Sohbet.Net");
            list.add(new String[]{"irc.solidirc.com"});//list.add("SolidIRC");
            list.add(new String[]{"irc.sorcery.net/9000", "irc.us.sorcery.net/9000", "irc.eu.sorcery.net/9000"});//list.add("SorceryNet");
            list.add(new String[]{"us.spidernet.org", "eu.spidernet.org", "irc.spidernet.org"});//list.add("Spidernet");
            list.add(new String[]{"irc.starchat.net", "gainesville.starchat.net", "freebsd.starchat.net", "sunset.starchat.net", "revenge.starchat.net", "tahoma.starchat.net", "neo.starchat.net"});//list.add("StarChat");
            list.add(new String[]{"irc.tni3.com"});//list.add("TNI3");
            list.add(new String[]{"us.undernet.org", "eu.undernet.org"});//list.add("UnderNet");
            list.add(new String[]{"irc.lirex.com", "irc.naturella.com", "irc.spnet.net","irc.techno-link.com", "irc.telecoms.bg", "irc.tu-varna.edu"});//list.add("UniBG");
            list.add(new String[]{"irc.whiffle.org"});//list.add("Whiffle");
            list.add(new String[]{"irc.worldnet.net", "irc.fr.worldnet.net"});//list.add("Worldnet");
            list.add(new String[]{"irc.xentonix.net"});//list.add("Xentonix.net");
            list.add(new String[]{"Buffalo.NY.US.XWorld.org", "Minneapolis.MN.US.Xworld.Org", "Rochester.NY.US.XWorld.org", "Bayern.DE.EU.XWorld.Org", "Chicago.IL.US.XWorld.Org"});//list.add("XWorld");
            return list;
        }

    }

}
