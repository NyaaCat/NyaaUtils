package cat.nyaa.utils.database;

public enum ColumnType {
    TEXT,
    INTEGER, // use long in java
    REAL; //use double in java

    public static ColumnType from(Class cls) {
        if (cls == long.class || cls == Long.class) return INTEGER;
        if (cls == boolean.class || cls == Boolean.class) return INTEGER;
        if (cls == double.class || cls == Double.class) return REAL;
        if (cls == String.class) return TEXT;
        throw new IllegalArgumentException("Unsupported type");
    }

    /**
     * Convert a database object to java object.
     * Currently used for convert int to bool
     *
     * @param obj         the object returned from database: integer/float/string
     * @param desiredType long/double/string/boolean
     * @return
     */
    public static Object toSystemType(Object obj, Class desiredType) {
        if (obj == null) return null;
        if (desiredType == boolean.class || desiredType == Boolean.class) {
            if (obj instanceof Number) {
                return ((Number) obj).longValue() != 0;
            } else {
                throw new IllegalArgumentException("object cannot be parsed as boolean");
            }
        } else if (desiredType == long.class || desiredType == Long.class){
            return ((Number)obj).longValue();
        } else if (desiredType == double.class || desiredType == Double.class) {
            return ((Number)obj).doubleValue();
        } else if (desiredType == String.class) {
            return (String) obj;
        } else {
            throw new IllegalArgumentException("Unacceptable desiredType: " + desiredType.toString());
        }
    }

    /**
     * Convert to Database type
     * Current used for bool --> long
     */
    public Object toDatabaseType(Object obj) {
        if (obj == null) return null;
        switch (this) {
            case INTEGER: {
                Class cls = obj.getClass();
                if (cls == boolean.class || cls == Boolean.class) {
                    return (boolean) obj ? 1L : 0L;
                } else {
                    return obj;
                }
            }
            default:
                return obj;
        }
    }
}
