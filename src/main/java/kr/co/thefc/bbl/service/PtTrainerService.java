package kr.co.thefc.bbl.service;

import kr.co.thefc.bbl.model.trainerForm.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public interface PtTrainerService {

    HashMap trainerSave(PtTrainerForm ptTrainerForm);

    HashMap trainerInfoDetailSave(PtTrainerDetailForm ptTrainerDetailForm);

}