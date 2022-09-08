package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tarefas.EmitirBoleto;
import tarefas.LiquidarBoletos;

@AllArgsConstructor
@Getter
public enum Menu {

    EMITIR_BOLETOS("Emitir boletos", new EmitirBoleto()),
    LIQUIDAR_BOLETOS("Liquidar boletos", new LiquidarBoletos()),
    SAIR_DO_SISTEMA("Sair do sistema", () -> System.out.println("Saindo do sistema...")),
    BOLETOS_PARA_LIQUIDAR("Lista", () -> System.out.println("Lista"));


    private String nome;
    private Runnable tarefa;

}