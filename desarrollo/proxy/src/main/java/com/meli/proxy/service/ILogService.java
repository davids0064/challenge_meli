package com.meli.proxy.service;

import org.springframework.http.HttpStatus;

public interface ILogService {

    void registroLog(String ip, String path, HttpStatus httpStatus);

}
