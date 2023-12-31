package com.vladmihalcea.hpjp.hibernate.listener;

import java.util.Date;

/**
 * @author Vlad Mihalcea
 */
public interface Updatable {

    void setTimestamp(Date timestamp);

    Date getTimestamp();
}
