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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tomáš
 */
public class PersonManagerImpl implements PersonManager
{

    public static final Logger logger = Logger.getLogger(PersonManagerImpl.class.getName());

    private Connection conn;

    public void setConn(Connection conn)
    {
        this.conn = conn;
    }

    @Override
    public void createPerson(Person person) throws IllegalArgumentException
    {
        if (person == null)
        {
            throw new IllegalArgumentException("person is null");
        }
        if (person.getId() != null)
        {
            throw new IllegalArgumentException("person id is already set");
        }

        PreparedStatement st = null;
        try
        {
            //conn = dataSource.getConnection();

            //conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Person (name,surname) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, person.getName());
            st.setString(2, person.getLastName());

            int addedRows = st.executeUpdate();
            if (addedRows != 1)
            {
                throw new ServiceFailureException("Internal Error: More rows "
                        + "inserted when trying to insert person " + person);
            }

            //ResultSet keyRS = st.getGeneratedKeys();
            //person.setId(getKey(keyRS,person));
            Long id = DBUtilities.getId(st.getGeneratedKeys());
            person.setId(id);
        } catch (SQLException ex)
        {
            throw new ServiceFailureException("Error when inserting person " + person, ex);
        } finally
        {
            if (st != null)
            {
                try
                {
                    st.close();
                } catch (SQLException ex)
                {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void editPerson(Person person) throws IllegalArgumentException
    {
        if (person == null)
        {
            throw new IllegalArgumentException("person is null");
        }
        if (person.getId() != null)
        {
            throw new IllegalArgumentException("person id is already set");
        }

        try
        {

            try (PreparedStatement st = conn.prepareStatement("UPDATE person SET name = ?, surname = ?  WHERE id = ?"))
            {
                st.setString(1, person.getName());
                st.setString(2, person.getLastName());
                st.setLong(3, person.getId());

                int updated = st.executeUpdate();
                if (updated != 1)
                {
                    throw new ServiceFailureException("Internal Error: More rows "
                            + "inserted when trying to insert person " + person);
                }
            }

        } catch (SQLException ex)
        {
            String msg = "Error when updating person in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }

    }

    @Override
    public void removePerson(Person person) throws IllegalArgumentException
    {
        if (person == null)
        {
            throw new IllegalArgumentException("person is null");
        }
        if (person.getId() == null)
        {
            throw new IllegalArgumentException("person id is already set");
        }

        String sql = "DELETE FROM person WHERE id = ?";

        try (PreparedStatement st = conn.prepareStatement(sql))
        {
            st.setLong(1, person.getId());
            int removed = st.executeUpdate();

            if (removed != 1)
            {
                throw new ServiceFailureException("Internal Error: More rows "
                        + "deleted when trying to delete person " + person);
            }
        } catch (SQLException e)
        {
            String msg = "Error when removing contact from the db";
            logger.log(Level.SEVERE, msg, e);
            throw new ServiceFailureException(msg, e);
        }
    }

    @Override
    public Person findPersonById(Long id) throws IllegalArgumentException
    {
        PreparedStatement st = null;
        try
        {
            st = conn.prepareStatement(
                    "SELECT id, name, surname FROM person WHERE id = ?");
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next())
            {
                Person person = resultSetToPerson(rs);

                if (rs.next())
                {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                            + "(source id: " + id + ", found " + person + " and " + resultSetToPerson(rs));
                }

                return person;
            } else
            {
                return null;
            }

        } catch (SQLException ex)
        {
            throw new ServiceFailureException(
                    "Error when retrieving person with id " + id, ex);
        } finally
        {
            if (st != null)
            {
                try
                {
                    st.close();
                } catch (SQLException ex)
                {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public List<Person> getAllPeople()
    {
        List<Person> people = new ArrayList<>();

        //try(Connection conn = dataSource.getConnection())
        try(PreparedStatement st = conn.prepareStatement("SELECT id, name, surname FROM person");
                ResultSet rs = st.executeQuery())
        {
            while(rs.next())
            {
                people.add(resultSetToPerson(rs));
            }

        } catch (SQLException ex)
        {
            throw new ServiceFailureException("Error when retrieving all people from db.", ex);
        }
        
        if (people.size() == 0)
        {
            return Collections.EMPTY_LIST;
        } else
        {
            return people;
        }
    }

    private Long getKey(ResultSet keyRS, Person person) throws ServiceFailureException, SQLException
    {
        if (keyRS.next())
        {
            if (keyRS.getMetaData().getColumnCount() != 1)
            {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert person " + person
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next())
            {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert person " + person
                        + " - more keys found");
            }
            return result;
        } else
        {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert person " + person
                    + " - no key found");
        }
    }

    private Person resultSetToPerson(ResultSet rs) throws SQLException
    {
        Person person = new Person();
        person.setId(rs.getLong("id"));
        person.setName(rs.getString("name"));
        person.setLastName(rs.getString("surname"));
        return person;
    }
}
