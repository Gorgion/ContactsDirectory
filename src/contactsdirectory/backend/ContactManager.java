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
    Contact createContact(Contact contact);
    void editContact(Contact contact);
    void removeContact(Contact contact);
    Contact findContactById(Long id);
}
