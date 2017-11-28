package guda.mvcx.core.ext;

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
        this.dataSource = new HikariDataSource();
    }
}
