package liquibase.statement;

/** Statements that can return (a single) value */
public abstract class ReturningSqlStatement extends AbstractSqlStatement {
    protected String resultIn;
    /** name of the property the result should be stored */
    public String getResultIn() {
        return resultIn;
    }
}
