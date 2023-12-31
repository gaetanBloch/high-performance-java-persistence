package com.vladmihalcea.hpjp.jdbc.batch.generatedkeys.sequence;

import com.vladmihalcea.hpjp.util.providers.DataSourceProvider;
import com.vladmihalcea.hpjp.util.providers.OracleDataSourceProvider;

/**
 * OracleSequenceGeneratedKeysBatchPreparedStatementTest - Oracle class for testing JDBC PreparedStatement generated keys for Sequences
 *
 * @author Vlad Mihalcea
 */
public class OracleSequenceGeneratedKeysBatchPreparedStatementTest extends AbstractSequenceGeneratedKeysBatchPreparedStatementTest {

    @Override
    protected String callSequenceSyntax() {
        return "select post_seq.NEXTVAL from dual";
    }

    @Override
    protected DataSourceProvider dataSourceProvider() {
        return new OracleDataSourceProvider();
    }
}
