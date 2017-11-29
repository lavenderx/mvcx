package io.quasar.core.ext;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * https://vertxchina.github.io/vertx-translation-chinese/data/MySQL&PostgreSQLClient.html
 *
 * @see HikariCPDataSourceProvider
 */
public class HikariCPDataSourceFactory extends UnpooledDataSourceFactory {

    public HikariCPDataSourceFactory() {
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/test");
        config.setUsername("postgres");
        config.setPassword("12345678");
        config.setMaximumPoolSize(20);
        config.setIdleTimeout(60_000);
        config.setMinimumIdle(2);
        config.setAutoCommit(true);
        config.setSchema("postgres");

        this.dataSource = new HikariDataSource(config);
    }
}
