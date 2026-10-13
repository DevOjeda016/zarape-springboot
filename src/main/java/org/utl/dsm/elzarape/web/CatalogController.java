package org.utl.dsm.elzarape.web;

import org.utl.dsm.elzarape.dto.Dtos.*;
import org.utl.dsm.elzarape.model.*;
import org.utl.dsm.elzarape.repository.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Catálogo: lectura pública, escritura solo ADMIN. El borrado es lógico (active=false), como en el sistema original. */
@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final BranchRepository branches;

    public CatalogController(CategoryRepository categories, ProductRepository products, BranchRepository branches) {
        this.categories = categories;
        this.products = products;
        this.branches = branches;
    }

    // ---------- Categorías ----------
    @GetMapping("/categories")
    public List<CategoryView> listCategories() {
        return categories.findByActiveTrueOrderByDescription().stream().map(CategoryView::of).toList();
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryView createCategory(@Valid @RequestBody CategoryRequest req) {
        Category c = new Category();
        c.setDescription(req.description());
        c.setType(req.type());
        return CategoryView.of(categories.save(c));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryView updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        Category c = categories.findById(id).filter(Category::isActive).orElseThrow(() -> ApiException.notFound("Categoría"));
        c.setDescription(req.description());
        c.setType(req.type());
        return CategoryView.of(categories.save(c));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        Category c = categories.findById(id).filter(Category::isActive).orElseThrow(() -> ApiException.notFound("Categoría"));
        if (products.countByCategoryIdAndActiveTrue(id) > 0) {
            throw ApiException.conflict("La categoría tiene productos activos");
        }
        c.setActive(false);
        categories.save(c);
    }

    // ---------- Productos ----------
    @GetMapping("/products")
    public List<ProductView> listProducts(@RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) ProductType type) {
        List<Product> list = categoryId != null ? products.findByActiveTrueAndCategoryIdOrderByName(categoryId)
                : type != null ? products.findByActiveTrueAndCategoryTypeOrderByName(type)
                : products.findByActiveTrueOrderByName();
        return list.stream().map(ProductView::of).toList();
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductView createProduct(@Valid @RequestBody ProductRequest req) {
        Product p = new Product();
        apply(p, req);
        return ProductView.of(products.save(p));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductView updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        Product p = products.findById(id).filter(Product::isActive).orElseThrow(() -> ApiException.notFound("Producto"));
        apply(p, req);
        return ProductView.of(products.save(p));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        Product p = products.findById(id).filter(Product::isActive).orElseThrow(() -> ApiException.notFound("Producto"));
        p.setActive(false);
        products.save(p);
    }

    private void apply(Product p, ProductRequest req) {
        Category c = categories.findById(req.categoryId()).filter(Category::isActive)
                .orElseThrow(() -> ApiException.badRequest("Categoría inválida"));
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPhoto(req.photo());
        p.setPrice(req.price());
        p.setCategory(c);
    }

    // ---------- Sucursales ----------
    @GetMapping("/branches")
    public List<BranchView> listBranches() {
        return branches.findByActiveTrueOrderByName().stream().map(BranchView::of).toList();
    }

    @PostMapping("/branches")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchView createBranch(@Valid @RequestBody BranchRequest req) {
        BranchOffice b = new BranchOffice();
        apply(b, req);
        return BranchView.of(branches.save(b));
    }

    @PutMapping("/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchView updateBranch(@PathVariable Long id, @Valid @RequestBody BranchRequest req) {
        BranchOffice b = branches.findById(id).filter(BranchOffice::isActive).orElseThrow(() -> ApiException.notFound("Sucursal"));
        apply(b, req);
        return BranchView.of(branches.save(b));
    }

    @DeleteMapping("/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBranch(@PathVariable Long id) {
        BranchOffice b = branches.findById(id).filter(BranchOffice::isActive).orElseThrow(() -> ApiException.notFound("Sucursal"));
        b.setActive(false);
        branches.save(b);
    }

    private void apply(BranchOffice b, BranchRequest req) {
        b.setName(req.name());
        b.setAddress(req.address());
        b.setCity(req.city());
        b.setHours(req.hours());
        b.setLatitude(req.latitude());
        b.setLongitude(req.longitude());
        b.setPhoto(req.photo());
    }
}
