package usp.sqlTableRelation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单粗暴地搜索sql中的表关联关系，但是只能处理靠join关联的表
 * 示例解析select t1.a from tableA AS t1 left join t2 on t1.type="no" and t2.type="abc" and t1.id = t2.tid
 * 0.字符串处理,将left join等连接语句替换成@LJ等,方便解析
 * select t1.a from tableA AS t1 @LJ t2 on t1.type="no" and t2.type="abc" and t1.id = t2.tid
 * 1.找到from,开始获取连接sql
 * from tableA AS t1 @LJ t2 on t1.type="no" and t2.type="abc" and t1.id = t2.tid
 * 2.from后的第一个sql段必为表,将tableA存入relation,建立tableA的tableRelation,同时将tableNeedAlias赋值为tableA用于寻找别名
 * 同时将tableA存入tableSet,因为一个from后面的代码段是一大段连接关系,需要统一处理
 * tableA AS t1 @LJ t2 on t1.type="no" and t2.type="abc" and t1.id = t2.tid
 * 3.as后的第一个sql段是别名,将别名t1赋予tableNeedAlias中记录的表tableA,存入relation中的tableRelationMap,map中现在有两个KEY,
 * 对应同一个tableRelation
 * t1 @LJ t2 on t1.type="no" and t2.type="abc" and t1.id = t2.tid
 * 4.连接符后的第一个sql段必是表,也可能是别名,先在tableRelationMap中寻找,没找到则添加到tableRelationMap中
 * t2 on t1.type = "no" and t2.type = "abc" and t1.id = t2.tid
 * 5.on后的的三段sql必是条件或连接语句,通过判断"."的数量来区分,若有3个"."则是表连接条件,遇到and则继续这个判断,收集表连接语句
 * t1.type = "no" and t2.type = "abc" and t1.id = t2.tid
 * t2.type = "abc" and t1.id = t2.tid
 * t1.id = t2.tid
 * 6.将表连接语句存入afterON列表
 * 7.找到连接语句后继续寻找连接符或连接中止标志
 * 8.找到连接符后重复4到7步,如果找到中止标志(where,select)
 */
public class SqlTableReleation {
    private static final String fileSql = "D://1.txt";
    public static void main(String[] args) {

        //读取sql文件
        BufferedReader br = null;
        StringBuffer sb = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileSql), "UTF-8")); //这里可以控制编码
            sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String sql = new String(sb); //StringBuffer ==> String
        Relation relation = releation(sql);
        Set<TableRelation> set = new HashSet<>();
        for(TableRelation tr:relation.getTableRelationMap().values()){
            set.add(tr);
        }
        relation.print();
    }

    /**
     * 括号回车符换行符替换成空格
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll(" ");
        }
        dest = dest.replace("("," ");
        dest = dest.replace(")"," ");
        dest = dest.replace("IS NOT","ISNOT");
        dest = dest.toUpperCase();
        return dest;
    }
    private static Map<String,String> codeMap;
    public static Map<String,String> getCodeMap(){
        if (codeMap!=null&&codeMap.size()>0)
            return codeMap;

        codeMap = new HashMap();
        codeMap.put("INNER JOIN","@IJ");
        codeMap.put("LEFT JOIN","@LJ");
        codeMap.put("RIGHT JOIN","@RJ");
//        codeMap.put("GROUP BY","@GB");
//        codeMap.put("IS NULL","@IN");
//        codeMap.put("IS NOT NULL","@INN");
//        codeMap.put("ORDER BY","@OB");
        return codeMap;
    }

    /**
     * 1.标记INNER JOIN,LEFT JOIN,RIGHT JOIN
     * 2.按空格区分
     *
     * @param sql
     * @return
     */
    public static List<String> sqlList(String sql){
        sql = replaceBlank(sql);
        for(Map.Entry<String,String> entry:getCodeMap().entrySet()){
            sql = sql.replace(entry.getKey(),entry.getValue());
        }

        String[] sa = sql.split(" ");
        List<String> result = Arrays.asList(sa);
        return result;
    }

    public static boolean isJoin(String str){
        for(String join:getCodeMap().values()){
            if(str.equals(join)){
                return true;
            }
        }
        return false;
    }

    /**
     * 产生关系
     * 在from关键词后面找表并记录表名和别名
     * 在连接关键词后寻找连接表并记录连接关系
     *
     * from面的表必然是连接主表，但在多级关联中，被连表也会在之后变成连接主表
     * 连接关系开始于from 结束于：select ,where
     */
    public static Relation releation(String sql){
        Relation relation = Relation.getRelation();
        List<String> sqlList = sqlList(sql);
        Iterator<String> it =  sqlList.iterator();
        boolean isFrom = false;//from后必是表,切代表连接语句的开始
        boolean isAlias = false;//as后是别名
        boolean isRtable = false;//连接符后是连接表
        boolean isRelation = false;//on后是连接关系
        String joinCondition = "";
        int relationIndex = 0;
        int relationNum = 3;
        while (it.hasNext()){
            String str = it.next();
            System.out.println(str);
            //空字符串跳过
            if(str==null||str.replace(" ","").isEmpty()){
                continue;
            }
            //连接结束标志,处理连接数据
            if(str.toUpperCase().equals("WHERE")||str.toUpperCase().equals("SELECT")){
                if(relation.isJoin()){
                    System.out.println("没有开始连接就遇到了结束标志");
                    continue;
                }
                relation.endJoin();
            }
            str = str.replace("\"","");
            //存表,开始连接
            if(isFrom){
                if(relation.isJoin()){
                    System.out.println("没有结束上一段连接关系就遇到了from");
                    relation.endJoin();
                }
                relation.setJoin(true);

                relation.setTable(str);
                relation.setTableNeedAlias(str);
                isFrom = false;
                continue;
            }
            //存别名
            if(isAlias){
                relation.setAlias(str);
                isAlias = false;
                continue;

            }
            //存连接表
            if(isRtable){
                //连接表也需要存在连接主表中
                relation.setTable(str);
                relation.setTableNeedAlias(str);
                isRtable = false;
                continue;
            }
            //存连接关系，连接关系应该由3段sql组成
            if(isRelation){
                if(relationIndex++<relationNum){
                    joinCondition = joinCondition.concat(" ").concat(str);
                    continue;
                }
                relationIndex = 0;
                isRelation = false;
                relation.getAfterON().add(joinCondition);
                joinCondition="";
            }

            if(str.toUpperCase().equals("FROM")){
                isFrom=true;
                continue;
            }
            //as除了可能出现在表后也可能出现在select的结果列后面
            if(str.toUpperCase().equals("AS")){
                isAlias=true;
                continue;
            }
            if(isJoin(str)){
                isRtable=true;
                continue;
            }
            if(relation.isJoin()&&(str.toUpperCase().equals("ON")||str.toUpperCase().equals("AND"))){
                isRelation=true;
                continue;
            }
        }


        return relation;
    }

}
