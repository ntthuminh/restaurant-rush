public interface InputSource extends AutoCloseable {
    String readLine();

    default boolean echoesInput() {
        return false;
    }

    @Override
    default void close() {
    }
}