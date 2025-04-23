package com.woow.axsalud.service.impl;

import com.woow.axsalud.service.api.DoctorQueueManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class DoctorQueueManagerImpl implements DoctorQueueManager {

    private final Queue<Long> doctorQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void doctorConnected(long doctorId) {
        if (!doctorQueue.contains(doctorId)) {
            doctorQueue.offer(doctorId);
        }
    }

    @Override
    public void doctorDisconnected(long doctorId) {
        doctorQueue.remove(doctorId);
    }

    @Override
    public long getNextDoctor() {
        Long doctorId = doctorQueue.poll();
        if (doctorId != null) {
            doctorQueue.offer(doctorId);
        }
        return doctorId;
    }

    @Override
    public List<Long> getAllDoctors() {
        return new ArrayList<>(doctorQueue);
    }
}
