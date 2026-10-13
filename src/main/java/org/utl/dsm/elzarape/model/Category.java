package org.utl.dsm.elzarape.model;

import jakarta.persistence.*;

@Entity
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String description;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private ProductType type;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProductType getType() { return type; }
    public void setType(ProductType type) { this.type = type; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
