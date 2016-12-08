package cat.nyaa.utils.database;

import org.apache.commons.lang.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public abstract class BaseDatabase {

    protected class TableStructure<T> {

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
                    String name = "".equals(columnAnnotation.name()) ? m.getName().substring(3) : columnAnnotation.name();
                    if (columnNames.contains(name))
                        throw new RuntimeException("Duplicated getter column name: " + name);
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
                    String name = "".equals(columnAnnotation.name()) ? m.getName().substring(3) : columnAnnotation.name();
                    if (columnNames.contains(name))
                        throw new RuntimeException("Duplicated setter column name: " + name);
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
            for (int i = 0; i < columnNames.size(); i++) {
                if (i != 0) colStr += ",";
                String colName = columnNames.get(i);
                colStr += String.format("%s %s NOT NULL", colName, columnTypes.get(colName).name());
                if (i == primaryKeyIndex) colStr += " PRIMARY KEY";
            }
            return String.format("CREATE TABLE IF NOT EXISTS %s(%s)", tableName, colStr);
        }

        /* Only database acceptable objects can be returned: int/float/string */
        Map<String, Object> getColumnObjectMap(T obj, String... columns) throws ReflectiveOperationException {
            List<String> columnList = new ArrayList<>();
            Map<String, Object> objects = new HashMap<>();
            if (columns == null || columns.length == 0) {
                columnList.addAll(columnNames);
            } else {
                columnList.addAll(Arrays.asList(columns));
            }
            for (String colName : columnList) {
                if (columnFields.containsKey(colName)) {
                    Object columnValue = columnFields.get(colName).get(obj);
                    if (columnValue != null) objects.put(colName, columnTypes.get(colName).toDatabaseType(columnValue));
                }
                if (columnGetters.containsKey(colName)) {
                    Object columnValue = columnGetters.get(colName).invoke(obj);
                    if (columnValue != null) objects.put(colName, columnTypes.get(colName).toDatabaseType(columnValue));
                }
            }
            return objects;
        }

        T getObjectFromResultSet(ResultSet rs) throws ReflectiveOperationException, SQLException {
            T obj = tableClass.newInstance();
            for (String colName : columnNames) {
                Object colValue = rs.getObject(colName);
                if (columnFields.containsKey(colName)) {
                    Field f = columnFields.get(colName);
                    f.set(obj, ColumnType.toSystemType(colValue, f.getType()));
                }
                if (columnSetters.containsKey(colName)) {
                    Method m = columnSetters.get(colName);
                    m.invoke(obj, ColumnType.toSystemType(colValue, m.getParameterTypes()[0]));
                }
            }
            return obj;
        }
    }

    protected final Map<String, TableStructure<?>> tables = new HashMap<>();
    protected final Map<Class<?>, String> tableName = new HashMap<>();

    protected abstract Class<?>[] getTables();

    /* auto commit should be set to `true` */
    protected abstract Connection getConnection();

    public abstract void close();

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

    public <T> Query<T> query(Class<T> tableClass) {
        return new Query<>(tableClass);
    }

    public class Query<T> {
        private TableStructure<T> table;
        private Map<String, Object> whereClause = new HashMap<>();

        public Query(Class<T> tableClass) {
            if (!tableName.containsKey(tableClass)) throw new IllegalArgumentException("Unknown Table");
            if (!tables.containsKey(tableName.get(tableClass))) throw new IllegalArgumentException("Unknown Table");
            table = (TableStructure<T>) tables.get(tableName.get(tableClass));
        }


        public Query<T> whereEq(String columnName, Object obj) {
            return where(columnName, "=", obj);
        }

        /**
         * comparator can be any SQL comparator.
         * e.g. =, >, <
         */
        public Query<T> where(String columnName, String comparator, Object obj) {
            if (!table.columnNames.contains(columnName)) throw new IllegalArgumentException("Unknown Column Name");
            whereClause.put(columnName + comparator + "?", obj);
            return this;
        }

        /**
         * remove records matching the where clauses
         */
        public void delete() {
            String sql = "DELETE FROM " + table.tableName;
            List<Object> objects = new ArrayList<>();
            if (whereClause.size() > 0) {
                sql += " WHERE";
                for (Map.Entry e : whereClause.entrySet()) {
                    if (objects.size() > 0) sql += " AND";
                    sql += " " + e.getKey();
                    objects.add(e.getValue());
                }
            }
            try {
                PreparedStatement stmt = getConnection().prepareStatement(sql);
                int x = 1;
                for (Object obj : objects) {
                    stmt.setObject(x, obj);
                    //ColumnType.setPreparedStatement(stmt, x, obj);
                    x++;
                }
                stmt.execute();
                stmt.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * the where clauses are ignored
         *
         * @param object record to be inserted
         */
        public void insert(T object) {
            try {
                String sql = "INSERT INTO " + table.tableName + " VALUES(?";
                for (int i = 1; i < table.columnNames.size(); i++) sql += ",?";
                sql += ")";
                PreparedStatement stmt = getConnection().prepareStatement(sql);
                Map<String, Object> objMap = table.getColumnObjectMap(object);
                for (int i = 1; i <= table.columnNames.size(); i++) {
                    String colName = table.columnNames.get(i - 1);
                    if (!objMap.containsKey(colName)) {
                        stmt.setNull(i, Types.NULL);
                    } else {
                        stmt.setObject(i, objMap.get(colName));
                    }
                }
                stmt.execute();
                stmt.close();
            } catch (SQLException | ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }


        public List<T> select() {
            String sql = "SELECT * FROM " + table.tableName;
            List<Object> objects = new ArrayList<>();
            if (whereClause.size() > 0) {
                sql += " WHERE";
                for (Map.Entry e : whereClause.entrySet()) {
                    if (objects.size() > 0) sql += " AND";
                    sql += " " + e.getKey();
                    objects.add(e.getValue());
                }
            }
            try {
                PreparedStatement stmt = getConnection().prepareStatement(sql);
                int x = 1;
                for (Object obj : objects) {
                    stmt.setObject(x, obj);
                    x++;
                }
                List<T> results = new ArrayList<T>();
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    T obj = table.getObjectFromResultSet(rs);
                    results.add(obj);
                }
                stmt.close();
                return results;
            } catch (SQLException | ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        public T selectUnique() {
            List<T> results = select();
            if (results.size() < 1) throw new RuntimeException("SQL Selection has no result");
            if (results.size() > 1) throw new RuntimeException("SQL Selection result is not unique");
            return results.get(0);
        }

        /**
         * Update record according to the where clauses
         *
         * @param obj     new values for columns
         * @param columns columns need to be updated, update all columns if empty
         */
        public void update(T obj, String... columns) {
            try {
                List<String> updatedColumns = new ArrayList<>();
                Map<String, Object> newValues = table.getColumnObjectMap(obj, columns);
                if (columns == null || columns.length <= 0) {
                    updatedColumns.addAll(table.columnNames);
                } else {
                    for (String col : columns) {
                        if (!table.columnNames.contains(col))
                            throw new IllegalArgumentException("Unknown Column Name: " + col);
                    }
                    updatedColumns.addAll(Arrays.asList(columns));
                }

                List<Object> parameters = new ArrayList<>();
                String sql = "UPDATE " + table.tableName + " SET ";
                for (int i = 0; i < updatedColumns.size(); i++) {
                    if (i > 0) sql += ",";
                    sql += updatedColumns.get(i) + "=?";
                    parameters.add(newValues.get(updatedColumns.get(i)));
                }

                boolean firstClause = true;
                if (whereClause.size() > 0) {
                    sql += " WHERE";
                    for (Map.Entry e : whereClause.entrySet()) {
                        if (!firstClause) sql += " AND";
                        firstClause = false;
                        sql += " " + e.getKey();
                        parameters.add(e.getValue());
                    }
                }

                PreparedStatement stmt = getConnection().prepareStatement(sql);
                int idx = 1;
                for (Object o : parameters) {
                    if (o == null) {
                        stmt.setNull(idx, Types.NULL);
                    } else {
                        stmt.setObject(idx, o);
                    }
                    idx++;
                }
                stmt.execute();
                stmt.close();
            } catch (ReflectiveOperationException | SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
