package com.vladmihalcea.hpjp.util;

import com.vladmihalcea.hpjp.util.providers.Database;

/**
 * AbstractPostgreSQLIntegrationTest - Abstract PostgreSQL IntegrationTest
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractPostgreSQLIntegrationTest extends AbstractTest {

    @Override
    protected Database database() {
        return Database.POSTGRESQL;
    }
}
