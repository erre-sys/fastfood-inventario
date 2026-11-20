package ec.com.erre.fastfood.inventario;

import org.springframework.boot.SpringApplication;

public class TestInventarioApplication {

    public static void main(String[] args) {
        SpringApplication.from(InventarioApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
