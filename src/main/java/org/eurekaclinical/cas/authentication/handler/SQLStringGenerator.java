package org.eurekaclinical.cas.authentication.handler;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import org.arp.javautil.sql.DatabaseProduct;


/**
 *
 * @author Miao Ai
 */
class SQLStringGenerator {

    private DataSource ds;

    public DataSource getDataSource() {
        return ds;
    }

    public void setDataSource(DataSource ds) {
        this.ds = ds;
    }
    
    String getUserVerificationSQLString() throws SQLException {
        String result;
        try (Connection connection = ds.getConnection()) {
            if (DatabaseProduct.fromMetaData(connection.getMetaData()) == DatabaseProduct.POSTGRESQL) {
                result = "select a2.password from users a1 join local_users a2 on (a1.id=a2.id) where a1.username=? and a1.active='true'";
            } else {
                result = "select a2.password from users a1 join local_users a2 on (a1.id=a2.id) where a1.username=? and a1.active=1";
            }
        }

        return result;
    }
}
