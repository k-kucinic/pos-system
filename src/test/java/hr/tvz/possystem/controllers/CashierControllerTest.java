package hr.tvz.possystem.controllers;

import hr.tvz.possystem.model.Product;
import hr.tvz.possystem.model.SaleItem;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CashierControllerTest {

    @Test
    public void calculateTotalTest() {

        SaleItem saleItemMockFirst = new SaleItem.SaleItemBuilder().build();
        saleItemMockFirst.setQuantity(50);
        saleItemMockFirst.setUnitPrice(BigDecimal.valueOf(2));
        SaleItem saleItemMockSecond = new SaleItem.SaleItemBuilder().build();
        saleItemMockSecond.setQuantity(1);
        saleItemMockSecond.setUnitPrice(BigDecimal.valueOf(3.25));
        CashierController cashierController = new CashierController();
        List<SaleItem> saleItemsMock = List.of(saleItemMockFirst, saleItemMockSecond);
        cashierController.setSaleItemsFromCart(FXCollections.observableArrayList(saleItemsMock));
        BigDecimal result = cashierController.calculateTotal();

        assertEquals(0, result.compareTo(new BigDecimal("103.25")));
    }

    @Test
    public void calculateTotalDiscountTest() {
        SaleItem saleItemMockFirst = new SaleItem.SaleItemBuilder().build();
        saleItemMockFirst.setQuantity(2);
        saleItemMockFirst.setUnitPrice(BigDecimal.valueOf(2));
        Product product = new Product.ProductBuilder().build();
        product.setDiscount(BigDecimal.ZERO);
        saleItemMockFirst.setProduct(product);
        SaleItem saleItemMockSecond = new SaleItem.SaleItemBuilder().build();
        saleItemMockSecond.setQuantity(3);
        saleItemMockSecond.setUnitPrice(BigDecimal.valueOf(3));
        Product productSecond = new Product.ProductBuilder().build();
        productSecond.setDiscount(new BigDecimal("0.5"));
        saleItemMockSecond.setProduct(productSecond);
        CashierController cashierController = new CashierController();
        List<SaleItem> saleItemsMock = List.of(saleItemMockFirst, saleItemMockSecond);
        cashierController.setSaleItemsFromCart(FXCollections.observableArrayList(saleItemsMock));
        BigDecimal result = cashierController.totalDiscount();
        assertEquals(0, result.compareTo(new BigDecimal("1.50")));

    }

}