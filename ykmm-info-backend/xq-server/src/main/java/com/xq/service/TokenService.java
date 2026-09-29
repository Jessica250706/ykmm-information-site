package com.xq.service;

public interface TokenService {

    String createToken(Long userId, Integer role);
}
