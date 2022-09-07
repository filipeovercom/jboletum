package tarefas;

public class SairdoSistema implements Runnable{
    @Override
    public void run() {
        System.out.println("Saindo do Sistema...");
        Runtime.getRuntime().exit(0);
    }
}
