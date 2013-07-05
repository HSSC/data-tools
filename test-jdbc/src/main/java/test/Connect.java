package test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Connect {
	
	private static final String PROP_URL = "url";
	private static final String PROP_USER = "user";
	private static final String PROP_PASS = "password";

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		Properties props = getProperties();
		Connection connection = getConnection(props);
		selectDate(connection);
		System.out.println("Connect and select successful.");
	}
	
	private static void selectDate(Connection connection){
		Statement statement = null;
		ResultSet resultset = null;
		try {
			statement = connection.createStatement();
			resultset = statement.executeQuery("SELECT CURRENT_TIMESTAMP FROM DUAL");
		}
		catch (SQLException e) {
			logError("", e);
		}
		finally{
			try{resultset.close();}catch(Exception e){}
			try{statement.close();}catch(Exception e){}
			try{connection.close();}catch(Exception e){}
		}
	}
	
	private static Connection getConnection(Properties props){
		String url = props.getProperty(PROP_URL);
		String user = props.getProperty(PROP_USER);
		String password = props.getProperty(PROP_PASS);
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, user, password);
		}
		catch (SQLException e) {
			logError("FIXIT: Failed to connect to the database using the provided properties.", e);
		}
		return connection;
	}
	
	private static Properties getProperties(){
		// Get properties
		Properties props = new Properties();
		InputStream stream = Connect.class.getResourceAsStream("/connect.props");
		if(stream == null){
			logError("FIXIT: There is no connect.props file at the root of the classpath.");
		}
		
		try {
			props.load(stream);
		}
		catch (IOException e) {
			logError("FIXIT: Failed to load connect.props file at the root of the classpath.  Is it a properties file?", e);
		}
		validateProp(props, PROP_URL);
		validateProp(props, PROP_USER);
		validateProp(props, PROP_PASS);
		return props;
	}
	
	public static void validateProp(Properties props, String key){
		String property = props.getProperty(key);
		if(property != null){
			property = property.trim();
			if(property.length() > 0){
				System.out.println("Property: " + key + "=[" + property + "]");
				return;
			}
		}
		logError(new StringBuilder("Property '").append(key).append("' is either missing or emtpy."));
	}
	
	public static void logError(Object message){
		System.out.println(message.toString());
		throw new RuntimeException(message.toString());
	}
	
	public static void logError(Object message, Exception e){
		System.out.println(message.toString());
		throw new RuntimeException(message.toString(), e);
	}
}
