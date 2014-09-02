/*
 * ADPDataType.java
 *
 * Created on December 13, 2004, 1:32 PM
 *   -- copied from ConDoor's CDDataType
 */

/**
 * Generic data type . . . to expose toString, toVector, and fieldNames functions
 *
 * @version 13-Dec-2004
 * @author gcb
 */

import java.util.*;

public abstract class ADPDataType {
    
    /** Creates a new instance of CDDataType */
    public ADPDataType() {
    }
    
    /** @return string rendition of object */
    public String toString() {
        return null;
    }
    
    /** @return components of object packed into a Vector */
    public Vector toVector() {
        return null;
    }
    
    /** @return array of names of components of object */
    public String[] fieldNames() {
        return null;
    }
    
}
