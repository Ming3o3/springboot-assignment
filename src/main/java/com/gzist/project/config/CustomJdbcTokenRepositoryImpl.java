package com.gzist.project.config;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;

/**
 * 自定义持久化Token仓库实现
 * 适配使用自增id作为主键的persistent_logins表结构
 *
 * @author GZIST
 * @since 2025-12-25
 */
public class CustomJdbcTokenRepositoryImpl implements PersistentTokenRepository {

    /** 创建表的SQL（可选） */
    public static final String CREATE_TABLE_SQL = 
        "CREATE TABLE IF NOT EXISTS persistent_logins (" +
        "id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID（无业务意义）', " +
        "username VARCHAR(64) NOT NULL COMMENT '用户名', " +
        "series VARCHAR(64) NOT NULL COMMENT '序列号', " +
        "token VARCHAR(64) NOT NULL COMMENT '令牌值', " +
        "last_used TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后使用时间', " +
        "PRIMARY KEY (id), " +
        "UNIQUE KEY uk_series (series), " +
        "KEY idx_username (username)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    /** 插入新token的SQL */
    public static final String INSERT_TOKEN_SQL = 
        "INSERT INTO persistent_logins (username, series, token, last_used) VALUES (?, ?, ?, ?)";

    /** 根据series更新token的SQL */
    public static final String UPDATE_TOKEN_SQL = 
        "UPDATE persistent_logins SET token = ?, last_used = ? WHERE series = ?";

    /** 根据series查询token的SQL */
    public static final String LOAD_TOKEN_SQL = 
        "SELECT username, series, token, last_used FROM persistent_logins WHERE series = ?";

    /** 根据username删除token的SQL */
    public static final String REMOVE_USER_TOKENS_SQL = 
        "DELETE FROM persistent_logins WHERE username = ?";

    private JdbcTemplate jdbcTemplate;

    private boolean createTableOnStartup = false;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setCreateTableOnStartup(boolean createTableOnStartup) {
        this.createTableOnStartup = createTableOnStartup;
    }

    /**
     * 初始化方法，可选择是否创建表
     */
    public void init() {
        if (createTableOnStartup) {
            jdbcTemplate.execute(CREATE_TABLE_SQL);
        }
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        jdbcTemplate.update(INSERT_TOKEN_SQL, 
            token.getUsername(), 
            token.getSeries(), 
            token.getTokenValue(),
            new Date(token.getDate().getTime()));
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        jdbcTemplate.update(UPDATE_TOKEN_SQL, tokenValue, lastUsed, series);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        try {
            return jdbcTemplate.queryForObject(LOAD_TOKEN_SQL,
                (rs, rowNum) -> new PersistentRememberMeToken(
                    rs.getString("username"),
                    rs.getString("series"),
                    rs.getString("token"),
                    rs.getTimestamp("last_used")),
                seriesId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void removeUserTokens(String username) {
        jdbcTemplate.update(REMOVE_USER_TOKENS_SQL, username);
    }
}
