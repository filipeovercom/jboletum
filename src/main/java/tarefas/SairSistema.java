package tarefas;

public class SairSistema implements Runnable{
    @Override
    public void run() {
        System.out.println("Saindo do sistema...");
        Runtime.getRuntime().exit(0);
    }
}
