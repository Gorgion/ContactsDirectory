/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contactsdirectory.backend;

/**
 *
 * @author Tomáš
 */
public interface ContactManager {
    void createContact(Contact contact);
    void editContact(Contact contact);
    void removeContact(Contact contact);
    Contact getContact(Long id);
}
