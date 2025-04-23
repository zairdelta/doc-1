package com.woow.axsalud.service.api;
import java.util.List;

public interface DoctorQueueManager {
    void doctorConnected(long doctorId);
    // Cuando un doctor se desconecta
    void doctorDisconnected(long doctorId);
    long getNextDoctor();
    List<Long> getAllDoctors();
}
