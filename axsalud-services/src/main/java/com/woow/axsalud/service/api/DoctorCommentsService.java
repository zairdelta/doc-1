package com.woow.axsalud.service.api;

import com.woow.axsalud.service.api.dto.DoctorCommentsDTO;

import java.util.List;

public interface DoctorCommentsService {
    List<DoctorCommentsDTO> getDoctorCommentsByUserName(String userName);
}
