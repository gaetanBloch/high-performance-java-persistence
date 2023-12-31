package com.vladmihalcea.hpjp.jdbc.batch.generatedkeys.sequence;

import com.vladmihalcea.hpjp.util.providers.DataSourceProvider;
import com.vladmihalcea.hpjp.util.providers.SQLServerDataSourceProvider;

/**
 * PostgreSQLSequenceCallTest - PostgreSQL sequence call
 *
 * @author Vlad Mihalcea
 */
public class SQLServerSequenceCallTest extends AbstractSequenceCallTest {

    @Override
    protected String callSequenceSyntax() {
        return "select NEXT VALUE FOR post_seq";
    }

    @Override
    protected DataSourceProvider dataSourceProvider() {
        return new SQLServerDataSourceProvider();
    }
}
