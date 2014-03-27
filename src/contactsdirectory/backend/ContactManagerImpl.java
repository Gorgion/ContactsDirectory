/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package contactsdirectory.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Tomáš
 */
public class ContactManagerImpl implements ContactManager
{

    private static final Logger logger = Logger.getLogger(ContactManagerImpl.class.getName());

    //private Connection conn;
    private DataSource dataSource;
    
    public void setDataSource(DataSource ds)
    {
        dataSource = ds;
    }

    @Override
    public void createContact(Contact contact) throws IllegalArgumentException
    {
        checkDataSource();
        validateContact(contact);

        if (contact.getId() != null)
        {
            throw new IllegalArgumentException("contact id is already set");
        }

        //Connection conn = null;
        //PreparedStatement st = null;
        //PreparedStatement st2 = null;
        try (Connection conn = dataSource.getConnection())//(PreparedStatement st)
        {
            //conn.setAutoCommit(false);
            Long id;
            int addedRows;

            try (PreparedStatement st = conn.prepareStatement("INSERT INTO Contact (type,note) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS);)
            {
                st.setInt(1, DBUtilities.contactTypeToInt(contact.getType()));
                st.setString(2, contact.getNote());

                addedRows = st.executeUpdate();
                if (addedRows != 1)
                {
                    //conn.setAutoCommit(true);
                    throw new ServiceFailureException("Internal Error: More rows "
                            + "inserted when trying to insert contact " + contact);
                }

                id = DBUtilities.getId(st.getGeneratedKeys());
                contact.setId(id);
            }

            switch (contact.getType())
            {
                case MAIL:
                    try (PreparedStatement st = conn.prepareStatement("INSERT INTO mailcontact (contactid, mailaddress) VALUES (?,?)",
                            Statement.RETURN_GENERATED_KEYS);)
                    {
                        st.setLong(1, id);
                        st.setString(2, ((MailContact) contact).getMailAddress());
                        addedRows = st.executeUpdate();
                    }
                    break;
                case PHONE:
                    try (PreparedStatement st = conn.prepareStatement("INSERT INTO phonecontact (contactid, phonenumber) VALUES (?,?)",
                            Statement.RETURN_GENERATED_KEYS);)
                    {
                        st.setLong(1, id);
                        st.setString(2, ((PhoneContact) contact).getPhoneNumber());
                        addedRows = st.executeUpdate();
                    }
                    break;
            }

            if (addedRows != 1)
            {
                //conn.setAutoCommit(true);
                throw new ServiceFailureException("Internal Error: More rows "
                        + "inserted when trying to insert typed contact" + contact);
            }

            //conn.commit();
            //conn.setAutoCommit(true);            
        } catch (SQLException e)
        {
            String msg = "Error when inserting contact into db";
            logger.log(Level.SEVERE, msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public void editContact(Contact contact) throws IllegalArgumentException
    {
        checkDataSource();
        validateContact(contact);
        if (contact.getId() == null)
        {
            throw new IllegalEntityException("contact id is null");
        }

        try (Connection conn = dataSource.getConnection())
        {
            //conn.setAutoCommit(false);
            try (PreparedStatement st = conn.prepareStatement("SELECT type FROM contact WHERE id = ?"))
            {
                st.setLong(1, contact.getId());
                try (ResultSet rs = st.executeQuery())
                {
                    if (rs.next())
                    {
                        ContactType storedType = DBUtilities.intToContactType(rs.getInt("type"));

                        if (storedType != contact.getType())
                        {
                            throw new IllegalEntityException("contact type cannot be changed, only note can be changed");
                        }

                        if (rs.next())
                        {
                            throw new ServiceFailureException(
                                    "Internal error: More entities with the same id found " + contact.getId());
                        }
                    }
                }
            }
            try (PreparedStatement st = conn.prepareStatement("UPDATE contact SET note = ? WHERE id = ?"))
            {
                st.setString(1, contact.getNote());
                st.setLong(2, contact.getId());

                int updated = st.executeUpdate();
                if (updated != 1)
                {
                    throw new ServiceFailureException("Internal Error: More rows "
                            + "inserted when trying to insert contact: " + contact);
                }
            }
            //conn.commit();
        } catch (SQLException ex)
        {
            String msg = "Error when updating contact in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }

    @Override
    public void removeContact(Contact contact) throws IllegalArgumentException
    {
        checkDataSource();
        validateContact(contact);

        if (contact.getId() == null)
        {
            throw new IllegalEntityException("contact id is null");
        }

        try// (Connection conn = dataSource.getConnection())
        {
            switch (contact.getType())
            {
                case MAIL:
                    removeContactFromTypeTable(contact, "mailcontact");
                    break;
                case PHONE:
                    removeContactFromTypeTable(contact, "phonecontact");
                    break;
            }

            removeContactFromGeneralTable(contact);
        } catch (SQLException e)
        {
            String msg = "Error when removing contact from the db";
            logger.log(Level.SEVERE, msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public Contact getContact(Long id) throws IllegalArgumentException
    {
        checkDataSource();
        validateId(id);

        //PreparedStatement st = null;
        //PreparedStatement st2 = null;
        Contact contact = null;
        try (Connection conn = dataSource.getConnection())
        {

            try (PreparedStatement st = conn.prepareStatement(
                    "SELECT id, type, note FROM contact WHERE id = ?");)
            {
                st.setLong(1, id);
                try (ResultSet rsContact = st.executeQuery();)
                {
                    if (rsContact.next())
                    {
                        String sql = null;
                        switch (DBUtilities.intToContactType(rsContact.getInt("type")))
                        {
                            case MAIL:
                                //st2 = conn.prepareStatement(
                                sql = "SELECT mailaddress FROM MAILCONTACT WHERE contactid = ?";
                                //st2.setLong(1, id);
                                break;
                            case PHONE:
                                //st2 = conn.prepareStatement(
                                sql = "SELECT phonenumber FROM phonecontact WHERE contactid = ?";
                                //st2.setLong(1, id);
                                break;
                        }
                        try (PreparedStatement st2 = conn.prepareStatement(sql);)
                        {
                            st2.setLong(1, id);
                            try (ResultSet rsType = st2.executeQuery();)
                            {
                                if (rsType.next())
                                {
                                    contact = resultSetToContact(rsContact, rsType);

                                    if (rsType.next())
                                    {
                                        throw new ServiceFailureException(
                                                "Internal error: More entities with the same id found "
                                                + "(source id: " + id + ", found " + contact + " and " + resultSetToContact(rsContact, rsType));
                                    }
                                }

                                if (rsContact.next())
                                {
                                    throw new ServiceFailureException(
                                            "Internal error: More entities with the same id found "
                                            + "(source id: " + id + ", found " + contact + " and " + resultSetToContact(rsContact, rsType));
                                }
                            }
                        }
                    }
                }
            }
            return contact;
        } catch (SQLException ex)
        {
            throw new ServiceFailureException(
                    "Error when retrieving contact with id " + id, ex);
        }
    }

    private void checkDataSource()
    {
        if (dataSource == null)
        {
            throw new IllegalStateException("DataSource is not set");
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

    private Contact resultSetToContact(ResultSet rsContact, ResultSet rsType) throws SQLException
    {
        Contact contact = null;

        switch (DBUtilities.intToContactType(rsContact.getInt("type")))
        {
            case MAIL:
                contact = new MailContact();
                contact.setType(ContactType.MAIL);
                ((MailContact) contact).setMailAddress(rsType.getString("mailaddress"));
                break;
            case PHONE:
                contact = new PhoneContact();
                contact.setType(ContactType.PHONE);
                ((PhoneContact) contact).setPhoneNumber(rsType.getString("phonenumber"));
                break;
        }
        contact.setId(rsContact.getLong("id"));
        contact.setNote(rsContact.getString("note"));
        return contact;
    }

    private void validateId(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("contact id is null");
        }

        if (id <= 0)
        {
            throw new IllegalArgumentException("contact id is out of range");
        }
    }

    private void removeContactFromGeneralTable(Contact contact) throws SQLException
    {
        String sql = "DELETE FROM contact WHERE id = ?";

        try (Connection conn = dataSource.getConnection())
        {
            try (PreparedStatement st = conn.prepareStatement(sql))
            {
                st.setLong(1, contact.getId());
                int removed = st.executeUpdate();

                if (removed != 1)
                {
                    throw new ServiceFailureException("Internal Error: More rows "
                            + "deleted when trying to delete contact " + contact);
                }
            }
        }
    }

    private void removeContactFromTypeTable(Contact contact, String table) throws SQLException
    {
        validateTable(table);
        String sql = "DELETE FROM " + table + " WHERE contactid = ?";

        try (Connection conn = dataSource.getConnection())
        {
            try (PreparedStatement st = conn.prepareStatement(sql))
            {
                st.setLong(1, contact.getId());
                int removed = st.executeUpdate();

                if (removed != 1)
                {
                    throw new ServiceFailureException("Internal Error: More rows "
                            + "deleted when trying to delete contact " + contact);
                }
            }
        }
    }

    private void validateTable(String table) throws SQLException
    {
        switch(table)
        {
            case "phonecontact":
            case "mailcontact":
                return;
            default:
                throw new SQLException("Illegal table was selected for record deletion.");
        }
    }
}
