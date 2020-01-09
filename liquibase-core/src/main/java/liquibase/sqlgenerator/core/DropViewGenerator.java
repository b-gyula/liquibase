package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropViewStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.View;
import static liquibase.database.Database.ObjectType.*;

public class DropViewGenerator extends AbstractSqlGenerator<DropViewStatement> {

    @Override
    public ValidationErrors validate(DropViewStatement dropViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", dropViewStatement.getViewName());
        if(!database.supportsDropIfExists().contains(VIEW)) {
            validationErrors.checkDisallowedField("ifExists", "", database);
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
            new UnparsedSql("DROP VIEW "
                    + (statement.ifExists() && database.supportsDropIfExists().contains(TABLE) ?
                    "IF EXISTS ":"")
                 + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName()
                    , statement.getViewName()), getAffectedView(statement))
        };
    }

    protected Relation getAffectedView(DropViewStatement statement) {
        return new View().setName(statement.getViewName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
