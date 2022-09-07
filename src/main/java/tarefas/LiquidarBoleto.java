package tarefas;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class LiquidarBoleto implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {
        return Map.of(
                "Boletos liquidados: ", String.valueOf(350)
        );
    }
}
