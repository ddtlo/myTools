package usp.SqlParamReplace;


import java.util.ArrayList;
import java.util.List;

public class SqlAndParam {
    private List<String> sqls = new ArrayList<>();
    private List<String> params = new ArrayList<>();

    public List<String> getSqls() {
        return sqls;
    }

    public void setSqls(List<String> sqls) {
        this.sqls = sqls;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void setSql(String sql) {
        this.sqls.add(sql);
    }

    public void setParam(String param) {
        this.params.add(param);
    }


}
