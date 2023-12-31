package com.vladmihalcea.hpjp.jdbc.batch.generatedkeys.sequence;

import com.vladmihalcea.hpjp.util.providers.DataSourceProvider;
import com.vladmihalcea.hpjp.util.providers.PostgreSQLDataSourceProvider;

/**
 * PostgreSQLSequenceCallTest - PostgreSQL sequence call
 *
 * @author Vlad Mihalcea
 */
public class PostgreSQLSequenceCallTest extends AbstractSequenceCallTest {

    @Override
    protected String callSequenceSyntax() {
        return "select nextval('post_seq')";
    }

    @Override
    protected DataSourceProvider dataSourceProvider() {
        return new PostgreSQLDataSourceProvider();
    }
}
