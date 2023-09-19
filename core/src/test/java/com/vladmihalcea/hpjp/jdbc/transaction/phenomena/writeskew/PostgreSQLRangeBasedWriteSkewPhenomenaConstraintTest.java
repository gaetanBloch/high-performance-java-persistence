package com.vladmihalcea.hpjp.jdbc.transaction.phenomena.writeskew;

import com.vladmihalcea.hpjp.util.exception.ExceptionUtil;
import com.vladmihalcea.hpjp.util.providers.Database;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class PostgreSQLRangeBasedWriteSkewPhenomenaConstraintTest extends AbstractDepartmentEmployeePhenomenaTest {

    public PostgreSQLRangeBasedWriteSkewPhenomenaConstraintTest(String isolationLevelName, int isolationLevel) {
        super(isolationLevelName, isolationLevel);
    }

    @Override
    protected Database database() {
        return Database.POSTGRESQL;
    }

    @Override
    public void afterInit() {
        super.afterInit();

        doInJDBC(connection -> {
            executeStatement("DROP TRIGGER IF EXISTS check_department_budget_trigger ON employee;");
            executeStatement("DROP FUNCTION check_department_budget();");

            executeStatement("CREATE OR REPLACE FUNCTION check_department_budget() RETURNS trigger AS $$ " +
                             "DECLARE " +
                             "    allowed_budget BIGINT; " +
                             "    new_budget BIGINT; " +
                             "BEGIN " +
                             "    SELECT INTO allowed_budget budget FROM department where id = NEW.department_id; " +
                             "    SELECT INTO new_budget SUM(salary) FROM employee where department_id = NEW.department_id; " +
                             "    IF new_budget > allowed_budget THEN " +
                             "        RAISE EXCEPTION 'Overbudget department [id:%] by [%]', " +
                             "                        NEW.department_id, " +
                             "                        (new_budget - allowed_budget);" +
                             "    END IF; " +
                             "   RETURN NEW;" +
                             "END; " +
                             "$$ LANGUAGE plpgsql;"
            );

            executeStatement("CREATE TRIGGER check_department_budget_trigger " +
                             "AFTER INSERT OR UPDATE ON employee " +
                             "FOR EACH ROW EXECUTE PROCEDURE check_department_budget();"
            );
        });
    }

    @Override
    public void destroy() {
        executeStatement("DROP TRIGGER IF EXISTS check_department_budget_trigger ON employee;");
        executeStatement("DROP FUNCTION check_department_budget();");
        super.destroy();
    }

    @Test
    public void testWriteSkewCheckConstraintInsertUpdate() {
        final AtomicBoolean preventedByLocking = new AtomicBoolean();
        final AtomicBoolean preventedByMVCC = new AtomicBoolean();
        final AtomicBoolean preventedByConstraint = new AtomicBoolean();

        try {
            doInJDBC(aliceConnection -> {
                if (!aliceConnection.getMetaData().supportsTransactionIsolationLevel(isolationLevel)) {
                    LOGGER.info("Database {} doesn't support {}", dataSourceProvider().database(), isolationLevelName);
                    return;
                }
                prepareConnection(aliceConnection);

                try {
                    executeSync(() -> {
                        doInJDBC(bobConnection -> {
                            prepareConnection(bobConnection);
                            try {
                                try (
                                    PreparedStatement employeeStatement = bobConnection.prepareStatement(insertEmployeeSql());
                                ) {
                                    int employeeId = 4;
                                    int index = 0;
                                    employeeStatement.setLong(++index, 1);
                                    employeeStatement.setString(++index, "Dave");
                                    employeeStatement.setLong(++index, 9_000);
                                    employeeStatement.setLong(++index, employeeId);
                                    employeeStatement.executeUpdate();
                                }
                            } catch (Exception e) {
                                if (ExceptionUtil.isLockTimeout(e)) {
                                    preventedByLocking.set(true);
                                } else if (ExceptionUtil.isMVCCAnomalyDetection(e)) {
                                    preventedByMVCC.set(true);
                                } else {
                                    throw new IllegalStateException(e);
                                }
                            }
                        });
                    });
                } catch (Exception e) {
                    if (ExceptionUtil.isLockTimeout(e)) {
                        preventedByLocking.set(true);
                    } else if (ExceptionUtil.isMVCCAnomalyDetection(e)) {
                        preventedByMVCC.set(true);
                    } else {
                        throw new IllegalStateException(e);
                    }
                }
                update(aliceConnection, updateEmployeeSalarySql());
            });
        } catch (Exception e) {
            if (ExceptionUtil.isLockTimeout(e)) {
                preventedByLocking.set(true);
            } else if (ExceptionUtil.isMVCCAnomalyDetection(e)) {
                preventedByMVCC.set(true);
            } else if (isCheckConstraintException(e)) {
                preventedByConstraint.set(true);
            } else {
                throw new IllegalStateException(e);
            }
        }
        doInJDBC(aliceConnection -> {
            long salaryCount = selectColumn(aliceConnection, sumEmployeeSalarySql(), Number.class, Duration.ofSeconds(1)).longValue();
            if (99_000 != salaryCount) {
                LOGGER.info("Isolation level {} allows overbudgeting since the salary count is {} instead of 99000", isolationLevelName, salaryCount);
            } else {
                LOGGER.info(
                    "Isolation level {} prevents overbudgeting due to {}",
                    isolationLevelName,
                    preventedByLocking.get() ?
                        "locking" :
                    preventedByMVCC.get() ?
                        "MVCC" :
                    preventedByConstraint.get() ?
                        "check constraint" :
                        "unknown"
                );
            }
        });
    }

    @Test
    public void testWriteSkewCheckConstraintSelectInsertUpdate() {
        final AtomicBoolean preventedByLocking = new AtomicBoolean();
        final AtomicBoolean preventedByMVCC = new AtomicBoolean();
        final AtomicBoolean preventedByConstraint = new AtomicBoolean();

        try {
            doInJDBC(aliceConnection -> {
                if (!aliceConnection.getMetaData().supportsTransactionIsolationLevel(isolationLevel)) {
                    LOGGER.info("Database {} doesn't support {}", dataSourceProvider().database(), isolationLevelName);
                    return;
                }
                prepareConnection(aliceConnection);
                long salaryCount = selectColumn(aliceConnection, sumEmployeeSalarySql(), Number.class, Duration.ofSeconds(1)).longValue();
                assertEquals(90_000, salaryCount);

                try {
                    executeSync(() -> {
                        doInJDBC(bobConnection -> {
                            prepareConnection(bobConnection);
                            try {
                                try (
                                    PreparedStatement employeeStatement = bobConnection.prepareStatement(insertEmployeeSql());
                                ) {
                                    int employeeId = 4;
                                    int index = 0;
                                    employeeStatement.setLong(++index, 1);
                                    employeeStatement.setString(++index, "Dave");
                                    employeeStatement.setLong(++index, 9_000);
                                    employeeStatement.setLong(++index, employeeId);
                                    employeeStatement.executeUpdate();
                                }
                            } catch (Exception e) {
                                if (ExceptionUtil.isLockTimeout(e)) {
                                    preventedByLocking.set(true);
                                } else if (ExceptionUtil.isMVCCAnomalyDetection(e)) {
                                    preventedByMVCC.set(true);
                                } else {
                                    throw new IllegalStateException(e);
                                }
                            }
                        });
                    });
                } catch (Exception e) {
                    if (ExceptionUtil.isLockTimeout(e)) {
                        preventedByLocking.set(true);
                    } else if (ExceptionUtil.isMVCCAnomalyDetection(e)) {
                        preventedByMVCC.set(true);
                    } else {
                        throw new IllegalStateException(e);
                    }
                }
                update(aliceConnection, updateEmployeeSalarySql());
            });
        } catch (Exception e) {
            if (ExceptionUtil.isLockTimeout(e)) {
                preventedByLocking.set(true);
            } else if (ExceptionUtil.isMVCCAnomalyDetection(e)) {
                preventedByMVCC.set(true);
            } else if (isCheckConstraintException(e)) {
                preventedByConstraint.set(true);
            } else {
                throw new IllegalStateException(e);
            }
        }
        doInJDBC(aliceConnection -> {
            long salaryCount = selectColumn(aliceConnection, sumEmployeeSalarySql(), Number.class, Duration.ofSeconds(1)).longValue();
            if (99_000 != salaryCount) {
                LOGGER.info("Isolation level {} allows overbudgeting since the salary count is {} instead of 99000", isolationLevelName, salaryCount);
            } else {
                LOGGER.info(
                    "Isolation level {} prevents overbudgeting due to {}",
                    isolationLevelName,
                    preventedByLocking.get() ?
                        "locking" :
                    preventedByMVCC.get() ?
                        "MVCC" :
                    preventedByConstraint.get() ?
                        "check constraint" :
                        "unknown"
                );
            }
        });
    }

    private boolean isCheckConstraintException(Exception e) {
        boolean constraintViolation = ExceptionUtil.rootCause(e).getMessage().contains("Overbudget");
        if(constraintViolation) {
            LOGGER.error("Constraint violation", e);
        }
        return constraintViolation;
    }
}
