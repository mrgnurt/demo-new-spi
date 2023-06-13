package org.example;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DbUtil {
    public static final String CONFIG_KEY_JDBC_DRIVER = "org.postgresql.Driver";
    public static final String CONFIG_KEY_JDBC_URL = "jdbc:postgresql://10.0.65.141:5432/corp_customer";
    public static final String CONFIG_KEY_DB_USERNAME = "ibadmin";
    public static final String CONFIG_KEY_DB_PASSWORD = "K0copas";
    public static final String CONFIG_KEY_VALIDATION_QUERY = "validationQuery";

    public static Connection getConnection(ComponentModel config) throws SQLException{
        String driverClass = CONFIG_KEY_JDBC_DRIVER;
        log.info("getConnection" + driverClass);
        try {
            Class.forName(driverClass);
        }
        catch(ClassNotFoundException nfe) {
            throw new RuntimeException("Invalid JDBC driver: " + driverClass + ". Please check if your driver if properly installed");
        }
        
        return DriverManager.getConnection(CONFIG_KEY_JDBC_URL,
          CONFIG_KEY_DB_USERNAME,
          CONFIG_KEY_DB_PASSWORD);
    }
}
