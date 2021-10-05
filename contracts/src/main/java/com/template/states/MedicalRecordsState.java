package com.template.states;

import com.template.contracts.MedicalRecordsContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(MedicalRecordsContract.class)
public class MedicalRecordsState implements ContractState {


    //private variables
    private int patientEMR; //Id Paciente
    private String patientName;
    private String patientMother;
    private String patientIdentificator; //CPF
    private String patientData;          //CID
    private Party receiverHospital;
    private Party requestHospital;


    @ConstructorForDeserialization
    public MedicalRecordsState(int patientEMR,
                               String patientName,
                               String patientMother,
                               String patientIdentificator,
                               String patientData,
                               Party requestHospital,
                               Party receiverHospital) {
        this.patientEMR = patientEMR;
        this.patientName = patientName;
        this.patientMother = patientMother;
        this.patientIdentificator = patientIdentificator;
        this.patientData = patientData;
        this.requestHospital = requestHospital;
        this.receiverHospital = receiverHospital;
    }


    //getters
    public int getPatientEMR() { return patientEMR; }
    public String getPatientName() { return patientName; }
    public String getPatientData() {return patientData; }
    public String getPatientMother() {return patientMother; }
    public String getPatientIdentificator() {return patientIdentificator; }
    public Party getRequestHospital() { return requestHospital; }
    public Party getReceiverHospital() { return receiverHospital; }

   @Override
   public String toString() {
       return "patientEMR : " + patientEMR +
               "patientName : " + patientName +
               "patientData : " + patientData +
               "patientMother: " + patientMother +
               "patientIdentificator: " + patientIdentificator;
   }

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(requestHospital, receiverHospital);
    }
}