/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package contactsdirectory.backend;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Tomáš
 */
public class DBUtilities
{
    /**
     * Returns id from given ResultSet
     * 
     * @param rs ResultSet with id
     * @return id from given ResultSet
     * @throws SQLException when operation fails
     */
    public static Long getId(ResultSet rs) throws SQLException {
        if (rs.getMetaData().getColumnCount() != 1) {
            throw new IllegalArgumentException("Given ResultSet contains more columns");
        }
        if (rs.next()) {
            Long result = rs.getLong(1);
            if (rs.next()) {
                throw new IllegalArgumentException("Given ResultSet contains more rows");
            }
            return result;
        } else {
            throw new IllegalArgumentException("Given ResultSet contain no rows");
        }
    }
    
    public static int contactTypeToInt(ContactType type)
    {        
        switch(type)
        {
            case MAIL: return 1;
            case PHONE: return 2;
            default: return -1;
        }
    }
    
    public static ContactType intToContactType(int i)
    {        
        switch(i)
        {
            case 1: return ContactType.MAIL;
            case 2: return ContactType.PHONE;
            default: return null;
        }
    }
}
