/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package contactsdirectory.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
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
 * @author xribaric
 */
public class PersonManagerImplTest {
   
    private PersonManagerImpl manager;
    
    /*@Before
    public void setUp() throws SQLException {
        manager = new PersonManagerImpl();
    }*/
    
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

        manager = new PersonManagerImpl();

        //manager.setDataSource(ds);
        conn0 = DriverManager.getConnection("jdbc:derby:memory:DirectoryManagerTest;create=true");
        manager.setConn(conn0);
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
    public void createPerson() {
        Person person = new PersonBuilder().setName("Fero").setSurname("Mrkvicka").build();
                //newPerson("Fero", "Mrkvicka");
        manager.createPerson(person);

        Long personId = person.getId();
        assertNotNull(personId);
        Person result = manager.findPersonById(personId);
        assertEquals(person, result);
        assertNotSame(person, result);
        assertDeepEquals(person, result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createPersonWithWrongArguments()
    {
        manager.createPerson(null);
    }
    
    @Test
    public void removePerson() 
    {
        Person p1 = new PersonBuilder().setName("Fero").setSurname("Mrkvicka").build();
                //newPerson("Fero", "Mrkvicka");
        Person p2 = new PersonBuilder().setName("Jozo").setSurname("Tekvicka").build();
                //newPerson("Jozo", "Tekvicka");
        manager.createPerson(p1);
        manager.createPerson(p2);
        
        assertNotNull(manager.findPersonById(p1.getId()));
        assertNotNull(manager.findPersonById(p2.getId()));

        manager.removePerson(p1);
        
        assertNull(manager.findPersonById(p1.getId()));
        assertNotNull(manager.findPersonById(p2.getId()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void removePersonWithNullArguments()
    {
        manager.removePerson(null);
    }
    
    @Test
    public void getAllPeople()
    {
        Person person = new PersonBuilder().setName("name").setSurname("surname").build();
        Person person2 = new PersonBuilder().setName("name2").setSurname("surname2").build();
        Person person3 = new PersonBuilder().setName("name3").setSurname("surname3").build();

        manager.createPerson(person);
        manager.createPerson(person2);
        manager.createPerson(person3);

        List<Person> expected = Arrays.asList(person, person2, person3);
        List<Person> result = manager.getAllPeople();

        assertNotNull(result);

        Collections.sort(result, personIdComparator);
        Collections.sort(expected, personIdComparator);

        assertEquals(expected, result);
        assertDeepEquals(expected, result);
    }
    
    @Test
    public void getAllPeopleFromEmptyDB()
    {
        List<Person> result = manager.getAllPeople();

        assertNotNull(result);
        assertEquals(Collections.EMPTY_LIST, result);        
    }
            
    private static Person newPerson(String name, String lastname) {
        Person person = new Person();
        person.setName(name);
        person.setLastName(lastname);
        return person;
    }
    
    private void assertDeepEquals(Person expected, Person actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getLastName(), actual.getLastName());
    }
 
    private static Comparator<Person> personIdComparator = new Comparator<Person>()
    {

        @Override
        public int compare(Person o1, Person o2)
        {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };

    private void assertDeepEquals(List<Person> expectedList, List<Person> actualList)
    {
        for (int i = 0; i < expectedList.size(); i++)
        {
            Person expected = expectedList.get(i);
            Person actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
}

class PersonBuilder
{
    private final Person person;
    
    public PersonBuilder()
    {
        person = new Person();
    }
    
    public Person build()
    {
        return person;
    }    
    
    public PersonBuilder setSurname(String surname)
    {
        person.setLastName(surname);
        return this;
    }
    
    public PersonBuilder setId(Long id)
    {
        person.setId(id);
        return this;
    }
    
    public PersonBuilder setName(String name)
    {
        person.setName(name);
        return this;
    }
}