package impl;

import interfaces.Fetcher;

public class DefaultFetcher implements Fetcher {
    @Override
    public Object getInfo(int pid) {
        return new Integer(1);
    }

    @Override
    public Integer getID(Object o) {
        return 1;
    }
}
