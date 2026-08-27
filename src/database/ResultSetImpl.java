package database;

import java.sql.Timestamp;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class ResultSetImpl implements AlyraResultSet {
    private Map<String, Object>[] data;
    private Object[][] values;
    private int indexData;
    
    @SuppressWarnings("unchecked")
    public ResultSetImpl(final ResultSet rs) throws Exception {
        this.indexData = -1;
        Statement stmt = null;
        
        try {
            if (rs == null) {
                throw new Exception("ResultSet cannot be null");
            }
            
            // Store statement reference để đóng sau
            stmt = rs.getStatement();
            
            final ResultSetMetaData rsmd = rs.getMetaData();
            final int nColumn = rsmd.getColumnCount();
            
            List<Map<String, Object>> listData = new ArrayList<>();
            List<Object[]> listValues = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> rowMap = new HashMap<>();
                Object[] rowValues = new Object[nColumn];
                for (int j = 1; j <= nColumn; ++j) {
                    final String tableName = rsmd.getTableName(j);
                    final String columnName = rsmd.getColumnName(j);
                    final Object columnValue = rs.getObject(j);
                    
                    // Store with column name (case insensitive)
                    rowMap.put(columnName.toLowerCase(), columnValue);
                    
                    // Store with table.column format if table name exists
                    if (tableName != null && !tableName.isEmpty()) {
                        rowMap.put(tableName.toLowerCase() + "." + columnName.toLowerCase(), columnValue);
                    }
                    
                    rowValues[j - 1] = columnValue;
                }
                listData.add(rowMap);
                listValues.add(rowValues);
            }
            
            this.data = listData.toArray(new Map[0]);
            this.values = listValues.toArray(new Object[0][0]);
        } catch (final Exception e) {
            throw new Exception("Error processing ResultSet: " + e.getMessage(), e);
        } finally {
            // Properly close resources
            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (final Exception ex) {
                // Log but don't throw
                System.err.println("Warning: Failed to close ResultSet: " + ex.getMessage());
            }
            
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
            } catch (final Exception ex) {
                // Log but don't throw
                System.err.println("Warning: Failed to close Statement: " + ex.getMessage());
            }
        }
    }
    
    @Override
    public void dispose() {
        if (this.data != null) {
            for (Map<String, Object> map : this.data) {
                if (map != null) {
                    map.clear();
                }
            }
            this.data = null;
        }
        
        if (this.values != null) {
            // Proper cleanup of 2D array
            for (int i = 0; i < this.values.length; i++) {
                if (this.values[i] != null) {
                    for (int j = 0; j < this.values[i].length; j++) {
                        this.values[i][j] = null;
                    }
                    this.values[i] = null;
                }
            }
            this.values = null;
        }
    }
    
    @Override
    public boolean next() throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        ++this.indexData;
        return this.indexData < this.data.length;
    }
    
    // FIX: Sửa lỗi logic của first() method
    @Override
    public boolean first() throws Exception {
        if (this.data == null || this.data.length == 0) {
            return false; // No data available
        }
        this.indexData = 0; // Move to first record
        return true;
    }
    
    // FIX: Sửa lỗi validation trong gotoResult
    @Override
    public boolean gotoResult(final int index) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (index < 0 || index >= this.data.length) {
            throw new Exception("Index out of bound: " + index + " (valid range: 0-" + (this.data.length - 1) + ")");
        }
        this.indexData = index;
        return true;
    }
    
    @Override
    public boolean gotoFirst() throws Exception {
        if (this.data == null || this.data.length == 0) {
            return false;
        }
        this.indexData = 0;
        return true;
    }
    
    @Override
    public void gotoBeforeFirst() {
        this.indexData = -1;
    }
    
    @Override
    public boolean gotoLast() throws Exception {
        if (this.data == null || this.data.length == 0) {
            return false;
        }
        this.indexData = this.data.length - 1;
        return true;
    }
    
    @Override
    public int getRows() throws Exception {
        if (this.data == null) {
            return 0;
        }
        return this.data.length;
    }
    
    // FIX: Add proper null handling and type safety for all getter methods
    @Override
    public byte getByte(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        try {
            return Byte.parseByte(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to byte");
        }
    }
    
    @Override
    public byte getByte(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        try {
            return Byte.parseByte(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to byte");
        }
    }
    
    @Override
    public int getInt(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to int");
        }
    }
    
    @Override
    public int getInt(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to int");
        }
    }
    
    @Override
    public float getFloat(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        if (value == null) {
            return 0.0f;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to float");
        }
    }
    
    @Override
    public float getFloat(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        if (value == null) {
            return 0.0f;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to float");
        }
    }
    
    @Override
    public double getDouble(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to double");
        }
    }
    
    @Override
    public double getDouble(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to double");
        }
    }
    
    @Override
    public long getLong(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to long");
        }
    }
    
    @Override
    public long getLong(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to long");
        }
    }
    
    @Override
    public String getString(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        return value == null ? null : String.valueOf(value);
    }
    
    @Override
    public String getString(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        return value == null ? null : String.valueOf(value);
    }
    
    @Override
    public Object getObject(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        return this.values[this.indexData][column - 1];
    }
    
    @Override
    public Object getObject(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        return this.data[this.indexData].get(column.toLowerCase());
    }
    
    // FIX: Improve boolean handling
    @Override
    public boolean getBoolean(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        if (value == null) {
            return false;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value instanceof String) {
            String str = ((String) value).toLowerCase().trim();
            return "true".equals(str) || "1".equals(str) || "yes".equals(str) || "y".equals(str);
        }
        
        return false;
    }
    
    @Override
    public boolean getBoolean(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        if (value == null) {
            return false;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value instanceof String) {
            String str = ((String) value).toLowerCase().trim();
            return "true".equals(str) || "1".equals(str) || "yes".equals(str) || "y".equals(str);
        }
        
        return false;
    }
    
    // FIX: Improve Timestamp handling
    @Override
    public Timestamp getTimestamp(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        if (value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) value).getTime());
        }
        if (value instanceof Long) {
            return new Timestamp((Long) value);
        }
        throw new Exception("Cannot convert " + value.getClass().getSimpleName() + " to Timestamp");
    }
    
    @Override
    public Timestamp getTimestamp(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        if (value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) value).getTime());
        }
        if (value instanceof Long) {
            return new Timestamp((Long) value);
        }
        throw new Exception("Cannot convert " + value.getClass().getSimpleName() + " to Timestamp");
    }
    
    @Override
    public short getShort(final int column) throws Exception {
        if (this.values == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.values[this.indexData][column - 1];
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        try {
            return Short.parseShort(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to short");
        }
    }
    
    @Override
    public short getShort(final String column) throws Exception {
        if (this.data == null) {
            throw new Exception("No data available");
        }
        if (this.indexData < 0 || this.indexData >= this.data.length) {
            throw new Exception("Cursor position invalid. Call next(), first(), or goto method first");
        }
        Object value = this.data[this.indexData].get(column.toLowerCase());
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        try {
            return Short.parseShort(value.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Cannot convert '" + value + "' to short");
        }
    }
    
    @Override
    public void close() {
        dispose();
    }
}