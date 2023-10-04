package deors.core.directory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Expectations;
import mockit.Mocked;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

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

    @Test(expected = DirectoryException.class)
    public void testConstructorError(@Mocked LDAPConnection connection)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            result = new LDAPException("error", 1, "error");
        }};

        new DirectoryManager("localhost", 2000);
    }

    @Test
    public void testConstructorOk(@Mocked LDAPConnection connection)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());
    }

    @Test(expected = DirectoryException.class)
    public void testConstructorErrorAlreadyConnected(@Mocked LDAPConnection connection)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        dm.createConnection("otherhost", 3000);
    }

    @Test
    public void testCloseConnectionOk(@Mocked LDAPConnection connection)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.disconnect();
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        dm.closeConnection();

        assertFalse(dm.isConnected());
    }

    @Test(expected = DirectoryException.class)
    public void testCloseConnectionError(@Mocked LDAPConnection connection)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.disconnect();
            result = new LDAPException("error", 1, "error");
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.closeConnection();
    }

    @Test
    public void testGetAttributeValueDNNotFound(
            @Mocked LDAPConnection connection, @Mocked LDAPSearchResults searchResults)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = searchResults;
            searchResults.hasMore();
            result = false;
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertNull(dm.getAttributeValue("theObjectDN", "theAttributeName"));
    }

    @Test
    public void testGetAttributeValueDNFoundNoValue(
            @Mocked LDAPConnection connection, @Mocked LDAPSearchResults searchResults)
        throws DirectoryException, LDAPException {

        LDAPEntry entry = new LDAPEntry("theObjectDN", new LDAPAttributeSet());

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = searchResults;
            searchResults.hasMore();
            result = true;
            searchResults.next();
            result = entry;
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertNull(dm.getAttributeValue("theObjectDN", "theAttributeName"));
    }

    @Test
    public void testGetAttributeValueOk(
            @Mocked LDAPConnection connection, @Mocked LDAPSearchResults searchResults)
        throws DirectoryException, LDAPException {

        LDAPAttributeSet attributes = new LDAPAttributeSet();
        attributes.add(new LDAPAttribute("theAttributeName", "theValue"));
        LDAPEntry entry = new LDAPEntry("theObjectDN", attributes);

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = searchResults;
            searchResults.hasMore();
            result = true;
            searchResults.next();
            result = entry;
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertEquals("theValue", dm.getAttributeValue("theObjectDN", "theAttributeName"));
    }

    @Test(expected = DirectoryException.class)
    public void testGetAttributeValueError(@Mocked LDAPConnection connection)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = new LDAPException("error", 1, "error");
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.getAttributeValue("theObjectDN", "theAttributeName");
    }

    @Test
    public void testGetAttributeValuesEmpty(
            @Mocked LDAPConnection connection, @Mocked LDAPSearchResults searchResults)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = searchResults;
            searchResults.hasMore();
            result = false;
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertArrayEquals(new String[0], dm.getAttributeValues("theObjectDN", "theAttributeName"));
    }

    @Test
    public void testGetAttributeValuesOk(
            @Mocked LDAPConnection connection, @Mocked LDAPSearchResults searchResults)
        throws DirectoryException, LDAPException {

        LDAPAttributeSet attributes = new LDAPAttributeSet();
        attributes.add(new LDAPAttribute("theAttributeName", new String[] {"theValue1", "theValue2"}));
        LDAPEntry entry = new LDAPEntry("theObjectDN", attributes);

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = searchResults;
            searchResults.hasMore();
            result = true;
            searchResults.next();
            result = entry;
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertArrayEquals(new String[] {"theValue1", "theValue2"}, dm.getAttributeValues("theObjectDN", "theAttributeName"));
    }

    @Test(expected = DirectoryException.class)
    public void testGetAttributeValuesError(@Mocked LDAPConnection connection)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = new LDAPException("error", 1, "error");
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.getAttributeValues("theObjectDN", "theAttributeName");
    }

    @Test
    public void testGetAttributeValueBytesEmpty(
            @Mocked LDAPConnection connection, @Mocked LDAPSearchResults searchResults)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = searchResults;
            searchResults.hasMore();
            result = false;
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertArrayEquals(new byte[0], dm.getAttributeValueBytes("theObjectDN", "theAttributeName"));
    }

    @Test
    public void testGetAttributeValueBytesOk(
            @Mocked LDAPConnection connection, @Mocked LDAPSearchResults searchResults)
        throws DirectoryException, LDAPException {

        LDAPAttributeSet attributes = new LDAPAttributeSet();
        attributes.add(new LDAPAttribute("theAttributeName", new byte[] {4, 8, -32}));
        LDAPEntry entry = new LDAPEntry("theObjectDN", attributes);

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = searchResults;
            searchResults.hasMore();
            result = true;
            searchResults.next();
            result = entry;
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertArrayEquals(new byte[] {4, 8, -32}, dm.getAttributeValueBytes("theObjectDN", "theAttributeName"));
    }

    @Test(expected = DirectoryException.class)
    public void testGetAttributeValueBytesError(@Mocked LDAPConnection connection)
        throws DirectoryException, LDAPException {

        new Expectations() {{
            connection.connect("localhost", 2000);
            connection.search("theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false);
            result = new LDAPException("error", 1, "error");
        }};

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.getAttributeValueBytes("theObjectDN", "theAttributeName");
    }
}
