package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tarefas.EmitirBoleto;

@AllArgsConstructor
@Getter
public enum Menu {

    EMITIR_BOLETOS("Emitir boletos", new EmitirBoleto()),
    LIQUIDAR_BOLETOS("Liquidar boletos", () -> System.out.println("Liquidar Boletos")),
    SAIR_DO_SISTEMA("Sair do sistema", () -> System.out.println("Saindo do sistema..."));

    private String nome;
    private Runnable tarefa;
}
