package org.utl.dsm.elzarape.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Unifica ticket + comanda del sistema original. */
@Entity
@Table(name = "customer_order")
public class CustomerOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser client;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private BranchOffice branch;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private boolean paid;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.IN_SERVICE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    public Long getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public AppUser getClient() { return client; }
    public void setClient(AppUser client) { this.client = client; }
    public BranchOffice getBranch() { return branch; }
    public void setBranch(BranchOffice branch) { this.branch = branch; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<OrderItem> getItems() { return items; }
}
