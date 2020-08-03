package usp.SqlParamReplace;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 单页替换参数
 */
public class SqlTransformPlusMain {

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
        String s = new String(sb); //StringBuffer ==> String
        System.out.println("txt1内容为==> " + s);

        String array[][] = fenhang(s);
        for(String[] s2:array){
            String ss = replace(s2[0],s2[1]);
            System.out.println(ss);
        }
//        if (s.contains("$")) {
//            throw new RuntimeException("sql中仍有未替换的占位符$");
//        }
//        System.out.println("最终sql:" + s);
    }

    public static String[][] fenhang(String sql) {
        //按中括号分行
        List<String> fenhang = new ArrayList();
        int begin = 0;
        while(sql.contains("]]")){
            fenhang.add(sql.substring(begin,sql.indexOf("]]")+2));
            sql = sql.substring(sql.indexOf("]]")+2);
//            System.out.println("分行==> " + fenhang.get(fenhang.size()-1));
        }

        String result[][] = new String[fenhang.size()][];
        for (int i = 0; i < fenhang.size(); i++) {
            String hang = fenhang.get(i);
            result[i] = new String[2];
            result[i][0] = hang.substring(0,hang.indexOf("[["));
            result[i][1] = hang.substring(hang.indexOf("[["));
        }
        return result;
    }

    public static String replace(String sql, String paramS) {
        //参数数组
        String[] params = paramS.split("], \\[");

        //依次替换数组中的占位字段,从大到小替换
        for (int i = params.length; i > 0; i--) {
            if (params[i - 1] == null || params[i - 1].length() == 0) {
                throw new RuntimeException("参数为空");
            }
            //去除参数中的key,括号,并将双引号改为单引号
            String param = params[i - 1].substring(params[i - 1].indexOf(",") + 1);
            param = param.replace("\"", "'");
            param = param.replace("]", "");
            if (!sql.contains("$" + i)) {
                throw new RuntimeException("没有找到第" + i + "个占位符");
            }
            sql = sql.replace("$" + i, param);
        }
        if (sql.contains("$")) {
            throw new RuntimeException("sql中仍有未替换的占位符$");
        }
        return sql;
    }

}