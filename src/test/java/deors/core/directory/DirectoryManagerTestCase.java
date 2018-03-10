package deors.core.directory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DirectoryManagerTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public DirectoryManagerTestCase() {

        super();
    }

    @Test
    public void testDefaultConstructor() {

        DirectoryManager dm = new DirectoryManager();
        assertNotNull(dm);
        assertFalse(dm.isConnected());
        assertNull(dm.getConnection());
    }

    @Test
    public void testConstructorIAE1() throws DirectoryException {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("error while creating connection: invalid directory host and/or port");

        new DirectoryManager(null, 2000);
    }

    @Test
    public void testConstructorIAE2() throws DirectoryException {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("error while creating connection: invalid directory host and/or port");

        new DirectoryManager("", 2000);
    }

    @Test
    public void testConstructorIAE3() throws DirectoryException {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("error while creating connection: invalid directory host and/or port");

        new DirectoryManager("localhost", -1);
    }

    @Test
    public void testCloseNotConnected() throws DirectoryException {

        thrown.expect(DirectoryException.class);
        thrown.expectMessage("error while closing connection: there is no active connection to be closed");

        DirectoryManager dm = new DirectoryManager();
        dm.closeConnection();
    }

    @Test
    public void testGetAttributeValueNotConnected() throws DirectoryException {

        thrown.expect(DirectoryException.class);
        thrown.expectMessage("there is no active connection to perform action");

        DirectoryManager dm = new DirectoryManager();
        dm.getAttributeValue(null, null);
    }

    @Test
    public void testGetAttributeValuesNotConnected() throws DirectoryException {

        thrown.expect(DirectoryException.class);
        thrown.expectMessage("there is no active connection to perform action");

        DirectoryManager dm = new DirectoryManager();
        dm.getAttributeValues(null, null);
    }

    @Test
    public void testGetAttributeValueBytesNotConnected() throws DirectoryException {

        thrown.expect(DirectoryException.class);
        thrown.expectMessage("there is no active connection to perform action");

        DirectoryManager dm = new DirectoryManager();
        dm.getAttributeValueBytes(null, null);
    }
}
