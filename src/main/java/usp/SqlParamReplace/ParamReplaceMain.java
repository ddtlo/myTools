package usp.SqlParamReplace;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamReplaceMain {
    private static final String fileSql = "/home/chl/txt1";
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
        String s = new String(sb); //StringBuffer ==> String
//        System.out.println("txt1内容为==> " + s);
        SqlAndParam saq = breakSql(s);
        String finalSql = "";
        for(int i = 0;i<saq.getSqls().size();i++){
            String sql = SqlTransformMain.replace(saq.getSqls().get(i),saq.getParams().get(i));

            finalSql = finalSql.concat(sql+";"+"\n");
        }

        System.out.println(finalSql);
    }



    /**
     * 分解原始sql
     * 原始sql中含有大量无用的数据,这里采用直接获得sql和参数的办法,无视其他的东西
     * select开始到[之前是sql,[[到]]的是参数
     * 分别存储sql和参数
     */
    private static SqlAndParam breakSql(String oSql){
        SqlAndParam sap = new SqlAndParam();
        //转小写,方便查找关键词
        oSql = oSql.toLowerCase();
        //替换字符
        Pattern p = Pattern.compile("\t|\r|\n");
        Matcher m = p.matcher(oSql);
        oSql = m.replaceAll(" ");
        while (oSql.indexOf("select")>=0){
            //去除select之前的所有字符串
            oSql = oSql.substring(oSql.indexOf("select"));
            //截取到[之前,存入sql
            sap.setSql(oSql.substring(0,oSql.indexOf("[")));
            oSql = oSql.substring(oSql.indexOf("["));
            //截取到]]之后,存入param
            sap.setParam(oSql.substring(0,oSql.indexOf("]]")+2));
            oSql = oSql.substring(oSql.indexOf("]]")+2);
        }

        return sap;
    }

}
