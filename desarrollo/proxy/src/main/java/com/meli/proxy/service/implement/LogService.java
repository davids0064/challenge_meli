package com.meli.proxy.service.implement;

import com.meli.proxy.jpa.entity.LogEntity;
import com.meli.proxy.jpa.repository.LogRepository;
import com.meli.proxy.service.ILogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogService implements ILogService {

    private final LogRepository logRepository;

    public void registroLog(String ip, String path){
        LogEntity logEntity = new LogEntity();
        logEntity.setId(UUID.randomUUID().toString());
        logEntity.setFechaUso(LocalDateTime.now());
        logEntity.setIp(ip);
        logEntity.setPath(path);
        logRepository.save(logEntity);
    }

}
