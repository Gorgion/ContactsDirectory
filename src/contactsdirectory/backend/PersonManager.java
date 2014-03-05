/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contactsdirectory.backend;

/**
 *
 * @author Tomáš
 */
public interface PersonManager {
    Person createPerson(Person person);
    void editPerson(Person person);
    void removePerson(Person person);
    Person findPersonById(Long id);    
}
