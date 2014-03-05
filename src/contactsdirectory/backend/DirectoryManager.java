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
public interface DirectoryManager {
    void addContactToPerson(Person person, Contact contact);
    void removeContactFromPerson(Person person, Contact contact);
    List<Contact> findAllContactsOfPerson(Person person);
    Person findPersonWithContact(Contact contact);
}
