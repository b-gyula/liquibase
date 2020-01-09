package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropTableStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private boolean cascadeConstraints;
    private boolean ifExists;

    public DropTableStatement(String catalogName, String schemaName, String tableName,  boolean cascadeConstraints) {
        this(catalogName, schemaName, tableName, cascadeConstraints, true);
    }
    public DropTableStatement(String catalogName, String schemaName, String tableName,
                              boolean cascadeConstraints, boolean ifExists) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.cascadeConstraints = cascadeConstraints;
        this.ifExists = ifExists;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isCascadeConstraints() {
        return cascadeConstraints;
    }

    public boolean ifExists() {
        return ifExists;
    }
}
