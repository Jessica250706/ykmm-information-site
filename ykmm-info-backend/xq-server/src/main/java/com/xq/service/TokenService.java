package com.xq.service;

public interface TokenService {

    String createUserToken(Long userId);

    String createAdminToken(Long empId);
}
