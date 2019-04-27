package com.thorn.bbsmain.services.oa;

import com.thorn.bbsmain.utils.MsgBuilder;

import java.util.List;


public interface OAService<E> {
    List<E> getList(int page, String target, int limit);

    E getDetail(Object id);

    boolean update(E e);

    boolean delete(Object id);

    /**
     * 添加请求uri后缀到页面
     */
    MsgBuilder addData();
}
