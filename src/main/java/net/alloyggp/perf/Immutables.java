package net.alloyggp.perf;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;


public class Immutables {
    private Immutables() {
        //Not instantiable
    }

    public static <T> Collector<T, ?, ImmutableList<T>> collectList() {
        return new Collector<T, ImmutableList.Builder<T>, ImmutableList<T>>() {
            @Override
            public Supplier<ImmutableList.Builder<T>> supplier() {
                return () -> ImmutableList.builder();
            }

            @Override
            public BiConsumer<ImmutableList.Builder<T>, T> accumulator() {
                return (builder, elem) -> builder.add(elem);
            }

            @Override
            public BinaryOperator<ImmutableList.Builder<T>> combiner() {
                return (b1, b2) -> b1.addAll(b2.build());
            }

            @Override
            public Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher() {
                return builder -> builder.build();
            }

            @Override
            public Set<java.util.stream.Collector.Characteristics> characteristics() {
                return ImmutableSet.of();
            }
        };
    }

    public static <T extends Comparable<T>> Collector<T, ?, ImmutableSortedSet<T>> collectSortedSet() {
        return new Collector<T, ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>>() {
            @Override
            public Supplier<ImmutableSortedSet.Builder<T>> supplier() {
                return () -> ImmutableSortedSet.naturalOrder();
            }

            @Override
            public BiConsumer<ImmutableSortedSet.Builder<T>, T> accumulator() {
                return (builder, elem) -> builder.add(elem);
            }

            @Override
            public BinaryOperator<ImmutableSortedSet.Builder<T>> combiner() {
                return (b1, b2) -> b1.addAll(b2.build());
            }

            @Override
            public Function<ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>> finisher() {
                return builder -> builder.build();
            }

            @Override
            public Set<java.util.stream.Collector.Characteristics> characteristics() {
                return ImmutableSet.of();
            }
        };
    }

    public static <T> Collector<T, ?, ImmutableSet<T>> collectSet() {
        return new Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>>() {
            @Override
            public Supplier<ImmutableSet.Builder<T>> supplier() {
                return () -> ImmutableSet.builder();
            }

            @Override
            public BiConsumer<ImmutableSet.Builder<T>, T> accumulator() {
                return (builder, elem) -> builder.add(elem);
            }

            @Override
            public BinaryOperator<ImmutableSet.Builder<T>> combiner() {
                return (b1, b2) -> b1.addAll(b2.build());
            }

            @Override
            public Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher() {
                return builder -> builder.build();
            }

            @Override
            public Set<java.util.stream.Collector.Characteristics> characteristics() {
                return ImmutableSet.of();
            }
        };
    }
}
