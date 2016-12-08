package cat.nyaa.utils.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum ColumnType {
    TEXT,
    INTEGER,
    REAL; //use double in java

    public static ColumnType from(Class cls) {
        if (cls == int.class || cls == Integer.class) return INTEGER;
        if (cls == boolean.class || cls == Boolean.class) return INTEGER;
        if (cls == double.class || cls == Double.class) return REAL;
        if (cls == String.class) return TEXT;
        return null;
    }

    public static void setPreparedStatement(PreparedStatement stat, int index, Object obj) throws SQLException {
        Class cls = obj.getClass();
        if (cls == int.class || cls == Integer.class) {
            stat.setInt(index, (int) obj);
            return;
        }
        if (cls == boolean.class || cls == Boolean.class) {
            stat.setInt(index, ((boolean) obj) ? 1 : 0);
            return;
        }
        if (cls == double.class || cls == Double.class) {
            stat.setFloat(index, (float) obj);
            return;
        }
        if (cls == String.class) {
            stat.setString(index, (String) obj);
            return;
        }
        throw new IllegalArgumentException("Unknown object type: " + obj.getClass().toString());
    }

    /**
     * Convert a database object to java object.
     * Currently used for convert int to bool
     *
     * @param obj         the object returned from database: integer/float/string
     * @param desiredType integer/float/string/boolean
     * @return
     */
    public static Object toSystemType(Object obj, Class desiredType) {
        if (obj == null) return null;
        if ((desiredType == boolean.class || desiredType == Boolean.class) && obj.getClass() == Integer.class) {
            return (Integer) obj != 0;
        } else {
            return obj;
        }
    }

    /**
     * Convert between database object and java object.
     * Currently used for int <--> bool
     */
    public Object toDatabaseType(Object obj) {
        if (obj == null) return null;
        switch (this) {
            case INTEGER: {
                Class cls = obj.getClass();
                if (cls == boolean.class || cls == Boolean.class) {
                    return (boolean) obj ? 1 : 0;
                } else {
                    return obj;
                }
            }
            default:
                return obj;
        }
    }
}
