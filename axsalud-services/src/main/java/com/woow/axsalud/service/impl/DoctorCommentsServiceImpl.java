package com.woow.axsalud.service.impl;

import com.woow.axsalud.data.consultation.ComentariosMedicos;
import com.woow.axsalud.data.repository.ComentariosMedicosRepository;
import com.woow.axsalud.service.api.DoctorCommentsService;
import com.woow.axsalud.service.api.dto.DoctorCommentsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DoctorCommentsServiceImpl implements DoctorCommentsService {

    private ComentariosMedicosRepository comentariosMedicosRepository;


    public DoctorCommentsServiceImpl(final ComentariosMedicosRepository comentariosMedicosRepository) {
        this.comentariosMedicosRepository = comentariosMedicosRepository;
    }

    @Override
    public List<DoctorCommentsDTO> getDoctorCommentsByUserName(String userName) {

        log.info("Getting Doctor comments for userName: {}", userName);
        List<ComentariosMedicos> comentariosMedicos =
                comentariosMedicosRepository.findByAxSaludWooUser_CoreUser_UserName(userName);

        List<DoctorCommentsDTO> doctorCommentsDTOS =
                comentariosMedicos.stream()
                        .map(comentariosMedicos1 -> {
                            DoctorCommentsDTO doctorCommentsDTO = new DoctorCommentsDTO();
                            doctorCommentsDTO.setComment(comentariosMedicos1.getObservacionesMedicas());
                            doctorCommentsDTO.setDoctorFullName(comentariosMedicos1
                                    .getConsultationSession().getDoctor()
                                    .getCoreUser().getName() + " " +
                                    comentariosMedicos1.getConsultationSession()
                                            .getDoctor().getCoreUser().getLastName());
                            doctorCommentsDTO.setCreatedAt(comentariosMedicos1.getConsultationSession().getCreatedAt());
                            return doctorCommentsDTO;
                        })
                        .collect(Collectors.toList());
        return doctorCommentsDTOS;
    }
}
