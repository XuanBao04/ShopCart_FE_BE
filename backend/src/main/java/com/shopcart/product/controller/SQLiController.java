package com.shopcart.product.controller;

import com.shopcart.product.entity.Product;
import com.shopcart.product.service.impl.SQLiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sqli")
@RequiredArgsConstructor
public class SQLiController {

    private final SQLiService sqliService;

    @GetMapping("/vulnerable/search")
    public List<Product> searchVulnerable(@RequestParam String name) {
        return sqliService.searchVulnerable(name);
    }

    @GetMapping("/secure/search")
    public List<Product> searchSecure(@RequestParam String name) {
        return sqliService.searchSecure(name);
    }
}
