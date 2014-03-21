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
class IllegalEntityException extends RuntimeException
{
    public IllegalEntityException()
    {
        super();
    }
    
    public IllegalEntityException(String msg)
    {
        super(msg);
    }
    
    public IllegalEntityException(String msg, Throwable t)
    {
        super(msg, t);
    }
    
    public IllegalEntityException(Throwable t)
    {
        super(t);
    }
}
