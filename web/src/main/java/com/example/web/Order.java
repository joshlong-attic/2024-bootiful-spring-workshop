package com.example.web;

record Order(int id, String sku, float price) {
}

record LineItem(int id) {
}