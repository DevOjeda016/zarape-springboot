package org.utl.dsm.elzarape.config;

import org.utl.dsm.elzarape.model.*;
import org.utl.dsm.elzarape.repository.*;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Datos de demostración: admin inicial, catálogo de ejemplo y una sucursal. Solo inserta si no existen. */
@Component
@ConditionalOnProperty(name = "zarape.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final BranchRepository branches;
    private final PasswordEncoder encoder;
    private final String adminUser;
    private final String adminPassword;

    public DataSeeder(UserRepository users, CategoryRepository categories, ProductRepository products,
                      BranchRepository branches, PasswordEncoder encoder,
                      @Value("${zarape.seed.admin-username}") String adminUser,
                      @Value("${zarape.seed.admin-password}") String adminPassword) {
        this.users = users;
        this.categories = categories;
        this.products = products;
        this.branches = branches;
        this.encoder = encoder;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!users.existsByUsername(adminUser)) {
            AppUser a = new AppUser();
            a.setUsername(adminUser);
            a.setPasswordHash(encoder.encode(adminPassword));
            a.setRole(Role.ADMIN);
            a.setName("Administrador");
            a.setLastName("El Zarape");
            users.save(a);
            log.info("Usuario admin creado: {}", adminUser);
        }
        if (!users.existsByUsername("cocina")) {
            AppUser e = new AppUser();
            e.setUsername("cocina");
            e.setPasswordHash(encoder.encode("cocina123"));
            e.setRole(Role.EMPLOYEE);
            e.setName("Cocina");
            e.setLastName("Demo");
            users.save(e);
        }
        if (categories.count() == 0) {
            seedCatalog();
        }
        if (branches.count() == 0) {
            BranchOffice b = new BranchOffice();
            b.setName("El Zarape Centro");
            b.setAddress("Calle Principal 100, Col. Centro");
            b.setCity("León, Gto.");
            b.setHours("Lun-Dom 9:00 - 22:00");
            b.setLatitude(21.1236);
            b.setLongitude(-101.6859);
            b.setPhoto("/img/restaurant/default.png");
            branches.save(b);
        }
    }

    private void seedCatalog() {
        Category platillos = cat("Platillos", ProductType.FOOD);
        Category antojitos = cat("Antojitos", ProductType.FOOD);
        Category bebidas = cat("Bebidas", ProductType.DRINK);
        Category cervezas = cat("Cervezas y vinos", ProductType.DRINK);
        Category cafe = cat("Café y chocolate", ProductType.DRINK);

        prod(platillos, "Pozole", "Pozole rojo tradicional con guarnición", "pozole.jpg", "120.00");
        prod(platillos, "Chiles en nogada", "Chile poblano relleno con salsa de nogada", "chiles_nogada.jpg", "185.00");
        prod(platillos, "Mole poblano", "Pollo en mole poblano con arroz", "mole_poblano.jpg", "150.00");
        prod(platillos, "Cochinita pibil", "Cerdo marinado en achiote, estilo Yucatán", "cochinita_pibil.jpg", "140.00");
        prod(platillos, "Enchiladas", "Enchiladas rojas con queso y crema", "enchiladas.jpg", "110.00");
        prod(platillos, "Chilaquiles", "Chilaquiles verdes con pollo", "chilaquiles.jpg", "95.00");
        prod(antojitos, "Tacos al pastor", "Orden de 5 tacos con piña", "tacos_pastor.jpg", "85.00");
        prod(antojitos, "Quesadillas", "Quesadillas de queso Oaxaca", "quesadillas.jpg", "70.00");
        prod(antojitos, "Sopes", "Sopes con frijol, carne y salsa", "sopes.jpg", "65.00");
        prod(antojitos, "Tostadas", "Tostadas de tinga", "tostadas.jpg", "60.00");
        prod(antojitos, "Flautas", "Flautas doradas de pollo", "flautas.jpg", "75.00");
        prod(antojitos, "Elotes", "Elote preparado con mayonesa, queso y chile", "elotes.jpg", "35.00");
        prod(antojitos, "Tamales", "Tamales de rajas y de puerco", "tamales.jpg", "30.00");
        prod(antojitos, "Guacamole", "Guacamole fresco con totopos", "guacamole.jpg", "80.00");
        prod(antojitos, "Tamal de dulce", "Tamal dulce con pasas", "tamal_dulce.jpg", "25.00");
        prod(bebidas, "Coca-Cola", "Refresco 355 ml", "coca_cola.jpg", "30.00");
        prod(bebidas, "Agua mineral", "Agua mineral 355 ml", "agua_mineral.jpg", "28.00");
        prod(bebidas, "Limonada", "Limonada natural", "limonada.jpg", "35.00");
        prod(bebidas, "Jugo de naranja", "Jugo de naranja natural", "jugo_naranja.jpg", "40.00");
        prod(bebidas, "Jugo de lima", "Jugo de lima natural", "jugo_lima.jpg", "35.00");
        prod(cervezas, "Corona clara", "Cerveza clara 355 ml", "corona_clara.jpg", "45.00");
        prod(cervezas, "Corona oscura", "Cerveza oscura 355 ml", "corona_oscura.png", "50.00");
        prod(cervezas, "Piña colada", "Cóctel de piña colada", "piña_colada.jpg", "90.00");
        prod(cervezas, "Vino tinto", "Copa de vino tinto", "vino_tinto.jpg", "95.00");
        prod(cervezas, "Vino blanco", "Copa de vino blanco", "vino_blanco.jpg", "95.00");
        prod(cafe, "Café americano", "Café americano", "cafe_americano.jpg", "35.00");
        prod(cafe, "Capuchino", "Capuchino con espuma de leche", "capuchino.jpg", "50.00");
        prod(cafe, "Chocolate caliente", "Chocolate caliente de metate", "chocolate_caliente.jpg", "45.00");
    }

    private Category cat(String description, ProductType type) {
        Category c = new Category();
        c.setDescription(description);
        c.setType(type);
        return categories.save(c);
    }

    private void prod(Category category, String name, String description, String photo, String price) {
        Product p = new Product();
        p.setCategory(category);
        p.setName(name);
        p.setDescription(description);
        p.setPhoto("/img/products/" + photo);
        p.setPrice(new BigDecimal(price));
        products.save(p);
    }
}
