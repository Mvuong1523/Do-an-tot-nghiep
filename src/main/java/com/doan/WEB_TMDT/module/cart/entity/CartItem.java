package com.doan.WEB_TMDT.module.cart.entity;

import com.doan.WEB_TMDT.module.product.entity.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Một cart item thuộc 1 cart
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // Một cart item tham chiếu đến 1 product
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    public CartItem() {}

    public Long getId() {
        return id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
