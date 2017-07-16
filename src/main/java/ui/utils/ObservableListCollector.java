package ui.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collect elements into a JavaFx {@code ObservableList}.
 *
 * Created by matt on 09-Jul-17.
 */
public class ObservableListCollector<A> implements Collector<A, ObservableList<A>, ObservableList<A>> {

    @Override
    public Supplier<ObservableList<A>> supplier() {
        return FXCollections::observableArrayList;
    }

    @Override
    public BiConsumer<ObservableList<A>, A> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<ObservableList<A>> combiner() {
        return (a, b) -> {
            a.addAll(b);
            return a;
        };
    }

    @Override
    public Function<ObservableList<A>, ObservableList<A>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.noneOf(Characteristics.class);
    }
}
