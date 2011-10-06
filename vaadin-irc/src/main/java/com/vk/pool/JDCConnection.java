package com.vk.pool;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

public class JDCConnection implements Connection {

    private JDCConnectionPool pool;
    private Connection conn;
    private boolean inuse;
    private long timestamp;


    public JDCConnection(Connection conn, JDCConnectionPool pool) {
        this.conn=conn;
        this.pool=pool;
        this.inuse=false;
        this.timestamp=0;
    }

    public synchronized boolean lease() {
       if(inuse)  {
           return false;
       } else {
          inuse=true;
          timestamp=System.currentTimeMillis();
          return true;
       }
    }
    public boolean validate() {
	try {
            conn.getMetaData();
        }catch (Exception e) {
	    return false;
	}
	return true;
    }

    public boolean inUse() {
        return inuse;
    }

    public long getLastUse() {
        return timestamp;
    }

    public void close() throws SQLException {
        pool.returnConnection(this);
    }

    protected void expireLease() {
        inuse=false;
    }

    protected Connection getConnection() {
        return conn;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return conn.prepareCall(sql);
    }

    public Statement createStatement() throws SQLException {
        return conn.createStatement();
    }

    public String nativeSQL(String sql) throws SQLException {
        return conn.nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void rollback() throws SQLException {
        conn.rollback();
    }

    public boolean isClosed() throws SQLException {
        return conn.isClosed();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return conn.getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        conn.setReadOnly(readOnly);
    }
  
    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        conn.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        return conn.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        conn.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        return conn.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        return conn.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        conn.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return conn.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        conn.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        conn.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return conn.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return conn.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        conn.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return conn.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return conn.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return conn.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return conn.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return conn.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return conn.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return conn.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return conn.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        conn.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        conn.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return conn.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return conn.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return conn.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return conn.createStruct(typeName, attributes);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return conn.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return conn.isWrapperFor(iface);
    }
}
