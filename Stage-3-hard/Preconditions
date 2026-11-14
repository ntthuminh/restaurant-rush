public final class Preconditions {
    private Preconditions() {
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new InvalidActionException(message);
        }
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}