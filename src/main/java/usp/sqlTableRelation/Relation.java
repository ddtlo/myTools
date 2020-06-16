package usp.sqlTableRelation;

import com.alibaba.fastjson.JSON;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 记录一段sql中的表关系
 *
 */
public class Relation {
    private Map<String, TableRelation> tableRelationMap = new HashMap();//别名存在这里,不同的别名是不同的key指向同一个TableRelation
    private String tableNeedAlias;//用于别名,防止select后的as捣乱
    private List<String> afterON = new ArrayList<>();//on之后的连接字符串，在查询段结束后清空
    private Map<String,String> joinMap = new HashMap();//on之后的连接的表关系，在查询段结束后清空
    private boolean isJoin = false;

    private static Relation relation;

    public static Relation getRelation(){
        if(relation == null){
            relation = new Relation();
        }
        return relation;
    }

    public List<String> getAfterON() {
        return afterON;
    }

    public void setAfterON(List<String> afterON) {
        this.afterON = afterON;
    }

    public Map<String, String> getJoinMap() {
        return joinMap;
    }

    public void setJoinMap(Map<String, String> joinMap) {
        this.joinMap = joinMap;
    }

    public Map<String, TableRelation> getTableRelationMap() {
        return tableRelationMap;
    }

    public void setTableRelationMap(Map<String, TableRelation> tableRelationMap) {
        this.tableRelationMap = tableRelationMap;
    }

    public String getTableNeedAlias() {
        return tableNeedAlias;
    }

    public void setTableNeedAlias(String tableNeedAlias) {
        this.tableNeedAlias = tableNeedAlias;
    }

    public boolean isJoin() {
        return isJoin;
    }

    public void setJoin(boolean join) {
        isJoin = join;
    }

    public void setTable(String table){
        table=table.replace(" ","");
        if(tableRelationMap.get(table)==null){
            tableRelationMap.put(table,new TableRelation(table));
            return;
        }
    }

    public void setAlias(String alias){
        if(tableNeedAlias ==null)
            return;
        TableRelation tr = tableRelationMap.get(tableNeedAlias);
        if(tr == null){
            return;
        }
        tableRelationMap.put(alias,tr);
        tr.setAlias(alias);
        tableNeedAlias = null;
    }

    /**
     * 判断是否是表的列
     * 1.有"."
     * 2.不是true或false 必须
     * 3.不含有引号
     * 4.含英文字母 必须
     * 4 && 2 && (1||3)
     * @param str
     * @return
     */
    private boolean isTableJoin(String str){
        str = str.replace(" ","");
        boolean stop = str.contains(".");//点号
        boolean noKeepL = !str.toUpperCase().equals("TRUE") && !str.toUpperCase().equals("FALSE")&& !str.toUpperCase().equals("NULL");
        boolean noYinhao = !str.contains("'") && !str.contains("\"");
        boolean letter = false;//字母
        Pattern p = Pattern.compile("[a-zA-z]");
        if(p.matcher(str).find()) {
            letter = true;
        }

        if(letter&&noKeepL&(stop || noYinhao)){
            return true;
        }
        return false;
    }

    /**
     * 一段join关系结束,统计收集到的连接关系
     * 判断是否是表连接关系,分别判断等号的两边, 如果有一个点号,如果没有引号就代表单边是列,如果等号两边都是列,那就是表连接关系
     */
    public void endJoin(){
        for(String str :afterON){
            try {
                //连接符可能是等号也可能是 IS IN
                String link = "=";
                if(str.contains("IS")){
                    continue;
                }
                if(str.contains(" IN ")){
                    continue;
                }
                if(str.contains(" > ")){
                    continue;
                }
                if(str.contains(" < ")){
                    continue;
                }
                if(str.contains(" ISNOT ")){
                    continue;
                }
                String left = str.substring(0,str.indexOf(link));
                String right = str.substring(str.indexOf(link)+link.length(),str.length());
                if(!isTableJoin(left)||!isTableJoin(right)){
                    continue;
                }
                //为了简化操作,如果是那种不带表的列连接,直接把列当作表
                String leftTable = left;
                String rightTable = right;
                if(left.contains(".")){
                    leftTable = left.substring(0,left.indexOf("."));
                }
                if(right.contains(".")){
                    rightTable = right.substring(0,right.indexOf("."));
                }
                TableRelation trl = tableRelationMap.get(leftTable.replace(" ",""));
                TableRelation trr = tableRelationMap.get(rightTable.replace(" ",""));
                if(trl==null){
                    setTable(leftTable.replace(" ",""));
                }
                if(trr==null){
                    setTable(rightTable.replace(" ",""));
                }
                if(trl!=null){
                    trl.setRelation(tableRelationMap.get(rightTable.replace(" ","")).getTable(),str);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("解析失败sql:"+str);
            }
        }
        //清空临时数据
        afterON.clear();
        setJoin(false);
    }

    public void print(){
        Map<String,TableRelation> map = new HashMap<>();
        for(Map.Entry<String,TableRelation> entry:tableRelationMap.entrySet()){
            String msg = "";
            TableRelation tr = entry.getValue();
            if(entry.getValue().getRelation().size()>0){
                map.put(entry.getKey(),entry.getValue());
                msg = msg.concat(tr.getTable()).concat(":").concat("{");
                for(Map.Entry<String,String> en:tr.getRelation().entrySet()){
                    String msg3 = "(".concat(en.getKey()).concat(":").concat(en.getValue()).concat(")");
                    msg = msg.concat(msg3);
                }
                msg = msg.concat("}");
            }else{
                continue;
            }
            System.out.println(msg);
        }
        System.out.println(JSON.toJSON(map));
        System.out.println(JSON.toJSON(tableRelationMap));
    }
}
