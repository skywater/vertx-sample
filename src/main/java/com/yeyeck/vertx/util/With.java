
/**
 * Project Name: fast-flowable-pure-api
 * File Name: With.java
 * @date 2021年11月2日 上午11:26:43
 * Copyright (c) 2021 jpq.com All Rights Reserved.
 */

package com.yeyeck.vertx.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * TODO <br/>
 * @date 2021年11月2日 上午11:26:43
 * @author jpq
 * @version
 */
public class With<T> {

    private static final With<?> EMPTY = new With<>(null);
    private final T value;
    private With(T value) {
        this.value = value;
    }
    @SafeVarargs
	public static <R> With<R[]> ofs(R... values) {
    	if(null == values) {
    		return empty();
    	}
    	Object[] arr = Arrays.stream(values).filter(e -> null != e).toArray();
    	if(arr.length > 0) {
    		R[] newArray = (R[]) Array.newInstance(arr[0].getClass(), arr.length);
    		System.arraycopy(arr, 0, newArray, 0, arr.length);
            return new With<>(newArray);
    	}
        return empty();
    }
    public static <T> With<T> of(T value) {
        return new With<>(value);
    }
    public static <T> With<T> of(T value, T defVal) {
        return new With<>(null != value ? value : defVal);
    }
    public static <T> With<T> of(T value, Supplier<T> defVal) {
        Objects.requireNonNull(defVal);
        return new With<>(null != value ? value : defVal.get());
    }
    @SuppressWarnings("unchecked")
    public static<T> With<T> empty() {
        return (With<T>) EMPTY;
    }
    public T get() {
        return value;
    }
    public boolean isNull() {
        return value == null;
    }
    public void accept(Consumer<? super T> consumer) {
        if (value != null)
            consumer.accept(value);
    }
    public With<T> filter(Predicate<? super T> predicate) {
        return filter(predicate, () -> null);
    }
    public With<T> filter(Predicate<? super T> predicate, Supplier<T> defVal) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(defVal);
        if (isNull())
            return of(defVal.get());
        else
            return predicate.test(value) ? this : of(defVal.get());
    }
    @Deprecated
    public With<T> filterBlank() {
        return filterBlank(() -> null);
    }
    @Deprecated
    public With<T> filterBlank(Supplier<T> defVal) {
        if (isNull())
            return new With<>(defVal.get());
        else
            return !StringUtils.isBlank(value.toString()) ? this : new With<>(defVal.get());
    }
    @Deprecated
    public With<T> filterEmpty() {
        return filterEmpty(() -> null);
    }
    @Deprecated
    public With<T> filterEmpty(Supplier<T> defVal) {
        Objects.requireNonNull(defVal);
        if (isNull())
            return new With<>(defVal.get());
        else if(value instanceof Collection)
            return !((Collection<?>) value).isEmpty() ? this : new With<>(defVal.get());
        else if(value.getClass().isArray()) 
            return Array.getLength(value) > 0 ? this : new With<>(defVal.get());
        return this;
    }
    public With<T> filter() {
        return filter(() -> null);
    }
    public With<T> filter(Supplier<T> defVal) {
        Objects.requireNonNull(defVal);
        if (isNull())
            return new With<>(defVal.get());
        else if(value instanceof String)
        	return !StringUtils.isBlank(value.toString()) ? this : new With<>(defVal.get());
        else if(value instanceof Collection)
            return !((Collection<?>) value).isEmpty() ? this : new With<>(defVal.get());
        else if(value instanceof Map)
            return !((Map<?,?>) value).isEmpty() ? this : new With<>(defVal.get());
        else if(value.getClass().isArray()) 
            return Array.getLength(value) > 0 ? this : new With<>(defVal.get());
        return this;
    }
    public <U> With<U> map(Function<? super T, U> mapper) {
        Objects.requireNonNull(mapper);
        return isNull() ? empty() : With.of(mapper.apply(value)).filter();
    }
    @Deprecated
    public <U> With<U> flatMap(Function<? super T, With<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isNull())
            return empty();
        else {
            return mapper.apply(value).filter();
        }
    }

    /**
     * 新增方法：处理对象 <br/>
     * @author jpq
     * @param exec
     * @return
     */
    public With<T> exec(Consumer<T> exec) {
        Objects.requireNonNull(exec);
        if (isNull())
            return empty();
        else {
        	exec.accept(value);
            return this;
        }
    }
    public <R> With<T> exec(BiConsumer<T, R> exec, R r) {
        Objects.requireNonNull(exec);
        if (isNull())
            return empty();
        else if(null == r)
            return this;
    	exec.accept(value, r);
        return this;
    }
    public <R> With<T> exec(BiConsumer<T, R> exec, R r, Supplier<R> defVal) {
        Objects.requireNonNull(defVal);
        return exec(exec, null == r ? defVal.get() : r);
    }
    public <R> With<T> exec(BiConsumer<T, R> exec, R r, R defVal) {
        return exec(exec, null == r ? defVal : r);
    }
    public With<T> execStr(BiConsumer<T, String> exec, String r) {
        return execStr(exec, r, () -> null);
    }
    public With<T> execStr(BiConsumer<T, String> exec, String r, Supplier<String> defVal) {
        Objects.requireNonNull(defVal);
        return execStr(exec, r, StringUtils.isBlank(r) ? defVal.get() : null);
    }
    public With<T> execStr(BiConsumer<T, String> exec, String r, String defVal) {
        return exec(exec, StringUtils.isBlank(r) ? StringUtils.defaultIfBlank(defVal, null) : r);
    }
    
    /**
     * 处理对象内部参数 <br/>
     * @author jpq
     * @param func
     * @param exec
     * @return
     */
    public <R> With<T> exec(Function<T, R> func, BiConsumer<R, T> exec) {
        Objects.requireNonNull(func);
        Objects.requireNonNull(exec);
        if (isNull())
            return empty();
        With.of(func.apply(value)).exec(exec, value);
        return this;
    }
    
    /**
     * 平滑转换为 Stream <br/>
     * @author jpq
     * @param <U>
     * @param mapper
     * @return
     */
    public <U> Stream<U> stream(Function<? super T, Stream<U>> mapper) {
        Objects.requireNonNull(mapper);
        return isNull() ? Stream.empty() : mapper.apply(value);
    }
    /**
     * 保持原 Stream <br/>
     * @author jpq
     * @param <U>
     * @param mapper
     * @return
     */
    public <U> Stream<U> toStream(Function<? super T, Stream<U>> mapper) {
        Objects.requireNonNull(mapper);
        return mapper.apply(value);
    }
    public <U, K> With<K> streamMap(Function<? super T, Stream<U>> mapper, Function<Stream<U>, K> func) {
        Objects.requireNonNull(func);
        return of(func.apply(stream(mapper)));
    }
    public <U> With<T> streamExec(Function<? super T, Stream<U>> mapper, Consumer<Stream<U>> func) {
        Objects.requireNonNull(func);
        func.accept(stream(mapper));
        return this;
    }
    
    public T orElse(T other) {
        return value != null ? value : other;
    }
    public T orElseGet(Supplier<? extends T> other) {
        return value != null ? value : other.get();
    }
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
            return false;
        }

        With<?> other = (With<?>) obj;
        return Objects.equals(value, other.value);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    @Override
    public String toString() {
        return value != null ? String.format("With[%s]", value) : "With.empty";
    }
    
    public static void main(String[] args) {
    	String r = "ddd";
    	Supplier<String> defVal = () -> "aa";
    	String str1 = StringUtils.isBlank(r) ? StringUtils.defaultIfBlank(defVal.get(), "fff111") : r;
    	System.out.println(str1);
    	String str2 = StringUtils.defaultIfBlank(defVal.get(), "fff222");
    	System.out.println(str2);
    	System.out.println("----------");
    	Integer[] newInstance = (Integer[]) Array.newInstance(Integer.class, 1);
    	System.out.println(newInstance.length);
    	System.out.println(newInstance[0]);
		int[] arr = new int[] {};
		arr = null;
		System.out.println(With.ofs(null, null, 2, 5, 6).map(e -> e.length).get());
	}
}