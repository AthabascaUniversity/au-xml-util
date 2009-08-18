package ca.athabascau.apas.xml;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * Created :  May 15, 2009 1:52:06 PM MST
 * <p/>
 * Modified : $Date$
 * <p/>
 * Revision : $Revision$
 *
 * @author trenta
 */
public class HandleMap
{
    /**
     * @param map   the map to add to
     * @param key   the key to put
     * @param value the value to put
     *
     * @return the value put into the map, for use in the XSLT
     */
    public static Object put(Map map, Object key, Object value)
    {
        map.put(key, value);
        return value;
    }

    public static Object get(Map map, Object key)
    {
        return map.get(key);
    }
}
