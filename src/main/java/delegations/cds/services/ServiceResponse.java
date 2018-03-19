package delegations.cds.services;

import java.util.function.Function;

import javax.ws.rs.BadRequestException;

public abstract class ServiceResponse<T> {

    private ServiceResponse() {
        // to prevent extension
    }

    public static <T> Error<T> forException(final Exception e) {
        return new Error<>(e, e.getMessage());
    }

    public static <T> Error<T> forException(final String message) {
        return ServiceResponse.forException(new BadRequestException(message));
    }

    public static Success<Void> forSuccess() {
        return new Success<>();
    }

    public static <T> Success<T> forSuccess(final T payload) {
        Success<T> sr = new Success<>();
        sr.setPayload(payload);
        return sr;
    }

    public Success<T> asSuccess() {
        return (Success<T>) this;
    }

    public Error<T> asError() {
        return (Error<T>) this;
    }

    public abstract boolean successful();

    public abstract <V> ServiceResponse<V> propagate();

    public static final class Success<T> extends ServiceResponse<T> {
        private Object id;
        private T payload;

        private Success() {
            this(null, null);
        }

        private Success(final Object id, final T payload) {
            this.id = id;
            this.payload = payload;
        }

        public <V> Success<V> propagate(final Function<T, V> transformPayload) {
            return new Success<>(id, transformPayload.apply(payload));
        }

        @Override
        public <V> Success<V> propagate() {
            return new Success<>(id, null);
        }

        // Get and Set Id are explicit here because the external identifiers
        // that clients of the service layer should use are not necessarily the underlying Ids.
        public Object getId() {
            return id;
        }

        public ServiceResponse<T> setId(final Object o) {
            id = o;
            return this;
        }

        public T payload() {
            return payload;
        }

        public Success<T> setPayload(final T payload) {
            this.payload = payload;
            return this;
        }

        @Override
        public boolean successful() {
            return true;
        }

        @Override
        public String toString() {
        	return String.format("Success [id=%s, payload=%s]", id, payload);
        }
    }

    public static final class Error<T> extends ServiceResponse<T> {
        private final String message;
        private final Exception exception;

        private Error(final Exception exception, final String message) {
            this.message = message;
            this.exception = exception;
        }

        /**
         * Converts an {@link Error} to a different generic type.
         *
         * @return
         */
        @Override
        public <E> Error<E> propagate() {
            return new Error<>(exception, message);
        }

        public Exception exception() {
            return exception;
        }

        public String message() {
            return message;
        }

        @Override
        public boolean successful() {
            return false;
        }

        @Override
        public String toString() {
        	return String.format("Error [message=%s, exception=%s]", message, exception);
        }
    }
}
