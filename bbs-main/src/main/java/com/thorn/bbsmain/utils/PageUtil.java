package com.thorn.bbsmain.utils;

import com.thorn.bbsmain.exceptions.PageException;

import java.util.List;

/**
 * 分页
 */
public class PageUtil {
    /**
     * 分页
     *
     * @param list 待分页列表
     * @param page
     * @param step 每页显示多少
     * @return 一页的部分
     */
    public static List subList(List list, int page, int step) throws PageException {
        if (list == null || list.size() == 0) {
            return null;
        }
        int beginIndex = --page * step;
        int endIndex = beginIndex + step;
        endIndex = endIndex > list.size() ? list.size() : endIndex;
        if (endIndex <= beginIndex) {
            throw new PageException("获取消息：非法参数");
        }
        return list.subList(beginIndex, endIndex);
    }

    public static int getPage(List list, int step) {
        return list.size() / step + 1;
    }
}
