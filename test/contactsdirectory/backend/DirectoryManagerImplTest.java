/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contactsdirectory.backend;

//import static contactsdirectory.backend.ContactType.MAIL;
//import static contactsdirectory.backend.ContactType.PHONE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
public class DirectoryManagerImplTest {
    
    DirectoryManagerImpl manager;
    
    public DirectoryManagerImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
          manager = new DirectoryManagerImpl();      
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
    public void addContactToPerson()
    {                       
        try
        {
            manager.addContactToPerson(null, new MailContact());
            fail();
        }
        catch(NullPointerException e)
        {
            //OK
        }
        
        try
        {
            manager.addContactToPerson(new Person(), null);
            fail();
        }
        catch(NullPointerException e)
        {
            //OK
        }        
        
        ContactManager contactManager = new ContactManagerImpl();
        PersonManager personManager = new PersonManagerImpl();
        
        Contact contact = newContact(ContactType.MAIL,"note","test@test.com");
        Person person = newPerson("jmeno", "prijmeni");
        
        contactManager.createContact(contact);
        personManager.createPerson(person);
                
        manager.addContactToPerson(person, contact);                 
        
        List<Contact> result = manager.findAllContactsOfPerson(person);
        assertNotNull(result);
        
        assertEquals(contact, result.get(0));
    }
    
    @Test
    public void findAllContactsOfPerson()
    {
        try
        {
            manager.findAllContactsOfPerson(null);
            fail();
        }
        catch(NullPointerException e)
        {
            //OK
        }
        
        ContactManager contactManager = new ContactManagerImpl();
        PersonManager personManager = new PersonManagerImpl();
        
        Contact contact = newContact(ContactType.MAIL,"note","test@test.com");
        Contact contact2 = newContact(ContactType.PHONE,"note2","+420721532247");
        Contact contact3 = newContact(ContactType.PHONE,null,"+420721532222");
        Person person = newPerson("testName", "testSurname");
        
        personManager.createPerson(person);
        contactManager.createContact(contact);
        contactManager.createContact(contact2);
        contactManager.createContact(contact3);
        
        manager.addContactToPerson(person, contact);
        manager.addContactToPerson(person, contact2);
        manager.addContactToPerson(person, contact3);
        
        List<Contact> expected = Arrays.asList(contact, contact2, contact3);
        List<Contact> result = manager.findAllContactsOfPerson(person);
        
        assertNotNull(result);
        
        Collections.sort(result, contactIdComparator);
        Collections.sort(expected, contactIdComparator);
        
        assertEquals(expected, result);
        assertDeepEquals(expected, result);
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

    private static Person newPerson(String name, String lastName)
    {
        Person person = new Person();
        
        person.setName(name);
        person.setLastName(lastName);
        
        return person;
    }
    
    private static Comparator<Contact> contactIdComparator = new Comparator<Contact>() 
    {

        @Override
        public int compare(Contact o1, Contact o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };
    
    private void assertDeepEquals(List<Contact> expectedList, List<Contact> actualList)
    {
        for (int i = 0; i < expectedList.size(); i++) {
            Contact expected = expectedList.get(i);
            Contact actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
    
    private void assertDeepEquals(Contact expected, Contact actual)
    {
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
