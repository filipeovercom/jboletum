package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tarefas.EmitirBoleto;
import tarefas.LiquidarBoleto;
import tarefas.SairdoSistema;

@AllArgsConstructor
@Getter
public enum Menu {

    EMITIR_BOLETOS("Emitir boletos", new EmitirBoleto()),
    LIQUIDAR_BOLETOS("Liquidar boletos", new LiquidarBoleto()),
    SAIR_DO_SISTEMA("Sair do sistema", new SairdoSistema());

    private String nome;
    private Runnable tarefa;

}
