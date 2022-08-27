package models;

import java.util.function.Function;

@FunctionalInterface
public interface ToUpperCase<T> {
    
    T apply(T a, T b, T c, T d);
}
