package org.utl.dsm.elzarape.web;

import org.utl.dsm.elzarape.dto.Dtos.*;
import org.utl.dsm.elzarape.model.*;
import org.utl.dsm.elzarape.repository.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    /** Transiciones válidas del flujo de cocina; CANCELLED y DELIVERED son estados finales. */
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.IN_SERVICE, Set.of(OrderStatus.READY, OrderStatus.CANCELLED),
            OrderStatus.READY, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of());

    private final OrderRepository orders;
    private final ProductRepository products;
    private final BranchRepository branches;

    public OrderController(OrderRepository orders, ProductRepository products, BranchRepository branches) {
        this.orders = orders;
        this.products = products;
        this.branches = branches;
    }

    /** Crea ticket + comanda. Los precios se toman del catálogo, nunca del cliente. */
    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public OrderView create(@AuthenticationPrincipal AppUser user, @Valid @RequestBody OrderRequest req) {
        BranchOffice branch = branches.findById(req.branchId()).filter(BranchOffice::isActive)
                .orElseThrow(() -> ApiException.badRequest("Sucursal inválida"));

        // Une líneas repetidas del mismo producto.
        Map<Long, Integer> qtyByProduct = req.items().stream()
                .collect(Collectors.toMap(OrderItemRequest::productId, OrderItemRequest::quantity, Integer::sum));

        CustomerOrder order = new CustomerOrder();
        order.setClient(user);
        order.setBranch(branch);
        order.setPaymentMethod(req.paymentMethod());
        // Pago con tarjeta simulado en el MVP: se considera pagado al confirmar.
        order.setPaid(req.paymentMethod() == PaymentMethod.CARD);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : qtyByProduct.entrySet()) {
            Product p = products.findById(e.getKey()).filter(Product::isActive)
                    .orElseThrow(() -> ApiException.badRequest("Producto inválido: " + e.getKey()));
            if (e.getValue() > 99) {
                throw ApiException.badRequest("Cantidad máxima por producto: 99");
            }
            OrderItem item = new OrderItem();
            item.setProduct(p);
            item.setQuantity(e.getValue());
            item.setUnitPrice(p.getPrice());
            order.addItem(item);
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(e.getValue())));
        }
        order.setTotal(total);
        return OrderView.of(orders.save(order));
    }

    @GetMapping("/mine")
    @Transactional(readOnly = true)
    public List<OrderView> mine(@AuthenticationPrincipal AppUser user) {
        return orders.findByClientIdOrderByCreatedAtDesc(user.getId()).stream().map(OrderView::of).toList();
    }

    /** Vista de cocina / administración. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @Transactional(readOnly = true)
    public List<OrderView> all() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(OrderView::of).toList();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @Transactional
    public OrderView updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest req) {
        CustomerOrder o = orders.findById(id).orElseThrow(() -> ApiException.notFound("Pedido"));
        if (!TRANSITIONS.get(o.getStatus()).contains(req.status())) {
            throw ApiException.conflict("No se puede pasar de " + o.getStatus() + " a " + req.status());
        }
        o.setStatus(req.status());
        // Entregar un pedido en efectivo implica que ya se cobró.
        if (req.status() == OrderStatus.DELIVERED) {
            o.setPaid(true);
        }
        return OrderView.of(o);
    }
}
