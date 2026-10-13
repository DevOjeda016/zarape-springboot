package org.utl.dsm.elzarape.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private CustomerOrder order;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    /** Precio unitario congelado al momento de la compra. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public Long getId() { return id; }
    public CustomerOrder getOrder() { return order; }
    public void setOrder(CustomerOrder order) { this.order = order; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
