package impl;

public class test {
    public static void main(String[] args) {
        HotPostHandler handler = HotPostHandler.getSimpleHandler(new DefaultFetcher());
        Test test = new Test();
        test.test();
    }
}
