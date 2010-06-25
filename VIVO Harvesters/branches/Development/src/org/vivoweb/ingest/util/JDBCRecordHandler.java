/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the new BSD license
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.html
 * 
 * Contributors:
 *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.ingest.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class JDBCRecordHandler extends RecordHandler {
	
	private static Log log = LogFactory.getLog(JDBCRecordHandler.class);
	private Connection db;
	protected Statement cursor;
	protected String table;
	protected String idField;
	protected String dataField;
	
	/**
	 * Default Constructor
	 */
	public JDBCRecordHandler() {
		
	}
	
	/**
	 * Constructor
	 * @param jdbcDriverClass 
	 * @param connLine 
	 * @param username 
	 * @param password 
	 * @param tableName 
	 * @param idFieldName 
	 * @param dataFieldName 
	 * @throws IOException 
	 * 
	 */
	public JDBCRecordHandler(String jdbcDriverClass, String connLine, String username, String password, String tableName, String idFieldName, String dataFieldName) throws IOException {
		initAll(jdbcDriverClass, connLine, username, password, tableName, idFieldName, dataFieldName);
	}
	
	/**
	 * Constructor
	 * @param jdbcDriverClass 
	 * @param connType 
	 * @param host 
	 * @param port 
	 * @param dbName 
	 * @param username 
	 * @param password 
	 * @param tableName 
	 * @param idFieldName 
	 * @param dataFieldName
	 * @throws IOException 
	 * 
	 */
	public JDBCRecordHandler(String jdbcDriverClass, String connType, String host, String port, String dbName, String username, String password, String tableName, String idFieldName, String dataFieldName) throws IOException {
		this(jdbcDriverClass, buildConnLine(connType, host, port, dbName), username, password, tableName, idFieldName, dataFieldName);
	}
	
	/**
	 * Destructor
	 */
	protected void finalize() throws Throwable {
		this.cursor.close();
		this.db.close();
	}
	
	private static String buildConnLine(String connType, String host, String port, String dbName) {
		return "jdbc:"+connType+"://"+host+":"+port+"/"+dbName;
	}
	
	private void initAll(String jdbcDriverClass, String connLine, String username, String password, String tableName, String idFieldName, String dataFieldName) throws IOException {
		this.table = tableName;
		this.idField = idFieldName;
		this.dataField = dataFieldName;
		try {
			Class.forName(jdbcDriverClass);
			this.db = DriverManager.getConnection(connLine, username, password);
			this.cursor = this.db.createStatement();
			if(!checkTableExists()){
				throw new IOException("Database Does Not Contain Table: "+this.table);
			}
			if(!checkTableConfigured()){
				throw new IOException("Table '"+this.table+"' Is Not Structured Correctly");
			}
		} catch(ClassNotFoundException e) {
			log.error("Unable to initialize DB Driver Class",e);
			throw new IOException("Unable to initialize DB Driver Class: "+e.getMessage());
		} catch(SQLException e) {
			log.error("Unable to connect to DB",e);
			throw new IOException("Unable to connect to DB: "+e.getMessage());
		}
	}
	
	private boolean checkTableConfigured() {
		boolean a = true;
		try {
			this.cursor.execute("select UNIQUE_AUTOGENERATED_ID, "+this.idField+", "+this.dataField+" from "+this.table);
		} catch(SQLException e) {
			a = false;
		}
		return a;
	}
	
	private boolean checkTableExists() {
		boolean a;
		try {
			// ANSI SQL way.  Works in PostgreSQL, MSSQL, MySQL
			this.cursor.execute("select case when exists((select * from information_schema.tables where table_name = '"+this.table+"')) then 1 else 0 end");
			a = this.cursor.getResultSet().getBoolean(1);
		} catch(SQLException e) {
			try {
				// Other RDBMS. Graceful degradation
				a = true;
				this.cursor.execute("select 1 from "+this.table+" where 1 = 0");
			} catch(SQLException e1) {
				a = false;
			}
		}
		return a;
	}
	
	@Override
	public void addRecord(Record rec) throws IOException {
		try {
			PreparedStatement ps = this.db.prepareStatement("insert into "+this.table+"("+this.idField+", "+this.dataField+") values (?, ?)");
			ps.setString(1, rec.getID());
			ps.setBytes(2, rec.getData().getBytes());
			ps.executeUpdate();
		} catch(SQLException e) {
			log.error("Unable to add record: "+rec.getID(),e);
			throw new IOException("Unable to add record: "+rec.getID()+" - "+e.getMessage());
		}
	}
	
	@Override
	public void delRecord(String recID) throws IOException {
		try {
			this.cursor.execute("delete from "+this.table+" where "+this.idField+" = "+recID);
		} catch(SQLException e) {
			log.error("Unable to delete record: "+recID,e);
			throw new IOException("Unable to delete record: "+recID+" - "+e.getMessage());
		}
	}
	
	@Override
	public String getRecordData(String recID) throws IllegalArgumentException, IOException {
		try {
			return new String(this.cursor.executeQuery("select "+this.dataField+" from "+this.table+" where "+this.idField+" = "+recID).getBytes(1));
		} catch(SQLException e) {
			log.error("Unable to retrieve record: "+recID,e);
			throw new IOException("Unable to retrieve record: "+recID+" - "+e.getMessage());
		}
	}
	
	@Override
	public Iterator<Record> iterator() {
		JDBCRecordIterator ri = null;
		try {
			ri = new JDBCRecordIterator();
		} catch(SQLException e) {
			log.error("Unable to retrieve records",e);
		}
		return ri;
	}
	
	private class JDBCRecordIterator implements Iterator<Record> {
		ResultSet rs;
		
		protected JDBCRecordIterator() throws SQLException {
			this.rs = JDBCRecordHandler.this.cursor.executeQuery("select "+JDBCRecordHandler.this.idField+", "+JDBCRecordHandler.this.dataField+" from "+JDBCRecordHandler.this.table);
		}
		
		@SuppressWarnings("synthetic-access")
		public boolean hasNext() {
			try {
				return this.rs.next();
			} catch(SQLException e) {
				log.error("Unable to retrieve next record",e);
				return false;
			}
		}
		
		public Record next() {
			try {
				return new Record(this.rs.getString(1),new String(this.rs.getBytes(2)));
			} catch(SQLException e) {
				throw new NoSuchElementException(e.getMessage());
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public void setParams(Map<String, String> params) throws IllegalArgumentException, IOException {
		String jdbcDriverClass = getParam(params,"jdbcDriverClass",true);
		String connLine = getParam(params,"connLine",false);
		String connType = getParam(params,"connType",false);
		String host = getParam(params,"host",false);
		String port = getParam(params,"port",false);
		String dbName = getParam(params,"dbName",false);
		String username = getParam(params,"username",true);
		String password = getParam(params,"password",true);
		String tableName = getParam(params,"tableName",true);
		String idFieldName = getParam(params,"idFieldName",true);
		String dataFieldName = getParam(params,"dataFieldName",true);
		boolean has4part = !(connType == null || host == null || port == null || dbName == null);
		if(connLine == null) {
			if(!has4part) {
				throw new IllegalArgumentException("Must have either connLine OR connType, host, port, and dbName");
			}
			initAll(jdbcDriverClass, buildConnLine(connType, host, port, dbName), username, password, tableName, idFieldName, dataFieldName);
		} else {
			if(has4part) {
				throw new IllegalArgumentException("Must have either connLine OR connType, host, port, and dbName, not both");
			}
			initAll(jdbcDriverClass, connLine, username, password, tableName, idFieldName, dataFieldName);
		}
	}
	
}
