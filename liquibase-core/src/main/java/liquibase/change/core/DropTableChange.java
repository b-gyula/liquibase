package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.Warnings;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.Table;
import liquibase.util.BooleanUtils;

import static liquibase.change.ChangeParameterMetaData.ALL;
import static liquibase.database.Database.ObjectType.TABLE;

/**
 * Drops an existing table.
 */
@DatabaseChange(name="dropTable", description = "Drops an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DropTableChange extends AbstractChange implements DropIfExists {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private Boolean cascadeConstraints;
    private Boolean ifExists;

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", description = "Name of the table to drop"
            , requiredForDatabase = ALL)
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(description = "Add the `CASCADE CONSTRAINTS` to the statement")
    public Boolean isCascadeConstraints() { return cascadeConstraints; }

    public void setCascadeConstraints(Boolean cascadeConstraints) {
        this.cascadeConstraints = cascadeConstraints;
    }

    @DatabaseChangeProperty( since = "3.9"
            , description = "whether the existence of the view shall be checked before its drop to avoid error." +
            " Default: true if the DB supports it")
    public Boolean getIfExists() { return ifExists; }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @Override
    public Warnings warn(Database database) {
        return warn(database, TABLE);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        boolean constraints = false;
        if (isCascadeConstraints() != null) {
            constraints = isCascadeConstraints();
        }
        
        return new SqlStatement[]{
            new DropTableStatement(getCatalogName(), getSchemaName(), getTableName(), constraints,
                    (getIfExists() == null ? true : getIfExists()) && database.supportsDropIfExists().contains(TABLE))
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Table(getCatalogName(), getSchemaName(), getTableName()), database), "Table exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "Table " + getTableName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
