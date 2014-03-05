/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package contactsdirectory.backend;

import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tomáš
 */
public class ContactManagerImplTest {
    
    private ContactManagerImpl manager;
    
    public ContactManagerImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SQLException {
        manager = new ContactManagerImpl();
    }    
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    public void createContact() {
        Contact contact = newContact(ContactType.MAIL, "", "test@java.com");
        
        manager.createContact(contact);
        
        Long contactId = contact.getId();
        assertNotNull(contactId);
        Contact result = manager.findContactById(contactId);
        assertEquals(contact, result);
        assertNotSame(contact, result);
        assertDeepEquals(contact, result);
    }
    
    private static Contact newContact(ContactType type, String note, String data)
    {
        Contact contact = null;
        
        switch(type)
        {
            case MAIL:
                contact = new MailContact();
                contact.setType(type);
                contact.setNote(note);
                ((MailContact)contact).setMailAddress(data);
                break;
            case PHONE:
                contact = new PhoneContact();
                contact.setType(type);
                contact.setNote(note);
                ((PhoneContact)contact).setPhoneNumber(data);                
                break;
        }
        
        return contact;
    }

    private void assertDeepEquals(Contact expected, Contact actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getNote(), actual.getNote());
        switch(actual.getType())
        {
            case MAIL:
                assertEquals(((MailContact)expected).getMailAddress(), ((MailContact)actual).getMailAddress());
                break;
            case PHONE:
                assertEquals(((PhoneContact)expected).getPhoneNumber(), ((PhoneContact)actual).getPhoneNumber());
                break;
        }
    }
}