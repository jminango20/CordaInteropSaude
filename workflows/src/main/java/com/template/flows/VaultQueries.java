package com.template.flows;

import com.template.states.MedicalRecordsState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.node.services.Vault;
import net.corda.core.serialization.SingletonSerializeAsToken;

import java.util.ArrayList;
import java.util.List;

@CordaService
public class VaultQueries extends SingletonSerializeAsToken {
    private final AppServiceHub serviceHub;
    private final List<String> medicalRecordsDataPacient = new ArrayList<String>();

    public VaultQueries(AppServiceHub serviceHub) {
        this.serviceHub = serviceHub;
    }

    //query vault for the patient
    public List<String> queryVaultByPatient(String requestedPatientId) {
        Vault.Page<MedicalRecordsState> results = serviceHub.getVaultService().queryBy(MedicalRecordsState.class);
        if (results.equals(null)) {
            return null;
        }

        StateAndRef<MedicalRecordsState> patient = results.getStates().stream().filter(p -> {
            return p.getState().getData().getPatientName().equals(requestedPatientId);
        }).findAny().orElse(null);

        int medicalEMR = patient.getState().component1().getPatientEMR(); //patient EMR
        String medicalRecordsData = patient.getState().component1().getPatientData(); //patientName
        String medicalRecordsMother = patient.getState().component1().getPatientMother(); //patientMother
        String medicalRecordsIdentificator = patient.getState().component1().getPatientIdentificator(); //patientIdentificator

        medicalRecordsDataPacient.add(Integer.toString(medicalEMR));
        medicalRecordsDataPacient.add(medicalRecordsData);
        medicalRecordsDataPacient.add(medicalRecordsMother);
        medicalRecordsDataPacient.add(medicalRecordsIdentificator);

        //return medicalRecordsData + medicalRecordsMother + medicalRecordsIdentificator;
        return medicalRecordsDataPacient;
    }
}
