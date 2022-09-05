package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tarefas.EmitirBoleto;
import tarefas.LiquidarBoleto;

@AllArgsConstructor
@Getter
public enum Menu {

    EMITIR_BOLETOS("Emitir boletos", new EmitirBoleto()),
    LIQUIDAR_BOLETOS("Liquidar boletos", new LiquidarBoleto()),
    SAIR_DO_SISTEMA("Sair do sistema", () -> System.out.println("Saindo do sistema..."));

    private String nome;
    private Runnable tarefa;

  //  public static void main(String[] args) {
  //      String s = "0bb7afa2-a7e1-4e59-a5bd-2e21731c38102022111022862456922852587420000;OK";
  //      System.out.println(s.substring(0,36));
  //      System.out.println(s.substring(36,44));
  //      System.out.println(s.substring(44,48));
  //      System.out.println(s.substring(48,53));
  //      System.out.println(s.substring(53,57));
  //      System.out.println(s.substring(57,62));
  //      System.out.println(s.substring(62,65));
  //      System.out.println(s.substring(65,67));
  //
  //  }
}
