package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.core.View;

public class GetViewDefinitionGenerator extends AbstractSqlGenerator<GetViewDefinitionStatement> {

    @Override
    public ValidationErrors validate(GetViewDefinitionStatement getViewDefinitionStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", getViewDefinitionStatement.getViewName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);

        String sql = "select VIEW_DEFINITION from INFORMATION_SCHEMA.VIEWS where TABLE_NAME='" + database.correctObjectName(statement.getViewName(), View.class) + "'";

        if (database instanceof MySQLDatabase) {
            sql += " and TABLE_SCHEMA='" + schema.getCatalogName() + "'";
        } else {

            if (database.supportsSchemas()) {
                String schemaName = schema.getSchemaName();
                if (schemaName != null) {
                	sql += " and TABLE_SCHEMA='" + schemaName + "'";
                }
            }

            if (database.supportsCatalogs()) {
                String catalogName = schema.getCatalogName();
                if (catalogName != null) {
               		sql += " and TABLE_CATALOG='" + catalogName + "'";
                }
            }
        }

        return new Sql[]{
           new UnparsedSql(sql)
        };
    }
}
