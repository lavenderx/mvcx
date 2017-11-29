package io.quasar.core.ext;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class HikariDataSourceProvider implements Provider<DataSource> {

    private final HikariDataSource dataSource = new HikariDataSource();

    @Inject
    public void setDriverClassName(@Named("JDBC.driverClassName") final String driverClassName) {
        dataSource.setDriverClassName(driverClassName);
    }

    @Inject
    public void setUrl(@Named("JDBC.url") final String url) {
        dataSource.setJdbcUrl(url);
    }

    @Inject
    public void setUsername(@Named("JDBC.username") final String username) {
        dataSource.setUsername(username);
    }

    @Inject
    public void setPassword(@Named("JDBC.password") final String password) {
        dataSource.setPassword(password);
    }

    @Inject
    public void setMaximumPoolSize(@Named("JDBC.maxPoolSize") final int maxPoolSize) {
        dataSource.setMaximumPoolSize(maxPoolSize);
    }

    @Inject
    public void setIdleTimeout(@Named("JDBC.idleTimeoutMs") final long idleTimeoutMs) {
        dataSource.setIdleTimeout(idleTimeoutMs);
    }

    @Inject
    public void setMinimumIdle(@Named("JDBC.minIdle") final int minIdle) {
        dataSource.setMinimumIdle(minIdle);
    }

    @Inject
    public void setAutoCommit(@Named("JDBC.autoCommit") final boolean isAutoCommit) {
        dataSource.setAutoCommit(isAutoCommit);
    }

    @Inject
    public void setSchema(@Named("JDBC.schema") final String schema) {
        dataSource.setSchema(schema);
    }

    @Override
    public DataSource get() {
        return dataSource;
    }
}
