package fileBackup.monitoring.persistence;

import fileBackup.monitoring.DBError;
import io.vavr.control.Either;
import org.hibernate.Session;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Allows a sequence of transactions to be applied where 'transaction n' will only be executed is 'transaction n-1' is
 * successful. A failed transaction having Either.isLeft stops any further transactions running.
 *
 * Created by matt on 14-Jul-17.
 */
public class SequencedTransaction<T> {
    private LinkedList<Either<DBError, T>> transactionResults;

    public SequencedTransaction() {
        this.transactionResults = new LinkedList<>();
    }

    public SequencedTransaction<T> start(Function<Session, Supplier<T>> mapper) {
        transactionResults.push(Repository.runTransaction(mapper));
        return this;
    }

    /**
     * Only run transaction if the previous transaction did not have an error.
     */
    public SequencedTransaction<T> andThen(Function<Session, Supplier<T>> mapper) {
        Either<DBError, T> head = transactionResults.peek();
        if (head != null && head.isRight()) {
            transactionResults.push(Repository.runTransaction(mapper));
        }
        return this;
    }

    /**
     * Reduces all right values (successful transaction results) based on the reducer. If the last transaction was
     * an error then the last transaction is returned with no reducing being performed. Finally if there are no transactions
     * an empty Optional is returned.
     *
     * @param reducer How to reduce the right successful transaction values.
     * @return An {@code Optional} containing the result.
     */
    public Optional<Either<DBError, T>> reduceRight(BinaryOperator<T> reducer) {
        Either<DBError, T> head = transactionResults.peek();
        if (head != null && head.isLeft()) {
            return Optional.of(transactionResults.peek());
        }

        return transactionResults.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .reduce(reducer)
                .map(Either::right);
    }
}
