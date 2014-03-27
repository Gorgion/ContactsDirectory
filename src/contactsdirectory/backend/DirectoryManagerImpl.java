/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contactsdirectory.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Tomáš
 */
public class DirectoryManagerImpl implements DirectoryManager
{

    private static final Logger logger = Logger.getLogger(DirectoryManagerImpl.class.getName());

    private DataSource dataSource;

    public void setDataSource(DataSource ds)
    {
        dataSource = ds;
    }

    @Override
    public void addContactToPerson(Person person, Contact contact) throws IllegalArgumentException
    {
        checkDataSource();
        validatePerson(person);
        validateContact(contact);
        
        if(person.getId() == null)
        {
            throw new IllegalEntityException("person id is null");
        }

        if(contact.getId() == null)
        {
            throw new IllegalEntityException("contact id is null");
        }
        
        try(Connection conn = dataSource.getConnection())
        {
            try(PreparedStatement st = conn.prepareStatement("UPDATE contact SET personid = ? WHERE id = ?"))
            {
                st.setLong(1, person.getId());
                st.setLong(2, contact.getId());
                
                int updated = st.executeUpdate();
                if (updated != 1)
                {
                    throw new ServiceFailureException("Internal Error: More rows "
                            + "updated when trying to assign contact:" + contact +" to person:" + person);
                }
            }
        } catch (SQLException e)
        {
            String msg = "Error when assign contact to person in db";
            logger.log(Level.SEVERE, msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public void removeContactFromPerson(Person person, Contact contact) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Contact> findAllContactsOfPerson(Person person) throws IllegalArgumentException
    {
        List<Contact> contacts = new ArrayList<>();

        checkDataSource();
        validatePerson(person);
        if(person.getId() == null)
        {
            throw new IllegalEntityException("person id is null");
        }

        try (Connection conn = dataSource.getConnection())
        {
            try (PreparedStatement st = conn.prepareStatement("SELECT id FROM contact WHERE personid = ?"))
            {
                st.setLong(1, person.getId());
                try (ResultSet rs = st.executeQuery())
                {
                    ContactManager cm = new ContactManagerImpl();
                    cm.setDataSource(dataSource);

                    while (rs.next())
                    {
                        contacts.add(cm.getContact(rs.getLong("id")));
                    }
                    return contacts;
                }
            }
        } catch (SQLException e)
        {
            String msg = "Error when retreaving contacts from db";
            logger.log(Level.SEVERE, msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public Person findPersonWithContact(Contact contact) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void checkDataSource()
    {
        if (dataSource == null)
        {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    private static void validatePerson(Person person)
    {
        if (person == null)
        {
            throw new IllegalArgumentException("person is null");
        }

        if (person.getName() == null)
        {
            throw new ValidationException("person name is null");
        }

        if (person.getLastName() == null)
        {
            throw new ValidationException("person surname is null");
        }
    }

    private static void validateContact(Contact contact)
    {
        if (contact == null)
        {
            throw new IllegalArgumentException("contact is null");
        }
        if (contact.getType() == null)
        {
            throw new ValidationException("contact type is null");
        }
        if ((contact instanceof PhoneContact) && (((PhoneContact) contact).getPhoneNumber() == null))
        {
            throw new ValidationException("phone number is null");
        }
        if ((contact instanceof MailContact) && (((MailContact) contact).getMailAddress() == null))
        {
            throw new ValidationException("mail address is null");
        }
    }
}
