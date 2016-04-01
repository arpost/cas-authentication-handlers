package org.eurekaclinical.cas.authentication.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.dao.DataRetrievalFailureException;

/**
 *
 * @author Andrew Post
 */
public final class BuiltInAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private String sql;
    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        SQLStringGenerator sqlGen = new SQLStringGenerator();
        sqlGen.setDataSource(getDataSource());
        try {
            this.sql = sqlGen.getUserVerificationSQLString();
        } catch (SQLException sqle) {
            throw new DataRetrievalFailureException("Could not retrieve database metadata", sqle);
        }
    }

    @Override
    protected boolean authenticateUsernamePasswordInternal(UsernamePasswordCredentials credentials) throws AuthenticationException {
        if (dataSource == null) {
            throw new IllegalArgumentException("No data source found!");
        }
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(this.sql)) {
            String username = getPrincipalNameTransformer().transform(credentials.getUsername());
            String password = credentials.getPassword();
            String encryptedPassword = getPasswordEncoder().encode(password);
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString(1);
                    return dbPassword.equals(encryptedPassword);
                } else {
                    return false;
                }
            }
        } catch (SQLException sqle) {
            throw new DataRetrievalFailureException("Error retrieving user account information", sqle);
        }
    }

}
