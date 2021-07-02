package edu.indiana.d2i.sloan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruili on 2/20/17.
 */
public class VMResultsResponseBean {

    private List<ResultInfoBean> res = new ArrayList<ResultInfoBean>();

    public VMResultsResponseBean(List<ResultInfoBean> res) {
        this.res = res;
    }

    public List<ResultInfoBean> getRes() {
        return res;
    }

    @Override
    public String toString() {
        return res.toString();
    }


}
