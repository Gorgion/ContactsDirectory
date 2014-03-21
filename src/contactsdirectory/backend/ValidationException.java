/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package contactsdirectory.backend;

/**
 *
 * @author Tomáš
 */
class ValidationException extends RuntimeException
{

    public ValidationException()
    {
    }

    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ValidationException(Throwable cause)
    {
        super(cause);
    }    
    
    public ValidationException(String msg)
    {
        super(msg);
    }
    
}
