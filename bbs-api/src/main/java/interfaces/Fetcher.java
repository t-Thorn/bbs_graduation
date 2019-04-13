package interfaces;

/**
 * 信息提取
 */
public interface Fetcher<E> {
    E getInfo(int pid);

    Integer getID(E e);
}
