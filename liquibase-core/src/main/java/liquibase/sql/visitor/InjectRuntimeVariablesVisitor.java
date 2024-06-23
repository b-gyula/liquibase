package liquibase.sql.visitor;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;

public class InjectRuntimeVariablesVisitor extends AbstractSqlVisitor {
	public static String expandExpressions(DatabaseChangeLog changeLog, String sql) {
		return changeLog.getChangeLogParameters().expandExpressions(sql, changeLog);
	}

	final DatabaseChangeLog changeLog;
	public InjectRuntimeVariablesVisitor(final DatabaseChangeLog changeLogParams) {
		this.changeLog = changeLogParams;
	}
	@Override
	public String modifySql(String sql, Database database) {
		return expandExpressions(changeLog, sql);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

}
