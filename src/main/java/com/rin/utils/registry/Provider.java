package com.rin.utils.registry;

public interface Provider<T> {
    T get();
}