/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package contactsdirectory.backend;

import java.sql.SQLException;
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
 * @author xribaric
 */
public class PersonManagerImplTest {
   
    private PersonManagerImpl manager;
    
    @Before
    public void setUp() throws SQLException {
        manager = new PersonManagerImpl();
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