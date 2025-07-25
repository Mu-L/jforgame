package jforgame.demo.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jforgame.orm.core.OrmTemplate;
import jforgame.orm.entity.StatefulEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 使用Hikari数据源对orm-DbUtils的进一步封装
 */
public class DbUtils {

    private static Logger logger = LoggerFactory.getLogger(DbUtils.class);

    /**
     * 策划数据库
     */
    public static final String DB_DATA = "config";
    /**
     * 玩家数据库
     */
    public static final String DB_USER = "user";


    private static OrmTemplate configOrmTemplate;

    private static OrmTemplate userOrmTemplate;

    public static void init() throws Exception {
        Properties props = new Properties();
        props.load(new FileReader("configs/jdbc.properties"));
        HikariDataSource configDataSource = createDataSource(props, DB_DATA);
        HikariDataSource userDataSource = createDataSource(props, DB_USER);
        configOrmTemplate = new OrmTemplate(configDataSource);
        userOrmTemplate = new OrmTemplate(userDataSource);
    }

    private static HikariDataSource createDataSource(Properties props, String db) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty(db + ".dataSource.jdbc"));
        config.setUsername(props.getProperty(db + ".dataSource.user"));
        config.setPassword(props.getProperty(db + ".dataSource.password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }


    /**
     * 查询返回一个bean实体
     */
    public static <T> T queryOne(String alias, String sql, Class<?> entity, String id) throws SQLException {
        OrmTemplate OrmTemplate = getConnection(alias);
        return OrmTemplate.queryOne(sql, entity, id);
    }


    /**
     * 查询返回bean实体列表
     *
     * @param alias  数据库别名
     * @param sql
     * @param entity
     * @return
     */
    public static <T> List<T> queryMany(String alias, String sql, Class<?> entity) throws SQLException {
        OrmTemplate OrmTemplate = getConnection(alias);
        return OrmTemplate.queryMany(sql, entity);
    }

    /**
     * 查询返回一个map
     *
     * @param alias 数据库别名
     * @param sql
     * @return
     */
    public static Map<String, Object> queryMap(String alias, String sql) throws SQLException {
        OrmTemplate OrmTemplate = getConnection(alias);
        return OrmTemplate.queryMap(sql);
    }

    /**
     * 查询返回一个map
     *
     * @param alias 数据库别名
     * @param sql
     * @return
     */
    public static List<Map<String, Object>> queryMapList(String alias, String sql) throws SQLException {
        OrmTemplate OrmTemplate = getConnection(alias);
        return OrmTemplate.queryMapList(sql);
    }

    /**
     * 执行特定的sql语句(只有db库有执行权限)
     *
     * @param sql
     * @return
     */
    public static int executeUpdate(String sql) throws SQLException {
        OrmTemplate OrmTemplate = getConnection(DB_USER);
        return OrmTemplate.executeUpdate(sql);
    }

    public static int executePreparedUpdate(StatefulEntity entity) throws SQLException {
        OrmTemplate OrmTemplate = getConnection(DB_USER);
        return OrmTemplate.executeUpdate(entity);
    }

    public static int executeDelete(StatefulEntity entity) throws SQLException {
        OrmTemplate OrmTemplate = getConnection(DB_USER);
        return OrmTemplate.executeDelete(entity);
    }

    public static int executePreparedInsert(StatefulEntity entity) throws SQLException {
        OrmTemplate OrmTemplate = getConnection(DB_USER);
        return OrmTemplate.executeInsert(entity);
    }

    public static OrmTemplate getConnection(String alias) {
        if (DB_DATA.equals(alias)) {
            return configOrmTemplate;
        } else if (DB_USER.equals(alias)) {
            return userOrmTemplate;
        } else {
            throw new RuntimeException("数据库别名不存在");
        }
    }

}
