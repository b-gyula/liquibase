package liquibase.statement.core;

import liquibase.statement.ReturningSqlStatement;

public class RawSqlStatement extends ReturningSqlStatement {

    private String sql;
    private String endDelimiter  = ";";


    public RawSqlStatement(String sql) {
        this.sql = sql;
    }

    public RawSqlStatement(String sql, String endDelimiter) {
        this(sql);
        if (endDelimiter != null) {
            this.endDelimiter = endDelimiter;
        }
    }

    public RawSqlStatement(String sql, String endDelimiter, String resultIn) {
        this(sql, endDelimiter);
        this.resultIn = resultIn;
    }

    public String getSql() {
        return sql;
    }

    public String getEndDelimiter() {
        return endDelimiter.replace("\\r","\r").replace("\\n","\n");
    }

    @Override
    public String toString() {
        return sql;
    }
}
