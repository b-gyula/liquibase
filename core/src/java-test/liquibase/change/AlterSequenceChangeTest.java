package liquibase.change;

import liquibase.database.OracleDatabase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link AlterSequenceChange}
 */
public class AlterSequenceChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Alter Sequence", new AlterSequenceChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        AlterSequenceChange refactoring = new AlterSequenceChange();
        refactoring.setSequenceName("SEQ_NAME");
        OracleDatabase database = new OracleDatabase();

        refactoring.setMinValue(100);
        assertEquals("ALTER SEQUENCE SEQ_NAME MINVALUE 100", refactoring.generateStatements(database)[0].getSqlStatement(database));

        refactoring.setMinValue(null);
        refactoring.setMaxValue(1000);
        assertEquals("ALTER SEQUENCE SEQ_NAME MAXVALUE 1000", refactoring.generateStatements(database)[0].getSqlStatement(database));

        refactoring.setMaxValue(null);
        refactoring.setIncrementBy(50);
        assertEquals("ALTER SEQUENCE SEQ_NAME INCREMENT BY 50", refactoring.generateStatements(database)[0].getSqlStatement(database));

        refactoring.setIncrementBy(null);
        refactoring.setOrdered(true);
        assertEquals("ALTER SEQUENCE SEQ_NAME ORDER", refactoring.generateStatements(database)[0].getSqlStatement(database));

        refactoring.setMinValue(1);
        refactoring.setMaxValue(2);
        refactoring.setIncrementBy(3);
        assertEquals("ALTER SEQUENCE SEQ_NAME INCREMENT BY 3 MINVALUE 1 MAXVALUE 2 ORDER", refactoring.generateStatements(database)[0].getSqlStatement(database));

    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AlterSequenceChange refactoring = new AlterSequenceChange();
        refactoring.setSequenceName("SEQ_NAME");

        assertEquals("Sequence SEQ_NAME altered", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        AlterSequenceChange refactoring = new AlterSequenceChange();
        refactoring.setSequenceName("SEQ_NAME");

        Element node = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("alterSequence", node.getNodeName());
        assertEquals("SEQ_NAME", node.getAttribute("sequenceName"));
        assertFalse(node.hasAttribute("incrementBy"));
        assertFalse(node.hasAttribute("maxValue"));
        assertFalse(node.hasAttribute("minValue"));
        assertFalse(node.hasAttribute("ordered"));

        refactoring.setIncrementBy(1);
        refactoring.setMaxValue(2);
        refactoring.setMinValue(3);
        refactoring.setOrdered(true);

        node = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("alterSequence", node.getNodeName());
        assertEquals("SEQ_NAME", node.getAttribute("sequenceName"));
        assertEquals("1", node.getAttribute("incrementBy"));
        assertEquals("2", node.getAttribute("maxValue"));
        assertEquals("3", node.getAttribute("minValue"));
        assertEquals("true", node.getAttribute("ordered"));
    }
}