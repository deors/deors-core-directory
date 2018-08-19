package deors.core.directory;

import java.util.Iterator;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

/**
 * Class for managing Directory services using LDAP protocol.
 *
 * @author deors
 * @version 1.0
 */
public class DirectoryManager {

    /**
     * Flag that is <code>true</code> when there is an active directory connection.
     *
     * @see DirectoryManager#isConnected()
     * @see DirectoryManager#CONNECTION_ACTIVE
     * @see DirectoryManager#CONNECTION_INACTIVE
     */
    private boolean connected;

    /**
     * Connection object where the active connection is stored.
     */
    private LDAPConnection connection;

    /**
     * Constant for an active connection.
     */
    public static final boolean CONNECTION_ACTIVE = true;

    /**
     * Constant for an inactive connection.
     */
    public static final boolean CONNECTION_INACTIVE = false;

    /**
     * The finalizer guardian.
     */
    protected final Object finalizerGuardian = new Object() {

        /**
         * Finalizes the object by closing the current connection if it is opened.
         *
         * @throws java.lang.Throwable a throwable object
         *
         * @see java.lang.Object#finalize()
         */
        protected void finalize()
            // CHECKSTYLE:OFF
            throws java.lang.Throwable {
            // CHECKSTYLE:ON

            try {
                if (isConnected()) {
                    closeConnection();
                }
            } finally {
                super.finalize();
            }
        }
    };

    /**
     * Default constructor.
     */
    public DirectoryManager() {
        super();
    }

    /**
     * Constructor that creates a connection to the given directory host and port.
     *
     * @param directoryHost the directory host name or IP address
     * @param directoryPort the directory service port
     *
     * @throws DirectoryException an error while accessing the directory
     */
    public DirectoryManager(String directoryHost, int directoryPort)
        throws DirectoryException {

        this();
        createConnection(directoryHost, directoryPort);
    }

    /**
     * Closes the actual directory connection.
     *
     * @throws DirectoryException an error while accessing the directory
     */
    public void closeConnection()
        throws DirectoryException {

        if (!connected) {
            throw new DirectoryException(DirectoryContext.getMessage("LDAPMGR_ERR_CLOSE_CONN_NO")); //$NON-NLS-1$
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (LDAPException ldape) {
                throw new DirectoryException(
                    DirectoryContext.getMessage("LDAPMGR_ERR_CLOSE_CONN", ldape.getMessage()), //$NON-NLS-1$
                    ldape);
            }
        }

        connected = false;
    }

    /**
     * Creates a connection to the given directory host and port.
     *
     * @param directoryHost the directory host name or IP address
     * @param directoryPort the directory service port
     *
     * @throws DirectoryException an error while accessing the directory
     */
    public final void createConnection(String directoryHost, int directoryPort)
        throws DirectoryException {

        if (connected) {
            throw new DirectoryException(DirectoryContext.getMessage("LDAPMGR_ERR_OPEN_CONN_EXISTS")); //$NON-NLS-1$
        }

        if (directoryHost == null || directoryHost.length() == 0 || directoryPort <= 0) {
            throw new IllegalArgumentException(DirectoryContext.getMessage("LDAPMGR_ERR_OPEN_CONN_ARG")); //$NON-NLS-1$
        }

        try {
            connection = new LDAPConnection();
            connection.connect(directoryHost, directoryPort);
        } catch (LDAPException ldape) {
            throw new DirectoryException(
                DirectoryContext.getMessage("LDAPMGR_ERR_OPEN_CONN", ldape.getMessage()), //$NON-NLS-1$
                ldape);
        }

        connected = true;
    }

    /**
     * Returns the connection object.
     *
     * @return the connection object
     */
    public LDAPConnection getConnection() {

        return connection;
    }

    /**
     * Returns the attribute object for the given object DN and attribute name, or
     * <code>null</code> if the attribute or object was not found.
     *
     * @param objectDN the object DN
     * @param attributeName the attribute name
     *
     * @return the attribute object or <code>null</code> if not found
     *
     * @throws DirectoryException an error while accessing the directory
     */
    private LDAPAttribute getAttribute(String objectDN, String attributeName)
        throws DirectoryException {

            try {
            LDAPSearchResults res = connection.search(
                objectDN, LDAPConnection.SCOPE_BASE,
                DirectoryContext.BLANK, new String[] {attributeName}, false);

            LDAPEntry nextEntry = null;
            LDAPAttributeSet attributeSet = null;
            LDAPAttribute attribute = null;
            @SuppressWarnings("rawtypes")
            Iterator allAttributes = null;

            if (res.hasMore()) {
                nextEntry = res.next();
                attributeSet = nextEntry.getAttributeSet();
                if (attributeSet.size() == 0) {
                    return null;
                }

                allAttributes = attributeSet.iterator();
                if (allAttributes.hasNext()) {
                    attribute = (LDAPAttribute) allAttributes.next();
                    if (attribute.size() == 0) {
                        return null;
                    }

                    return attribute;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (LDAPException ldape) {
            throw new DirectoryException(
                DirectoryContext.getMessage("LDAPMGR_ERR_SEARCH", ldape.getMessage()), //$NON-NLS-1$
                ldape);
        }
    }

    /**
     * Returns the attribute value as a string for the given object DN and attribute name,
     * or <code>null</code> if the attribute or object was not found.
     *
     * @param objectDN the object DN
     * @param attributeName the attribute name
     *
     * @return the attribute value as a string or <code>null</code> if not found
     *
     * @throws DirectoryException an error while accessing the directory
     */
    public String getAttributeValue(String objectDN, String attributeName)
        throws DirectoryException {

        if (!connected) {
            throw new DirectoryException(DirectoryContext.getMessage("LDAPMGR_ERR_NO_CONN")); //$NON-NLS-1$
        }

        LDAPAttribute attribute = getAttribute(objectDN, attributeName);

        if (attribute == null) {
            return null;
        }

        return attribute.getStringValue();
    }

    /**
     * Returns the attribute values as a string array for the given object DN and
     * attribute name, or an empty array if the attribute or object was not found.
     *
     * @param objectDN the object DN
     * @param attributeName the attribute name
     *
     * @return the attribute value as a string array or an empty array if not found
     *
     * @throws DirectoryException an error while accessing the directory
     */
    public String[] getAttributeValues(String objectDN, String attributeName)
        throws DirectoryException {

        if (!connected) {
            throw new DirectoryException(DirectoryContext.getMessage("LDAPMGR_ERR_NO_CONN")); //$NON-NLS-1$
        }

        LDAPAttribute attribute = getAttribute(objectDN, attributeName);

        if (attribute == null) {
            return new String[0];
        }

        return attribute.getStringValueArray();
    }

    /**
     * Returns the attribute value as a byte array for the given object DN and attribute
     * name, or an empty array if the attribute or object was not found.
     *
     * @param objectDN the object DN
     * @param attributeName the attribute name
     *
     * @return the attribute value as a byte array or an empty array if not found
     *
     * @throws DirectoryException an error while accessing the directory
     */
    public byte[] getAttributeValueBytes(String objectDN, String attributeName)
        throws DirectoryException {

        if (!connected) {
            throw new DirectoryException(DirectoryContext.getMessage("LDAPMGR_ERR_NO_CONN")); //$NON-NLS-1$
        }

        LDAPAttribute attribute = getAttribute(objectDN, attributeName);

        if (attribute == null) {
            return new byte[0];
        }

        return attribute.getByteValue();
    }

    /**
     * Returns the <code>connected</code> property value.
     *
     * @return the property value
     *
     * @see DirectoryManager#connected
     * @see DirectoryManager#CONNECTION_ACTIVE
     * @see DirectoryManager#CONNECTION_INACTIVE
     */
    public boolean isConnected() {
        return connected;
    }
}
