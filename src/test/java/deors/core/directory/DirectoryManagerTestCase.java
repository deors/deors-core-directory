package deors.core.directory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public class DirectoryManagerTestCase {

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
    public void testConstructorIAE1() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> new DirectoryManager(null, 2000));
        assertEquals("error while creating connection: invalid directory host and/or port", ex.getMessage());
    }

    @Test
    public void testConstructorIAE2() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> new DirectoryManager("", 2000));
        assertEquals("error while creating connection: invalid directory host and/or port", ex.getMessage());
    }

    @Test
    public void testConstructorIAE3() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> new DirectoryManager("localhost", -1));
        assertEquals("error while creating connection: invalid directory host and/or port", ex.getMessage());
    }

    @Test
    public void testCloseNotConnected() {

        DirectoryException ex = assertThrows(DirectoryException.class, () -> {
            DirectoryManager dm = new DirectoryManager();
            dm.closeConnection();
        });
        assertEquals("error while closing connection: there is no active connection to be closed", ex.getMessage());
    }

    @Test
    public void testGetAttributeValueNotConnected() {

        DirectoryException ex = assertThrows(DirectoryException.class, () -> {
            DirectoryManager dm = new DirectoryManager();
            dm.getAttributeValue(null, null);
        });
        assertEquals("there is no active connection to perform action", ex.getMessage());
    }

    @Test
    public void testGetAttributeValuesNotConnected() {

        DirectoryException ex = assertThrows(DirectoryException.class, () -> {
            DirectoryManager dm = new DirectoryManager();
            dm.getAttributeValues(null, null);
        });
        assertEquals("there is no active connection to perform action", ex.getMessage());
    }

    @Test
    public void testGetAttributeValueBytesNotConnected() {

        DirectoryException ex = assertThrows(DirectoryException.class, () -> {
            DirectoryManager dm = new DirectoryManager();
            dm.getAttributeValueBytes(null, null);
        });
        assertEquals("there is no active connection to perform action", ex.getMessage());
    }

    @Test
    public void testConstructorError() throws LDAPException {

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> doThrow(new LDAPException("error", 1, "error"))
                    .when(connection).connect("localhost", 2000))) {

            assertThrows(DirectoryException.class, () -> new DirectoryManager("localhost", 2000));
        }
    }

    @Test
    public void testConstructorOk() throws DirectoryException {

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class)) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);

            assertNotNull(dm);
            assertTrue(dm.isConnected());
        }
    }

    @Test
    public void testConstructorErrorAlreadyConnected() throws DirectoryException {

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class)) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);

            assertNotNull(dm);
            assertTrue(dm.isConnected());

            assertThrows(DirectoryException.class, () -> dm.createConnection("otherhost", 3000));
        }
    }

    @Test
    public void testCloseConnectionOk() throws DirectoryException {

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class)) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);

            assertNotNull(dm);
            assertTrue(dm.isConnected());

            dm.closeConnection();

            assertFalse(dm.isConnected());
        }
    }

    @Test
    public void testCloseConnectionError() throws DirectoryException, LDAPException {

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> doThrow(new LDAPException("error", 1, "error"))
                    .when(connection).disconnect())) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertThrows(DirectoryException.class, dm::closeConnection);
        }
    }

    @Test
    public void testGetAttributeValueDNNotFound() throws DirectoryException, LDAPException {

        LDAPSearchResults searchResults = mock(LDAPSearchResults.class);
        when(searchResults.hasMore()).thenReturn(false);

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenReturn(searchResults))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertNull(dm.getAttributeValue("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValueDNFoundNoValue() throws DirectoryException, LDAPException {

        LDAPEntry entry = new LDAPEntry("theObjectDN", new LDAPAttributeSet());
        LDAPSearchResults searchResults = mock(LDAPSearchResults.class);
        when(searchResults.hasMore()).thenReturn(true);
        when(searchResults.next()).thenReturn(entry);

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenReturn(searchResults))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertNull(dm.getAttributeValue("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValueOk() throws DirectoryException, LDAPException {

        LDAPAttributeSet attributes = new LDAPAttributeSet();
        attributes.add(new LDAPAttribute("theAttributeName", "theValue"));
        LDAPEntry entry = new LDAPEntry("theObjectDN", attributes);
        LDAPSearchResults searchResults = mock(LDAPSearchResults.class);
        when(searchResults.hasMore()).thenReturn(true);
        when(searchResults.next()).thenReturn(entry);

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenReturn(searchResults))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertEquals("theValue", dm.getAttributeValue("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValueError() throws DirectoryException, LDAPException {

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenThrow(new LDAPException("error", 1, "error")))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertThrows(DirectoryException.class,
                () -> dm.getAttributeValue("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValuesEmpty() throws DirectoryException, LDAPException {

        LDAPSearchResults searchResults = mock(LDAPSearchResults.class);
        when(searchResults.hasMore()).thenReturn(false);

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenReturn(searchResults))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertArrayEquals(new String[0], dm.getAttributeValues("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValuesOk() throws DirectoryException, LDAPException {

        LDAPAttributeSet attributes = new LDAPAttributeSet();
        attributes.add(new LDAPAttribute("theAttributeName", new String[] {"theValue1", "theValue2"}));
        LDAPEntry entry = new LDAPEntry("theObjectDN", attributes);
        LDAPSearchResults searchResults = mock(LDAPSearchResults.class);
        when(searchResults.hasMore()).thenReturn(true);
        when(searchResults.next()).thenReturn(entry);

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenReturn(searchResults))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertArrayEquals(new String[] {"theValue1", "theValue2"},
                dm.getAttributeValues("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValuesError() throws DirectoryException, LDAPException {

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenThrow(new LDAPException("error", 1, "error")))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertThrows(DirectoryException.class,
                () -> dm.getAttributeValues("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValueBytesEmpty() throws DirectoryException, LDAPException {

        LDAPSearchResults searchResults = mock(LDAPSearchResults.class);
        when(searchResults.hasMore()).thenReturn(false);

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenReturn(searchResults))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertArrayEquals(new byte[0], dm.getAttributeValueBytes("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValueBytesOk() throws DirectoryException, LDAPException {

        LDAPAttributeSet attributes = new LDAPAttributeSet();
        attributes.add(new LDAPAttribute("theAttributeName", new byte[] {4, 8, -32}));
        LDAPEntry entry = new LDAPEntry("theObjectDN", attributes);
        LDAPSearchResults searchResults = mock(LDAPSearchResults.class);
        when(searchResults.hasMore()).thenReturn(true);
        when(searchResults.next()).thenReturn(entry);

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenReturn(searchResults))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertArrayEquals(new byte[] {4, 8, -32},
                dm.getAttributeValueBytes("theObjectDN", "theAttributeName"));
        }
    }

    @Test
    public void testGetAttributeValueBytesError() throws DirectoryException, LDAPException {

        try (MockedConstruction<LDAPConnection> mocked = mockConstruction(LDAPConnection.class,
                (connection, context) -> when(connection.search(
                    "theObjectDN", LDAPConnection.SCOPE_BASE, "", new String[] {"theAttributeName"}, false))
                    .thenThrow(new LDAPException("error", 1, "error")))) {

            DirectoryManager dm = new DirectoryManager("localhost", 2000);
            assertThrows(DirectoryException.class,
                () -> dm.getAttributeValueBytes("theObjectDN", "theAttributeName"));
        }
    }
}
