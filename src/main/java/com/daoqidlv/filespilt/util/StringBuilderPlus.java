package com.daoqidlv.filespilt.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Description:
 * <p>
 * Date: 2018/09/27
 * Time: 11:08
 *
 * @author Shuanghe Yu
 */
public class StringBuilderPlus {

    private StringBuilder builder;

    public StringBuilderPlus() {
        builder = new StringBuilder();
    }

    public void setStringBuilder(StringBuilder sb) {
        this.builder = sb;
    }

    public StringBuilder getStringBuilder() {
        return builder;
    }

    public StringBuilderPlus append(String add) {
        if (StringUtils.isNotEmpty(add)) {
            builder.append(add);
        }
        return this;
    }

    public StringBuilderPlus deleteLastChar() {
        StringBuilder sb = this.getStringBuilder();
        if (sb != null && sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            setStringBuilder(sb);
        }
        return this;
    }

    /**
     * 清空
     *
     * @return
     */
    public StringBuilderPlus deleteAll() {
        //StringBuilder sb = this.getStringBuilder();
        //sb.delete(0, sb.length() - 1);
        //setStringBuilder(sb);
        //return this;

        if (builder.length() > 0) {
            setStringBuilder(builder.delete(0, builder.length()));
        }
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

}