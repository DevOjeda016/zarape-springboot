package org.utl.dsm.elzarape.model;

import jakarta.persistence.*;

@Entity
public class BranchOffice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String hours;

    private Double latitude;
    private Double longitude;

    @Column(length = 255)
    private String photo;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
