package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tarefas.EmitirBoleto;
import tarefas.LiquidarBoleto;
import tarefas.SairSistema;

import java.util.Map;
import java.util.function.Supplier;

@AllArgsConstructor
@Getter
public enum Menu {

    EMITIR_BOLETOS("Emitir boletos", new EmitirBoleto()),
    LIQUIDAR_BOLETOS("Liquidar boletos", new LiquidarBoleto()),
    SAIR_DO_SISTEMA("Sair do sistema", new SairSistema());

    private String nome;
    private Runnable tarefa;
}
