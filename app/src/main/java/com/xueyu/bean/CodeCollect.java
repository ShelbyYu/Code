package com.xueyu.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by Shey on 2015/6/20 22:34.
 * Email:1768037936@qq.com
 */
@Table(name = "CodeCollect")
public class CodeCollect extends EntityBase{

    @Column(column = "codeId")
    private int codeId;

    @Column(column = "systemTime")
    private String systemtime;

    public int getCodeId() {
        return codeId;
    }

    public void setCodeId(int codeId) {
        this.codeId = codeId;
    }

    public String getSystemtime() {
        return systemtime;
    }

    public void setSystemtime(String systemtime) {
        this.systemtime = systemtime;
    }
}
