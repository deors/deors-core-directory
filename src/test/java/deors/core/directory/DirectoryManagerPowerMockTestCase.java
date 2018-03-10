package deors.core.directory;

import static deors.core.directory.DirectoryContext.BLANK;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DirectoryManager.class)
public class DirectoryManagerPowerMockTestCase {

    public DirectoryManagerPowerMockTestCase() {

        super();
    }

    @Test(expected = DirectoryException.class)
    public void testConstructorError() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expectLastCall().andThrow(new LDAPException("error", 1, "error"));

        replay(lc, LDAPConnection.class);

        new DirectoryManager("localhost", 2000);
    }

    @Test
    public void testConstructorOk() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);

        replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        verify(lc, LDAPConnection.class);
    }

    @Test(expected = DirectoryException.class)
    public void testConstructorErrorAlreadyConnected() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);

        replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        dm.createConnection("otherhost", 3000);
    }

    @Test
    public void testCloseConnectionOk() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        lc.disconnect();

        replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);

        assertNotNull(dm);
        assertTrue(dm.isConnected());

        dm.closeConnection();

        assertFalse(dm.isConnected());

        verify(lc, LDAPConnection.class);
    }

    @Test(expected = DirectoryException.class)
    public void testCloseConnectionError() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        lc.disconnect();
        expectLastCall().andThrow(new LDAPException("error", 1, "error"));

        replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.closeConnection();
    }

    @Test
    public void testGetAttributeValueNull1() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(false);

        replay(lc, LDAPConnection.class, lsr);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertNull(dm.getAttributeValue("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr);
    }

    @Test
    public void testGetAttributeValueNull2() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);
        LDAPEntry le = createMock(LDAPEntry.class);
        LDAPAttributeSet las = createMock(LDAPAttributeSet.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(true);
        expect(lsr.next()).andReturn(le);
        expect(le.getAttributeSet()).andReturn(las);
        expect(las.size()).andReturn(0);

        replay(lc, LDAPConnection.class, lsr, le, las);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertNull(dm.getAttributeValue("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr, le, las);
    }

    @Test
    public void testGetAttributeValueNull3() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);
        LDAPEntry le = createMock(LDAPEntry.class);
        LDAPAttributeSet las = createMock(LDAPAttributeSet.class);
        Iterator it = createMock(Iterator.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(true);
        expect(lsr.next()).andReturn(le);
        expect(le.getAttributeSet()).andReturn(las);
        expect(las.size()).andReturn(1);
        expect(las.iterator()).andReturn(it);
        expect(it.hasNext()).andReturn(false);

        replay(lc, LDAPConnection.class, lsr, le, las, it);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertNull(dm.getAttributeValue("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr, le, las, it);
    }

    @Test
    public void testGetAttributeValueNull4() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);
        LDAPEntry le = createMock(LDAPEntry.class);
        LDAPAttributeSet las = createMock(LDAPAttributeSet.class);
        Iterator it = createMock(Iterator.class);
        LDAPAttribute la = createMock(LDAPAttribute.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(true);
        expect(lsr.next()).andReturn(le);
        expect(le.getAttributeSet()).andReturn(las);
        expect(las.size()).andReturn(1);
        expect(las.iterator()).andReturn(it);
        expect(it.hasNext()).andReturn(true);
        expect(it.next()).andReturn(la);
        expect(la.size()).andReturn(0);

        replay(lc, LDAPConnection.class, lsr, le, las, it, la);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertNull(dm.getAttributeValue("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr, le, las, it, la);
    }

    @Test
    public void testGetAttributeValueOk() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);
        LDAPEntry le = createMock(LDAPEntry.class);
        LDAPAttributeSet las = createMock(LDAPAttributeSet.class);
        Iterator it = createMock(Iterator.class);
        LDAPAttribute la = createMock(LDAPAttribute.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(true);
        expect(lsr.next()).andReturn(le);
        expect(le.getAttributeSet()).andReturn(las);
        expect(las.size()).andReturn(1);
        expect(las.iterator()).andReturn(it);
        expect(it.hasNext()).andReturn(true);
        expect(it.next()).andReturn(la);
        expect(la.size()).andReturn(1);
        expect(la.getStringValue()).andReturn("theValue");

        replay(lc, LDAPConnection.class, lsr, le, las, it, la);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertEquals("theValue", dm.getAttributeValue("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr, le, las, it, la);
    }

    @Test(expected = DirectoryException.class)
    public void testGetAttributeValueError() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false));
        expectLastCall().andThrow(new LDAPException("error", 1, "error"));

        replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.getAttributeValue("theObjectDN", "theAttributeName");
    }

    @Test
    public void testGetAttributeValuesEmpty() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(false);

        replay(lc, LDAPConnection.class, lsr);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertArrayEquals(new String[0], dm.getAttributeValues("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr);
    }

    @Test
    public void testGetAttributeValuesOk() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);
        LDAPEntry le = createMock(LDAPEntry.class);
        LDAPAttributeSet las = createMock(LDAPAttributeSet.class);
        Iterator it = createMock(Iterator.class);
        LDAPAttribute la = createMock(LDAPAttribute.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(true);
        expect(lsr.next()).andReturn(le);
        expect(le.getAttributeSet()).andReturn(las);
        expect(las.size()).andReturn(1);
        expect(las.iterator()).andReturn(it);
        expect(it.hasNext()).andReturn(true);
        expect(it.next()).andReturn(la);
        expect(la.size()).andReturn(1);
        expect(la.getStringValueArray()).andReturn(new String[] {"theValue1", "theValue2"});

        replay(lc, LDAPConnection.class, lsr, le, las, it, la);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertArrayEquals(new String[] {"theValue1", "theValue2"}, dm.getAttributeValues("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr, le, las, it, la);
    }

    @Test(expected = DirectoryException.class)
    public void testGetAttributeValuesError() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false));
        expectLastCall().andThrow(new LDAPException("error", 1, "error"));

        replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.getAttributeValues("theObjectDN", "theAttributeName");
    }

    @Test
    public void testGetAttributeValueBytesEmpty() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(false);

        replay(lc, LDAPConnection.class, lsr);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertArrayEquals(new byte[0], dm.getAttributeValueBytes("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr);
    }

    @Test
    public void testGetAttributeValueBytesOk() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);
        LDAPSearchResults lsr = createMock(LDAPSearchResults.class);
        LDAPEntry le = createMock(LDAPEntry.class);
        LDAPAttributeSet las = createMock(LDAPAttributeSet.class);
        Iterator it = createMock(Iterator.class);
        LDAPAttribute la = createMock(LDAPAttribute.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        expect(lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false))).andReturn(lsr);
        expect(lsr.hasMore()).andReturn(true);
        expect(lsr.next()).andReturn(le);
        expect(le.getAttributeSet()).andReturn(las);
        expect(las.size()).andReturn(1);
        expect(las.iterator()).andReturn(it);
        expect(it.hasNext()).andReturn(true);
        expect(it.next()).andReturn(la);
        expect(la.size()).andReturn(1);
        expect(la.getByteValue()).andReturn(new byte[] {4, 8, -32});

        replay(lc, LDAPConnection.class, lsr, le, las, it, la);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        assertArrayEquals(new byte[] {4, 8, -32}, dm.getAttributeValueBytes("theObjectDN", "theAttributeName"));

        verify(lc, LDAPConnection.class, lsr, le, las, it, la);
    }

    @Test(expected = DirectoryException.class)
    public void testGetAttributeValueBytesError() throws Exception {

        LDAPConnection lc = createMock(LDAPConnection.class);

        expectNew(LDAPConnection.class).andReturn(lc);
        lc.connect("localhost", 2000);
        lc.search(eq("theObjectDN"), eq(LDAPConnection.SCOPE_BASE), eq(BLANK), aryEq(new String[] {"theAttributeName"}), eq(false));
        expectLastCall().andThrow(new LDAPException("error", 1, "error"));

        replay(lc, LDAPConnection.class);

        DirectoryManager dm = new DirectoryManager("localhost", 2000);
        dm.getAttributeValueBytes("theObjectDN", "theAttributeName");
    }
}
