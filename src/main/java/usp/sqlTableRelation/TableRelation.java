package usp.sqlTableRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableRelation {
    public TableRelation(String table) {
        this.table = table;
    }

    private String table;//主表
    private List<String> alias = new ArrayList<String>();//别名
    private Map<String,String> relation = new HashMap<>();

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Map<String, String> getRelation() {
        return relation;
    }

    public void setRelation(Map<String, String> relation) {
        this.relation = relation;
    }

    public void setRelation(String rTable,String relationStr) {
        this.relation.put(rTable,relationStr);
    }

    public void setAlias(String aliasStr){
        alias.add(aliasStr);
    }

    public boolean equalsTable(String tableName) {
        if(table.equals(tableName)){
            return true;
        }
        for(String aliasStr:alias){
            if(aliasStr.equals(tableName)){
                return true;
            }
        }
        return false;
    }
}
