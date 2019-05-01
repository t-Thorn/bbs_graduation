package com.thorn.bbsmain.services.oa;

import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyOAService implements OAService {
    @Override
    public List getList(int page, String target, int limit, int type, MsgBuilder builder) {
        return null;
    }

    @Override
    public Object getDetail(Object id) {
        return null;
    }

    @Override
    public boolean update(Object object) {
        return false;
    }

    @Override
    public boolean delete(Object id) {
        return false;
    }

    @Override
    public MsgBuilder addData() {
        return null;
    }
}
