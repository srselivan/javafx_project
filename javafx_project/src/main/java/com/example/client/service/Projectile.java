package com.example.client.service;

public interface Projectile {
    void move() throws Exception;
    void rollback();
    double getX();
    double getY();
}
