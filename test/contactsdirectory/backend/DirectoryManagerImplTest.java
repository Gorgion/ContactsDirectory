/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contactsdirectory.backend;

//import static contactsdirectory.backend.ContactType.MAIL;
//import static contactsdirectory.backend.ContactType.PHONE;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
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
public class DirectoryManagerImplTest
{

    private DirectoryManagerImpl manager;
    private ContactManagerImpl contactManager;
    private PersonManagerImpl personManager;
    private DataSource ds;
    Connection conn0;

    private static DataSource prepareDataSource() throws SQLException
    {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:derby:memory:DirectoryManagerTest;create=true");
        return ds;
    }

    private void createTables() throws SQLException
    {
        try (Connection conn = ds.getConnection())
        {
            conn.prepareStatement("CREATE TABLE PERSON ("
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "NAME VARCHAR(50), "
                    + "SURNAME VARCHAR(50)"
                    + ")").executeUpdate();

            conn.prepareStatement("CREATE TABLE CONTACT ("
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "PERSONID BIGINT REFERENCES PERSON (ID), "
                    + "TYPE INT NOT NULL, "
                    + "NOTE VARCHAR(255) "
                    + ")").executeUpdate();

            conn.prepareStatement("CREATE TABLE MAILCONTACT ("
                    + "CONTACTID BIGINT NOT NULL REFERENCES CONTACT (ID), "
                    + "MAILADDRESS VARCHAR(70), "
                    + "PRIMARY KEY(CONTACTID, MAILADDRESS)"
                    + ")").executeUpdate();

            conn.prepareStatement("CREATE TABLE PHONECONTACT ("
                    + "CONTACTID BIGINT NOT NULL REFERENCES CONTACT (ID), "
                    + "PHONENUMBER VARCHAR(20), "
                    + "PRIMARY KEY(CONTACTID, PHONENUMBER)"
                    + ")").executeUpdate();
        }
    }

    @Before
    public void setUp() throws SQLException
    {
        ds = prepareDataSource();
        //DBUtilities.executeSqlScript(ds,ContactManager.class.getResource("createTables.sql"));
        createTables();

        manager = new DirectoryManagerImpl();
        contactManager = new ContactManagerImpl();
        personManager = new PersonManagerImpl();

        manager.setDataSource(ds);
        contactManager.setDataSource(ds);
        //personManager.setDataSource(ds);
        conn0 = DriverManager.getConnection("jdbc:derby:memory:DirectoryManagerTest;create=true");
        personManager.setConn(conn0);
    }

    @After
    public void tearDown() throws SQLException
    {
        try (Connection conn = ds.getConnection())
        {
            conn.prepareStatement("DROP TABLE PHONECONTACT").executeUpdate();
            conn.prepareStatement("DROP TABLE MAILCONTACT").executeUpdate();
            conn.prepareStatement("DROP TABLE contact").executeUpdate();
            conn.prepareStatement("DROP TABLE person").executeUpdate();
        }
        conn0.close();
    }

    @Test
    public void addContactToPerson()
    {
        Contact contact = new ContactBuilder().setType(ContactType.MAIL)
                .setNote("note").setData("test@test.com").build();//newContact(ContactType.MAIL,"note","test@test.com");
        Person person = new PersonBuilder().setName("name").setSurname("surname").build();//newPerson("jmeno", "prijmeni");

        contactManager.createContact(contact);
        personManager.createPerson(person);

        manager.addContactToPerson(person, contact);

        List<Contact> result = manager.findAllContactsOfPerson(person);
        assertNotNull(result);

        assertEquals(contact, result.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addContactToPersonWithNullArguments()
    {
        manager.addContactToPerson(null, new MailContact());

        manager.addContactToPerson(new Person(), null);
    }

    @Test
    public void findAllContactsOfPerson()
    {
        Contact contact = new ContactBuilder().setType(ContactType.MAIL)
                .setNote("note").setData("test@test.com").build();//newContact(ContactType.MAIL,"note","test@test.com");
        Contact contact2 = new ContactBuilder().setType(ContactType.PHONE)
                .setNote("note2").setData("+420721532247").build();//newContact(ContactType.PHONE,"note2","+420721532247");
        Contact contact3 = new ContactBuilder().setType(ContactType.PHONE)
                .setData("+420721532222").build();//newContact(ContactType.PHONE,null,"+420721532222");
        Person person = new PersonBuilder().setName("name").setSurname("surname").build();//newPerson("testName", "testSurname");

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

    @Test(expected = IllegalArgumentException.class)
    public void findAllContactsOfPersonWithNullArguments()
    {
        manager.findAllContactsOfPerson(null);
    }

    @Test
    public void removeContactFromPerson()
    {
        Contact contact = new ContactBuilder().setType(ContactType.MAIL)
                .setNote("note").setData("test@test.com").build();//newContact(ContactType.MAIL,"note","test@test.com");
        Person person = new PersonBuilder().setName("name").setSurname("surname").build();//newPerson("jmeno", "prijmeni");

        personManager.createPerson(person);
        contactManager.createContact(contact);

        manager.addContactToPerson(person, contact);
        manager.removeContactFromPerson(person, contact);

        List<Contact> result = manager.findAllContactsOfPerson(person);
        assertTrue(result.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeContactFromPersonWithNullArguments()
    {
        manager.addContactToPerson(null, new MailContact());
        manager.addContactToPerson(new Person(), null);
    }

    private static Contact newContact(ContactType type, String note, String data)
    {
        Contact contact = null;

        switch (type)
        {
            case MAIL:
                contact = new MailContact();
                contact.setType(type);
                contact.setNote(note);
                ((MailContact) contact).setMailAddress(data);
                break;
            case PHONE:
                contact = new PhoneContact();
                contact.setType(type);
                contact.setNote(note);
                ((PhoneContact) contact).setPhoneNumber(data);
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
        public int compare(Contact o1, Contact o2)
        {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };

    private void assertDeepEquals(List<Contact> expectedList, List<Contact> actualList)
    {
        for (int i = 0; i < expectedList.size(); i++)
        {
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
        switch (actual.getType())
        {
            case MAIL:
                assertEquals(((MailContact) expected).getMailAddress(), ((MailContact) actual).getMailAddress());
                break;
            case PHONE:
                assertEquals(((PhoneContact) expected).getPhoneNumber(), ((PhoneContact) actual).getPhoneNumber());
                break;
        }
    }
}
