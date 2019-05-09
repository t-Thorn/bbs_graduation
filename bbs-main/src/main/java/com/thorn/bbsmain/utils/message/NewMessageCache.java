package com.thorn.bbsmain.utils.message;

import java.util.concurrent.ConcurrentHashMap;

public class NewMessageCache {
    //保存用户管道，uid：channel
    private static ConcurrentHashMap<Integer, MessageObject> msgs = new ConcurrentHashMap<>();

    /**
     * 同步保证了不会被二次消费
     *
     * @param uid
     * @return
     */
    public static synchronized MessageObject getAndReset(int uid) {
        MessageObject msg = msgs.get(uid);
        remove(uid);
        return msg;
    }

    public static void put(int uid, MessageObject msg) {
        msgs.put(uid, msg);
    }

    public static void updateAnnouncement(int uid, String msg) {
        //用于公告加入消息，
        msgs.compute(uid, (k, v) -> {
            if (v == null) {
                v = new MessageObject();
                v.setUid(uid);
            }
            //公告采取覆盖策略
            v.setContent(msg);
            return v;
        });
    }

    public static void increase(int uid) {
        //用于公告加入消息，
        msgs.compute(uid, (k, v) -> {
            if (v == null) {
                v = new MessageObject();
                v.setUid(uid);
                v.setNum(1);
            }
            v.setNum(v.getNum() + 1);
            return v;
        });
    }

    public static void remove(int uid) {
        msgs.remove(uid);

    }
}
