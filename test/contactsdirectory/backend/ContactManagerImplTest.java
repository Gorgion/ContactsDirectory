/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package contactsdirectory.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Tomáš
 */
public class ContactManagerImplTest 
{
    private Connection conn;
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
    public void setUp() throws SQLException 
    {        
        //conn = DriverManager.getConnection("jdbc:derby://localhost:1527/ContactManagerTest;create=true","","app");
        conn = DriverManager.getConnection("jdbc:derby:memory:ContactManagerTest;create=true");
        
        conn.prepareStatement("CREATE TABLE PERSON (" +
            "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
            "NAME VARCHAR(50), " +
            "SURNAME VARCHAR(50)" +
            ")").executeUpdate();
        
        conn.prepareStatement("CREATE TABLE CONTACT (" +
            "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
            "PERSONID BIGINT REFERENCES PERSON (ID), " +
            "TYPE INT NOT NULL, " +
            "NOTE VARCHAR(255) " +
            ")").executeUpdate();
        
        conn.prepareStatement("CREATE TABLE MAILCONTACT (" +
            "CONTACTID BIGINT NOT NULL REFERENCES CONTACT (ID), " +
            "MAILADDRESS VARCHAR(70), " +
            "PRIMARY KEY(CONTACTID, MAILADDRESS)" +
            ")").executeUpdate();
        
        conn.prepareStatement("CREATE TABLE PHONECONTACT (" +
            "CONTACTID BIGINT NOT NULL REFERENCES CONTACT (ID), " +
            "PHONENUMBER VARCHAR(20), " +
            "PRIMARY KEY(CONTACTID, PHONENUMBER)" +
            ")").executeUpdate();
        
        manager = new ContactManagerImpl(conn);
    }    
    
    @After
    public void tearDown() throws SQLException 
    {   
        conn.prepareStatement("DROP TABLE PHONECONTACT").executeUpdate();
        conn.prepareStatement("DROP TABLE MAILCONTACT").executeUpdate();
        conn.prepareStatement("DROP TABLE contact").executeUpdate();
        conn.prepareStatement("DROP TABLE person").executeUpdate();
        conn.close();               
    }
    
    @Ignore @Test
    public void createMailContact()
    {               
        Contact contact = new ContactBuilder().setData("test@java.com")
                .setNote("note").setType(ContactType.MAIL).build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        
        Long contactId = contact.getId();
        assertNotNull(contactId);
        Contact result = manager.getContact(contactId);
        assertEquals(contact, result);
        assertNotSame(contact, result);
        assertDeepEquals(contact, result);
    }    
    
    @Test
    public void createPhoneContact()
    {               
        Contact contact = new ContactBuilder().setData("911")
                .setNote("note").setType(ContactType.PHONE).build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        
        Long contactId = contact.getId();
        assertNotNull(contactId);
        Contact result = manager.getContact(contactId);
        assertEquals(contact, result);
        assertNotSame(contact, result);
        assertDeepEquals(contact, result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createContactWithNullArguments()
    {        
        manager.createContact(null);
            
        Contact contact = new ContactBuilder().setType(null).build();//newContact(null, "note", "test@java.com");        
        manager.createContact(contact);
           
        contact = new ContactBuilder().setData(null).build();//newContact(ContactType.MAIL, "note", null);        
        manager.createContact(contact);            
    }
    
    @Test
    public void editContact()
    {
        Contact contact = new ContactBuilder().setData("test@java.com")
                .setNote("note").setType(ContactType.MAIL).build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        
        Long contactId = contact.getId();        
        
        contact.setNote("anotherNote");
        ((MailContact)contact).setMailAddress("test2@java.com");
        
        manager.editContact(contact);
        
        Contact result = manager.getContact(contactId);
        assertEquals(contact, result);
        assertNotSame(contact, result);
        assertDeepEquals(contact, result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void editContactWithNullArguments()            
    {
        manager.editContact(null);        
        
        Contact contact = new ContactBuilder().setData("test@java.com")
                .setNote("note").setType(ContactType.MAIL).build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        contact.setId(null);
        manager.editContact(contact);
        
        contact = new ContactBuilder().setData("test@java.com")
                .setNote("note").setType(ContactType.MAIL).build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        contact.setType(null);
        manager.editContact(contact);
        
        contact = new ContactBuilder().setData("test@java.com")
                .setNote("note").setType(ContactType.MAIL).build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        ((MailContact)contact).setMailAddress(null);
        manager.editContact(contact);
        
        contact = new ContactBuilder().setData("555")
                .setNote("note").setType(ContactType.PHONE).build();//newContact(ContactType.PHONE, "note", "555");
        manager.createContact(contact);
        ((PhoneContact)contact).setPhoneNumber(null);
        manager.editContact(contact);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void editContactWithIllegalTypeChange()
    {
        Contact contact = new ContactBuilder().setData("test@java.com")
                .setNote("note").setType(ContactType.MAIL).build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        contact.setType(ContactType.PHONE);
        manager.editContact(contact);
        
        contact = new ContactBuilder().setData("112")
                .setNote("note").setType(ContactType.PHONE).build();//newContact(ContactType.PHONE, "note", "test@java.com");
        manager.createContact(contact);
        contact.setType(ContactType.MAIL);
        manager.editContact(contact);
    }        
    
    @Test
    public void removeContact()
    {
        Contact contact = new ContactBuilder().setData("test@java.com")
                .setNote("note").setType(ContactType.MAIL).build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        
        Long contactId = contact.getId();        
        
        manager.removeContact(contact);
        
        Contact result = manager.getContact(contactId);
        assertNull(result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void removeContactWithNullArguments()
    {                
        manager.removeContact(null);
    }
    
    @Test
    public void findContactById()
    {
        Contact contact = new ContactBuilder().setType(ContactType.MAIL)
                .setNote("note").setData("test@java.com").build();//newContact(ContactType.MAIL, "note", "test@java.com");
        manager.createContact(contact);
        
        Long contactId = contact.getId();
        
        Contact result = manager.getContact(contactId);
        assertEquals(contact, result);
        assertNotSame(contact, result);
        assertDeepEquals(contact, result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void findContactByIdWithNullArguments()
    {
        manager.getContact(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void findContactByIdWithOutOfRangeArguments()
    {        
        Contact result = manager.getContact(Long.MIN_VALUE);
        assertNull(result);
        
        result = manager.getContact(0L);
        assertNull(result);
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

    private static void assertDeepEquals(Contact expected, Contact actual) 
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

class ContactBuilder
{
    private String note;
    private ContactType type;
    private String data;
    private Long id;
    
    public ContactBuilder()
    {
        note = "note";
        id = null;
        data = "data";
        type = ContactType.MAIL;
    }
    
    public Contact build()
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
        //contact.setId(id);
        return contact;
    }
    
    public ContactBuilder setType(ContactType type)
    {
        this.type = type;
        return this;
    }
    
    public ContactBuilder setData(String data)
    {
        this.data = data;
        return this;
    }
    
    public ContactBuilder setId(Long id)
    {
        this.id = id;
        return this;
    }
    
    public ContactBuilder setNote(String note)
    {
        this.note = note;
        return this;
    }
}