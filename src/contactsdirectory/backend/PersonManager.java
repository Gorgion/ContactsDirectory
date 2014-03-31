/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contactsdirectory.backend;

import java.util.List;

/**
 *
 * @author Tomáš
 */
public interface PersonManager {
    void createPerson(Person person);
    void editPerson(Person person);
    void removePerson(Person person);
    Person findPersonById(Long id);
    List<Person> getAllPeople();
}
