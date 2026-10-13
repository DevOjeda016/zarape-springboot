package org.utl.dsm.elzarape.dto;

import org.utl.dsm.elzarape.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Contratos de la API (requests y responses). */
public final class Dtos {
    private Dtos() {}

    // ---- Auth ----
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record SignupRequest(
            @NotBlank @Size(min = 3, max = 60) String username,
            @NotBlank @Size(min = 6, max = 100) String password,
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Size(max = 80) String lastName,
            @Size(max = 20) String phone) {}

    public record UserView(Long id, String username, Role role, String name, String lastName, String phone) {
        public static UserView of(AppUser u) {
            return new UserView(u.getId(), u.getUsername(), u.getRole(), u.getName(), u.getLastName(), u.getPhone());
        }
    }

    public record AuthResponse(String token, UserView user) {}

    // ---- Categorías ----
    public record CategoryRequest(@NotBlank @Size(max = 80) String description, @NotNull ProductType type) {}

    public record CategoryView(Long id, String description, ProductType type) {
        public static CategoryView of(Category c) { return new CategoryView(c.getId(), c.getDescription(), c.getType()); }
    }

    // ---- Productos ----
    public record ProductRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description,
            @Size(max = 255) String photo,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
            @NotNull Long categoryId) {}

    public record ProductView(Long id, String name, String description, String photo, BigDecimal price, CategoryView category) {
        public static ProductView of(Product p) {
            return new ProductView(p.getId(), p.getName(), p.getDescription(), p.getPhoto(), p.getPrice(), CategoryView.of(p.getCategory()));
        }
    }

    // ---- Sucursales ----
    public record BranchRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 200) String address,
            @Size(max = 100) String city,
            @Size(max = 100) String hours,
            Double latitude,
            Double longitude,
            @Size(max = 255) String photo) {}

    public record BranchView(Long id, String name, String address, String city, String hours, Double latitude, Double longitude, String photo) {
        public static BranchView of(BranchOffice b) {
            return new BranchView(b.getId(), b.getName(), b.getAddress(), b.getCity(), b.getHours(), b.getLatitude(), b.getLongitude(), b.getPhoto());
        }
    }

    // ---- Pedidos (ticket + comanda) ----
    public record OrderItemRequest(@NotNull Long productId, @Min(1) @Max(99) int quantity) {}

    public record OrderRequest(
            @NotNull Long branchId,
            @NotNull PaymentMethod paymentMethod,
            @NotEmpty List<@Valid OrderItemRequest> items) {}

    public record OrderStatusRequest(@NotNull OrderStatus status) {}

    public record OrderItemView(Long productId, String product, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        public static OrderItemView of(OrderItem i) {
            return new OrderItemView(i.getProduct().getId(), i.getProduct().getName(), i.getQuantity(), i.getUnitPrice(),
                    i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
        }
    }

    public record OrderView(Long id, Instant createdAt, String client, String branch, PaymentMethod paymentMethod,
                            boolean paid, OrderStatus status, BigDecimal total, List<OrderItemView> items) {
        public static OrderView of(CustomerOrder o) {
            AppUser c = o.getClient();
            String clientName = ((c.getName() == null ? "" : c.getName()) + " " + (c.getLastName() == null ? "" : c.getLastName())).trim();
            return new OrderView(o.getId(), o.getCreatedAt(), clientName.isEmpty() ? c.getUsername() : clientName,
                    o.getBranch().getName(), o.getPaymentMethod(), o.isPaid(), o.getStatus(), o.getTotal(),
                    o.getItems().stream().map(OrderItemView::of).toList());
        }
    }
}
