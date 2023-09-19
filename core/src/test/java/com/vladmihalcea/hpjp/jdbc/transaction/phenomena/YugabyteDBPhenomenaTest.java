package com.vladmihalcea.hpjp.jdbc.transaction.phenomena;

import com.vladmihalcea.hpjp.util.providers.DataSourceProvider;
import com.vladmihalcea.hpjp.util.providers.YugabyteDBDataSourceProvider;

/**
 * YugabyteDBPhenomenaTest - Test to validate YugabyteDB phenomena
 *
 * @author Vlad Mihalcea
 */
public class YugabyteDBPhenomenaTest extends AbstractPhenomenaTest {

    public YugabyteDBPhenomenaTest(String isolationLevelName, int isolationLevel) {
        super(isolationLevelName, isolationLevel);
    }

    @Override
    protected DataSourceProvider dataSourceProvider() {
        return new YugabyteDBDataSourceProvider();
    }
}
