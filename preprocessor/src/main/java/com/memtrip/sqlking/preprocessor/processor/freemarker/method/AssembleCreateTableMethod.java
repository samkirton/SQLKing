package com.memtrip.sqlking.preprocessor.processor.freemarker.method;

import com.memtrip.sqlking.preprocessor.processor.data.Column;
import com.memtrip.sqlking.preprocessor.processor.data.ForeignKey;
import com.memtrip.sqlking.preprocessor.processor.data.IndexColumn;
import com.memtrip.sqlking.preprocessor.processor.data.Index;
import com.memtrip.sqlking.preprocessor.processor.data.Table;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssembleCreateTableMethod implements TemplateMethodModelEx {

    private static final String ASSEMBLE_CREATE_TABLE = "assembleCreateTable";

    private static final String SQL_TEXT = "text";
    private static final String SQL_INTEGER = "integer";
    private static final String SQL_LONG = "long";
    private static final String SQL_REAL = "real";
    private static final String SQL_BLOB = "blob";

    public static Map<String, Object> getMethodMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(ASSEMBLE_CREATE_TABLE, new AssembleCreateTableMethod());
        return map;
    }

    private AssembleCreateTableMethod() {

    }

    /**
     * Build a create table statement based on the provided tableName and members
     * @param	table	The table that the statement will create
     * @return	A SQL statement that will create a table
     */
    private String buildCreateTableStatement(Table table, List<Table> tables)
        {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE ");
        sb.append(table.getName());
        sb.append(" (");

        for (int i = 0; i < table.getColumns().size(); i++)
            {
            Column column = table.getColumns().get(i);

            if (!column.isJoinable(tables))
                {
                sb.append(column.getName())
                    .append(" ")
                    .append(getSQLDataTypeFromClassRef(column.getType()));

                if (column.hasPrimaryKey())
                    {
                    sb.append(" PRIMARY KEY");
                    if (column.hasAutoIncrement())
                        {
                        sb.append(" AUTOINCREMENT");
                    }
                }

                sb.append(",");
            }
        }

        List<ForeignKey> foreignKeyList = table.getForeignKeys();
        for (ForeignKey foreignKey : foreignKeyList)
            {
            StringBuilder sb_FK = new StringBuilder();
            sb_FK.append(table.getName() + " - Foreign Key:  ")
                       .append( foreignKey.getForeignTableName())
                       .append(" ")
                       .append( foreignKey.getLocalColumnNames().toString().replaceAll("[\\[\\]]", ""))
                       .append( " References ")
                       .append( foreignKey.getForeignColumnNames().toString().replaceAll("[\\[\\]]", ""));
            System.out.println("\"" + sb_FK.toString() + "\"");
            sb.append(" FOREIGN KEY(")
                    .append(foreignKey.getLocalColumnNames().toString().replaceAll("[\\[\\]]", ""))
                    .append(") REFERENCES ")
                    .append(foreignKey.getForeignTableName())
                    .append("(")
                    .append(foreignKey.getForeignColumnNames().toString().replaceAll("[\\[\\]]", ""))
                    .append("),");
        }

        sb.deleteCharAt(sb.length() - 1);

        sb.append(");");

        return "\"" + sb.toString() + "\"";
    }

    /**
     * Determine the data type of the provided class reference and return
     * the associated SQL data type
     * @param	value	The class reference
     * @return	The SQL data type to return
     */
    private String getSQLDataTypeFromClassRef(String value) {
        switch (value) {
            case "java.lang.String":
                return SQL_TEXT;
            case "long":
                return SQL_LONG;
            case "int":
                return SQL_INTEGER;
            case "boolean":
                return SQL_INTEGER;
            case "double":
                return SQL_REAL;
            case "byte[]":
                return SQL_BLOB;
            default:
                return ""; // TODO: foreign key object
        }
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        Object tableNameValue = arguments.get(0);
        Object tablesValue = arguments.get(1);

        Table table;
        if (tableNameValue instanceof StringModel) {
            StringModel stringModel = (StringModel)tableNameValue;
            table = (Table)stringModel.getAdaptedObject(Table.class);
        } else {
            throw new IllegalStateException("The assembleCreateTable argument must be type of " +
                    "com.memtrip.sqlking.preprocessor.processor.data.Table");
        }

        List<Table> tables = Util.getTables(tablesValue);

        return buildCreateTableStatement(table, tables);
    }
}