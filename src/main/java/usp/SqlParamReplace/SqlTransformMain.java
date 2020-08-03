package usp.SqlParamReplace;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 批量替换sql中的参数,txt1是sql,txt2是参数
 */
public class SqlTransformMain {

    private static final String fileSql = "D://1.txt";
    private static final String fileParam = "D://2.txt";

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
        System.out.println("txt1内容为==> " + s);

        //读取param文件
        BufferedReader br2 = null;
        StringBuffer sb2 = null;
        try {
            br2 = new BufferedReader(new InputStreamReader(new FileInputStream(fileParam), "UTF-8")); //这里可以控制编码
            sb2 = new StringBuffer();
            String line = null;
            while ((line = br2.readLine()) != null) {
                sb2.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        //参数数组
        String s2 = new String(sb2); //StringBuffer ==> String
//        System.out.println("txt2内容为==> " + s2);
//        String[] as2 = s2.split("], \\[");
//
//        //依次替换数组中的占位字段,从大到小替换
//        for(int i = as2.length ;i>0;i--){
//            if(as2[i-1]==null||as2[i-1].length()==0){
//                throw new RuntimeException("参数为空");
//            }
//            //去除参数中的key,括号,并将双引号改为单引号
//            String param = as2[i-1].substring(as2[i-1].indexOf(",")+1);
//            param=param.replace("\"","'");
//            param=param.replace("]","");
//            if(!s.contains("$"+i)){
//                throw new RuntimeException("没有找到第"+i+"个占位符");
//            }
//            s = s.replace("$"+i,param);
//        }
        s = replace(s,s2);
        if(s.contains("$")){
            throw new RuntimeException("sql中仍有未替换的占位符$");
        }
        System.out.println("最终sql:"+s);
    }

    public static String replace(String sql ,String paramS){
        //参数数组
        String[] params = paramS.split("], \\[");

        //依次替换数组中的占位字段,从大到小替换
        for(int i = params.length ;i>0;i--){
            if(params[i-1]==null||params[i-1].length()==0){
                throw new RuntimeException("参数为空");
            }
            //去除参数中的key,括号,并将双引号改为单引号
            String param = params[i-1].substring(params[i-1].indexOf(",")+1);
            param=param.replace("\"","'");
            param=param.replace("]","");
            if(!sql.contains("$"+i)){
                throw new RuntimeException("没有找到第"+i+"个占位符");
            }
            sql = sql.replace("$"+i,param);
        }
        if(sql.contains("$")){
            throw new RuntimeException("sql中仍有未替换的占位符$");
        }
        return sql;
    }

}