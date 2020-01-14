package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.Warnings;
import liquibase.util.BooleanUtils;

import liquibase.database.Database.ObjectType;

public interface DropIfExists {

    Boolean getIfExists();

    default Warnings warn(Database database, ObjectType objectType) {
        Warnings warns = new Warnings();
        if(BooleanUtils.isTrue(getIfExists()) && !database.supportsDropIfExists().contains(objectType)) {
            warns.addWarning("'ifExists' not supported for dropView on " + database.fullName());
        }
        return warns;
    }
}
