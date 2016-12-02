package cat.nyaa.utils.database;

import org.apache.commons.lang.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseDatabase {
    protected static class TableStructure<T> {
        enum ColumnType {
            TEXT,
            INTEGER,
            REAL;
            static ColumnType from(Class cls) {
                if (cls == int.class || cls == Integer.class) return INTEGER;
                if (cls == boolean.class || cls == Boolean.class) return INTEGER;
                if (cls == float.class || cls == Float.class) return REAL;
                if (cls == String.class) return TEXT;
                return null;
            }
        }

        private final Class<T> tableClass;
        private final String tableName;
        protected final List<String> columnNames = new ArrayList<>();
        protected final Map<String, Field> columnFields = new HashMap<>();
        protected final Map<String, Method> columnGetters = new HashMap<>();
        protected final Map<String, Method> columnSetters = new HashMap<>();
        protected final Map<String, ColumnType> columnTypes = new HashMap<>();
        protected final int primaryKeyIndex;

        TableStructure(String tableName, Class<T> tableClass) {
            this.tableName = tableName;
            this.tableClass = tableClass;
            int primKeyIdx = -1;
            // load all the fields
            for (Field f : tableClass.getDeclaredFields()) {
                DatabaseTable.Column columnAnnotation = f.getAnnotation(DatabaseTable.Column.class);
                if (columnAnnotation == null) continue;
                String name = columnAnnotation.name();
                if ("".equals(name)) name = f.getName();
                if (columnNames.contains(name)) throw new RuntimeException("Duplicated field column name: " + name);
                columnNames.add(name);
                f.setAccessible(true);
                columnFields.put(name, f);
                columnTypes.put(name, ColumnType.from(f.getType()));
                DatabaseTable.PrimaryKey primAnnotation = f.getAnnotation(DatabaseTable.PrimaryKey.class);
                if (primAnnotation == null) continue;
                if (primKeyIdx != -1) throw new RuntimeException("Duplicated primary key at: " + f.getName());
                primKeyIdx = columnNames.size() - 1;
            }

            // load all the getter/setter
            for (Method m : tableClass.getDeclaredMethods()) {
                DatabaseTable.Column columnAnnotation = m.getAnnotation(DatabaseTable.Column.class);
                if (columnAnnotation == null) continue;
                if (m.getName().startsWith("get")) {
                    String name = "".equals(columnAnnotation.name())? m.getName().substring(3): columnAnnotation.name();
                    if (columnNames.contains(name)) throw new RuntimeException("Duplicated getter column name: " + name);
                    String setterName = "set" + m.getName().substring(3);
                    Method setterMethod;
                    try {
                        setterMethod = tableClass.getDeclaredMethod(setterName, m.getReturnType());
                    } catch (NoSuchMethodException ex) {
                        throw new RuntimeException("setter not found: " + m.toString(), ex);
                    }
                    m.setAccessible(true);
                    setterMethod.setAccessible(true);
                    columnGetters.put(name, m);
                    columnSetters.put(name, setterMethod);
                    columnTypes.put(name, ColumnType.from(m.getReturnType()));
                    columnNames.add(name);
                } else if (m.getName().startsWith("set")) {
                    String name = "".equals(columnAnnotation.name())? m.getName().substring(3): columnAnnotation.name();
                    if (columnNames.contains(name)) throw new RuntimeException("Duplicated setter column name: " + name);
                    String getterName = "get" + m.getName().substring(3);
                    Method getterMethod;
                    try {
                        getterMethod = tableClass.getDeclaredMethod(getterName, void.class);
                        if (getterMethod.getReturnType() != m.getParameterTypes()[0])
                            throw new RuntimeException("getter return type mismatch: " + getterMethod.getName());
                    } catch (NoSuchMethodException ex) {
                        throw new RuntimeException("getter not found: " + m.toString(), ex);
                    }
                    m.setAccessible(true);
                    getterMethod.setAccessible(true);
                    columnGetters.put(name, getterMethod);
                    columnSetters.put(name, m);
                    columnTypes.put(name, ColumnType.from(getterMethod.getReturnType()));
                    columnNames.add(name);
                }
                DatabaseTable.PrimaryKey primAnnotation = m.getAnnotation(DatabaseTable.PrimaryKey.class);
                if (primAnnotation == null) continue;
                if (primKeyIdx != -1) throw new RuntimeException("Duplicated primary key at: " + m.getName());
                primKeyIdx = columnNames.size() - 1;
            }
            primaryKeyIndex = primKeyIdx;
        }

        String getCreateTableSQL() {
            String colStr = "";
            for (int i = 0;i<columnNames.size();i++) {
                if (i!=0) colStr+=",";
                String colName = columnNames.get(i);
                colStr+=String.format("%s %s NOT NULL", colName, columnTypes.get(colName).name());
                if (i==primaryKeyIndex) colStr+=" PRIMARY KEY";
            }
            return String.format("CREATE TABLE IF NOT EXISTS %s(%s)", tableName, colStr);
        }
    }

    protected final Map<String, TableStructure<?>> tables = new HashMap<>();
    protected final Map<Class<?>, String> tableName = new HashMap<>();

    protected abstract Class<?>[] getTables();
    /* auto commit should be set to `true` */
    protected abstract Connection getConnection();

    /**
     * Scan through the whole class for column annotations
     */
    protected BaseDatabase() {
        for (Class<?> tableClass : getTables()) {
            DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
            if (tableAnnotation == null) continue; // TODO warning
            String name = tableAnnotation.name();
            if ("".equals(name)) name = tableClass.getName();
            tables.put(name, new TableStructure<>(name, tableClass));
            tableName.put(tableClass, name);
        }
    }

    protected void createTables() {
        for (TableStructure<?> c : tables.values()) {
            createTable(c);
        }
    }

    protected void createTable(String name) {
        Validate.notNull(name);
        createTable(tables.get(name));
    }
    protected void createTable(Class<?> cls) {
        Validate.notNull(cls);
        createTable(tableName.get(cls));
    }

    protected void createTable(TableStructure<?> struct) {
        Validate.notNull(struct);
        String sql = struct.getCreateTableSQL();
        try {
            Statement smt = getConnection().createStatement();
            smt.executeUpdate(sql);
            smt.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
