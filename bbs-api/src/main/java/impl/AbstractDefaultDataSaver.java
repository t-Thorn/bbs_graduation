package impl;

import interfaces.AbstractDataSaver;


public abstract class AbstractDefaultDataSaver extends AbstractDataSaver {

    /**
     * 必须利用compute方法实现动态更新
     * 保存到数据库 单条
     *
     * @param id 帖子id
     */
    @Override
    public abstract void save(Object id, Object hotPoint);
}
