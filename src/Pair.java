public class Pair<T, U> {
    public T getKey() {
        return key;
    }

    public U getValue() {
        return value;
    }

    private final T key;
    private final U value;

    public Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }
}
