package liquibase.change.core

import liquibase.Scope
import liquibase.change.ChangeStatus
import liquibase.database.core.PostgresDatabase
import liquibase.database.DatabaseConnection
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.SqlStatement
import liquibase.statement.core.InsertOrUpdateStatement
import liquibase.database.core.MSSQLDatabase

import static liquibase.ChecksumVersion.V8
import static liquibase.ChecksumVersion.V9

class LoadUpdateDataChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("FILE_NAME");

        then:
        "Data loaded from 'FILE_NAME' into table 'TABLE_NAME'" == refactoring.getConfirmationMessage()
    }

	def "loadUpdateEmpty database agnostic"() throws Exception {
		when:
		LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
		refactoring.setSchemaName("SCHEMA_NAME");
		refactoring.setTableName("TABLE_NAME");
		refactoring.setFile("liquibase/change/core/empty.data.csv");
		refactoring.setSeparator(",");

		SqlStatement[] sqlStatement = refactoring.generateRollbackStatements(new MSSQLDatabase());
		
		then:
		sqlStatement.length == 0
	}

    def "loadUpdate generates InsertOrUpdateStatements"() throws Exception {
        when:
        MockDatabase database = new MockDatabase();
        database.setConnection((DatabaseConnection) null)

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");

        SqlStatement[] statements = change.generateStatements(database);

        then:
        assert statements != null
        assert statements[0] instanceof InsertOrUpdateStatement
        assert !statements[0].getOnlyUpdate()
    }

    def "loadUpdate generates InsertOrUpdateStatements for Postgres"() throws Exception {
        when:
        PostgresDatabase database = new PostgresDatabase();

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/jhi_text.csv");
        change.setResourceAccessor(new ClassLoaderResourceAccessor());

        LoadDataColumnConfig idConfig = new LoadDataColumnConfig();
        idConfig.setHeader("id");
        idConfig.setType("NUMERIC");
        change.addColumn(idConfig);

        LoadDataColumnConfig pickupConfig = new LoadDataColumnConfig();
        pickupConfig.setHeader("selected_pickup_date");
        pickupConfig.setType("DATETIME");
        change.addColumn(pickupConfig);

        LoadDataColumnConfig effectiveConfig = new LoadDataColumnConfig();
        effectiveConfig.setHeader("effective_pickup_date");
        effectiveConfig.setType("DATETIME");
        change.addColumn(effectiveConfig);

        LoadDataColumnConfig textConfig = new LoadDataColumnConfig();
        textConfig.setHeader("textfield");
        textConfig.setType("CLOB");
        change.addColumn(textConfig);

        SqlStatement[] statements = change.generateStatements(database);

        then:
        assert statements != null
        assert statements[0] instanceof InsertOrUpdateStatement
        assert !statements[0].getOnlyUpdate()
    }

    def "loadUpdate generates InsertOrUpdateStatements with onlyUpdate"() throws Exception {
        when:
        MockDatabase database = new MockDatabase();
        database.setConnection((DatabaseConnection) null)

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");
        change.setOnlyUpdate(true);

        SqlStatement[] statements = change.generateStatements(database);

        then:
        assert statements != null
        assert statements[0] instanceof InsertOrUpdateStatement
        assert statements[0].getOnlyUpdate()
    }

    def "generateChecksum produces different values with each field - #version"() {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");

        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setFile("liquibase/change/core/sample.data2.csv");
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)
        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        V8 | "8:a91f2379b2b3b4c4a5a571b8e7409081" | "8:cce1423feea9e29192ef7c306eda0c94"
        V9 | "9:8280319eac780c3792e7cdd2de099891" | "9:a6cb98554859c3096cfdebdfe1ef39e5"
    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false;
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def change = new LoadUpdateDataChange()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check loadUpdateData status"
    }

    def "checksum does not change when no comments in CSV and comment property changes"() {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");
        //refactoring.setFileOpener(new JUnitResourceAccessor());

        refactoring.setCommentLineStartsWith("") //comments disabled
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setCommentLineStartsWith("#");
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        V8 | "8:a91f2379b2b3b4c4a5a571b8e7409081" | "8:a91f2379b2b3b4c4a5a571b8e7409081"
        V9 | "9:8280319eac780c3792e7cdd2de099891" | "9:8280319eac780c3792e7cdd2de099891"
    }

    def "checksum changes when there are comments in CSV"() {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-withComments.csv");

        refactoring.setCommentLineStartsWith("") //comments disabled
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setCommentLineStartsWith("#");
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        V8 | "8:becddfbcfda2ec516371ed36aaf1137a" | "8:e51a6408e921cfa151c50c7d90cf5baa"
        V9 | "9:83b74801c8e40d12eeb850da86ef5810" | "9:b470f206c5378dcea289ad01ac64e0b3"
    }

    def "checksum same for CSV files with comments and file with removed comments manually - #version"() {
        when:
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1-withComments.csv");

        refactoring.setCommentLineStartsWith("#");
        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        refactoring.setFile("liquibase/change/core/sample.data1-removedComments.csv");
        refactoring.setCommentLineStartsWith(""); //disable comments just in case
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return refactoring.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        V8 | "8:e51a6408e921cfa151c50c7d90cf5baa" | "8:e51a6408e921cfa151c50c7d90cf5baa"
        V9 | "9:b470f206c5378dcea289ad01ac64e0b3" | "9:b470f206c5378dcea289ad01ac64e0b3"
    }

    def "checksum change, when primaryKey change - #version"() {
        when:
        LoadUpdateDataChange loadData = new LoadUpdateDataChange();
        loadData.setTableName("TABLE_NAME");
        loadData.setFile("liquibase/change/core/sample.data.csv");
        loadData.setPrimaryKey("name")

        String md5sum1 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return loadData.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        loadData.setPrimaryKey("name,num"); // change primaryKey
        String md5sum2 = Scope.child([(Scope.Attr.checksumVersion.name()): version], {
            return loadData.generateCheckSum().toString()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        md5sum1 == originalChecksum
        md5sum2 == updatedChecksum

        where:
        version | originalChecksum | updatedChecksum
        V8 | "8:4af58779b39536e9d945384894337fce" | "8:4af58779b39536e9d945384894337fce"
        V9 | "9:84befedcb2f96ad1327863a031106f2d" | "9:2080d19736489c17a377500bbf16177b"
    }
}
