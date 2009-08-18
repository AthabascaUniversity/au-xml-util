package ca.athabascau.apas.xml;

import org.apache.log4j.Logger;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * Throws a TransformerException when an error occurs during XSL
 * transformations. The default behaviour of JAXP is to just print to stderr.
 * <p/>
 * Created :  Jul 30, 2009 4:21:00 PM MST
 * <p/>
 * Modified : $Date$
 * <p/>
 * Revision : $Revision$
 *
 * @author trenta
 */
public class ExceptionErrorListener implements ErrorListener
{
    private static final Logger logger =
        Logger.getLogger(ExceptionErrorListener.class);

    public void warning(TransformerException exception)
        throws TransformerException
    {
        logger.warn("xml warning: ", exception);
    }

    public void error(TransformerException exception)
        throws TransformerException
    {
        throw exception;
    }

    public void fatalError(TransformerException exception)
        throws TransformerException
    {
        throw exception;
    }
}
