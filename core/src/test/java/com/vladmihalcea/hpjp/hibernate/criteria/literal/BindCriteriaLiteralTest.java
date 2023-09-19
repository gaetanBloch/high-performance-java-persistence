package com.vladmihalcea.hpjp.hibernate.criteria.literal;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.query.criteria.ValueHandlingMode;

import java.util.Properties;

/**
 * @author Vlad Mihalcea
 */
public class BindCriteriaLiteralTest extends DefaultCriteriaLiteralTest {

    @Override
    protected void additionalProperties(Properties properties) {
        properties.put(AvailableSettings.CRITERIA_VALUE_HANDLING_MODE, ValueHandlingMode.BIND);
    }
}
