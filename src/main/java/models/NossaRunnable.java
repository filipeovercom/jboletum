package models;

@FunctionalInterface 
public interface NossaRunnable extends Runnable { 
    
    default void print() {
        System.out.println("Nome errado!");
    }
}
