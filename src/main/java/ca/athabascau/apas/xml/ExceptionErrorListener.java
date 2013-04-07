/**
 * This file is part of the au-xml-util package
 *
 * Copyright Trenton D. Adams <trenton daught d daught adams at gmail daught ca>
 * 
 * au-xml-util is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * au-xml-util is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with au-xml-util.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * See the COPYING file for more information.
 */
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
