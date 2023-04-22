package bsds.server;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class represents an operation on a key-value store.
 */
public class Operation implements Serializable {

    /**
     * The type of the operation.
     */
    public enum OpType {
        GET, PUT, DELETE
    }

    /**
     * The type of this operation.
     */
    public final OpType type;

    /**
     * The arguments of this operation.
     */
    public final String[] args;

    /**
     * Creates a new operation with the given type and arguments.
     *
     * @param type the type of the operation
     * @param args the arguments of the operation
     */
    private Operation(OpType type, String... args) {
        this.type = type;
        this.args = args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return type == operation.type && Arrays.equals(args, operation.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    /**
     * Creates a new GET operation with the given key.
     *
     * @param key the key to get
     * @return a new GET operation
     */
    public static Operation GET(String key) {
        return new Operation(OpType.GET, key);
    }

    /**
     * Creates a new PUT operation with the given key and value.
     *
     * @param key   the key to put
     * @param value the value to put
     * @return a new PUT operation
     */
    public static Operation PUT(String key, String value) {
        return new Operation(OpType.PUT, key, value);
    }

    /**
     * Creates a new DELETE operation with the given key.
     *
     * @param key the key to delete
     * @return a new DELETE operation
     */
    public static Operation DELETE(String key) {
        return new Operation(OpType.DELETE, key);
    }
}
