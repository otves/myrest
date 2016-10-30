package hello.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Блок для хранения параметров JDBC-соединения.
 * @author dev01
 */
public class ConfigJdbc {

    /**
     * Класс для JDBC-подключения.
     */
    private String driver;
    /**
     * Ссылка с параметрами подключения.
     */
    private String url;
    /**
     * Имя пользователя.
     */
    private String username;
    /**
     * Пароль.
     */
    private String password;

    /**
     * Конструктор.
     */
    public ConfigJdbc() {
    }

    /**
     * Конструктор.
     * @param driver Класс с JDBC-драйвером.
     * @param url Ссылка с параметрами подключения.
     * @param username Имя пользователя.
     * @param password Пароль.
     */
    public ConfigJdbc(
            final Class driver,
            final String url,
            final String username,
            final String password) {
        this.driver = driver.getName();
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Класс для JDBC-подключения.
     * @return Класс для JDBC-подключения.
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Класс для JDBC-подключения.
     * @param driver Класс для JDBC-подключения.
     */
    public void setDriver(final String driver) {
        this.driver = driver;
    }

    /**
     * Ссылка с параметрами подключения.
     * @return Ссылка с параметрами подключения.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Ссылка с параметрами подключения.
     * @param url Ссылка с параметрами подключения.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Имя пользователя.
     * @return Имя пользователя.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Имя пользователя.
     * @param username Имя пользователя.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Пароль.
     * @return Пароль.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Пароль.
     * @param password Пароль.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Получение JDBC-подключения.
     * @return JDBC-подключение.
     * @throws SQLException Указаны не верные параметры подключения.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            if (Class.forName(driver) != null) {
                conn = DriverManager.getConnection(url, username, password);
            }
        } catch (ClassNotFoundException ex) {
            throw new SQLException(ex);
        }
        return conn;
    }
}
