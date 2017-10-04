package nl.juraji.biliomi.utility.estreams;

import nl.juraji.biliomi.utility.estreams.einterface.*;
import nl.juraji.biliomi.utility.estreams.types.EStreamAssertionFailedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static nl.juraji.biliomi.utility.estreams.types.RethrowEInterface.*;

/**
 * Created by robin.
 * september 2016
 */
@SuppressWarnings("unused")
public interface EStream<T, E extends Exception> {

  Stream<T> stream();

  static <T, E extends Exception> EStream<T, E> from(T[] array) {
    return from(Arrays.stream(array));
  }

  static <T, E extends Exception> EStream<T, E> from(Collection<T> list) {
    return from(list.stream());
  }

  static <T, E extends Exception> EStream<T, E> from(Iterable<T> iterable) {
    return from(StreamSupport.stream(iterable.spliterator(), false));
  }

  static <T, E extends Exception> EStream<T, E> from(Stream<T> stream) {
    return () -> stream;
  }

  default EStream<T, E> filter(EPredicate<? super T, E> predicate) {
    return from(stream().filter(predicate(predicate)));
  }

  /**
   * Perform an assertion using a predicate
   *
   * @param predicate The predicate to test
   * @param message A message to use as exception message
   * @return The current stream
   * @throws EStreamAssertionFailedException When the predicate tests to false
   */
  default <R extends String> EStream<T, E> assertion(EPredicate<T, E> predicate, EFunction<T, R, E> message) throws EStreamAssertionFailedException {
    return from(stream().map(function(t -> {
      if (!predicate(predicate).test(t)) {
        throw new EStreamAssertionFailedException(message.apply(t));
      }
      return t;
    })));
  }

  default void close() {
    stream().close();
  }

  default <R> EStream<R, E> map(EFunction<? super T, ? extends R, E> function) {
    return from(stream().map(function(function)));
  }

  default <R> EStream<R, E> flatMap(EFunction<? super T, ? extends Stream<? extends R>, E> function) {
    return from(stream().flatMap(function(function)));
  }

  default EStream<T, E> sorted(EComparator<? super T, E> comparator) {
    return from(stream().sorted(comparator(comparator)));
  }

  default EStream<T, E> peek(EConsumer<? super T, E> consumer) {
    return from(stream().peek(consumer(consumer)));
  }

  default Optional<T> reduce(EBinaryOperator<T, E> binaryOperator) {
    return stream().reduce(binaryOperator(binaryOperator));
  }

  default void forEach(EConsumer<? super T, E> consumer) {
    stream().forEach(consumer(consumer));
  }

  default void forEachOrdered(EConsumer<? super T, E> consumer) {
    stream().forEachOrdered(consumer(consumer));
  }

  default <R> R collect(ESupplier<R, E> supplier, EBiConsumer<R, ? super T, E> accumulator, EBiConsumer<R, R, E> combiner) {
    return stream().collect(
        supplier(supplier),
        biConsumber(accumulator),
        biConsumber(combiner)
    );
  }

  default <R, A> R collect(Collector<T, A, R> collector) {
    return stream().collect(collector);
  }

  default <K, U> Map<K, U> collectToMap(EFunction<T, K, E> keyMapper, EFunction<T, U, E> valueMapper) {
    return stream().collect(Collectors.toMap(
        function(keyMapper),
        function(valueMapper)
    ));
  }

  default T[] toArray(IntFunction<T[]> generator) {
    return stream().toArray(generator);
  }

  default <V> EBiStream<T, V, E> mapToBiEStream(EFunction<T, V, E> valueMapper) {
    return EBiStream.from(stream(), valueMapper);
  }

  default <K, V> EBiStream<K, V, E> mapToBiEStream(EFunction<T, K, E> keyMapper, EFunction<T, V, E> valueMapper) {
    return EBiStream.from(stream(), keyMapper, valueMapper);
  }

  default Optional<T> min(EComparator<? super T, E> comparator) {
    return stream().min(comparator(comparator));
  }

  default Optional<T> max(EComparator<? super T, E> comparator) {
    return stream().max(comparator(comparator));
  }

  default boolean anyMatch(EPredicate<? super T, E> predicate) {
    return stream().anyMatch(predicate(predicate));
  }

  default boolean allMatch(EPredicate<? super T, E> predicate) {
    return stream().allMatch(predicate(predicate));
  }

  default boolean noneMatch(EPredicate<? super T, E> predicate) {
    return stream().noneMatch(predicate(predicate));
  }

  default Optional<T> findFirst() {
    return stream().findFirst();
  }

  default Optional<T> findAny() {
    return stream().findAny();
  }
}
