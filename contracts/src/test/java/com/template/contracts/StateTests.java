package com.template.contracts;


import com.template.states.MedicalRecordsState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StateTests {
    private final Party stMarys = new TestIdentity(new CordaX500Name("StMarysHospital", "London", "GB")).getParty();
    private final Party charite = new TestIdentity(new CordaX500Name("CharitéHospital", "Berlin", "DE")).getParty();

    @Test
    public void tokenStateHasIssuerOwnerAndAmountParamsOfCorrectTypeInConstructor() {
        new MedicalRecordsState(1,"Alexey", "Mae", "CPF","data", stMarys, charite);

    }

    @Test
    public void tokenStateHasGettersForIssuerOwnerAndAmount() {
        MedicalRecordsState medicalRecords = new MedicalRecordsState(1,"Alexey", "Mae", "CPF","data", stMarys, charite);
        assertEquals("Alexey", medicalRecords.getPatientName());
        assertEquals("Mae", medicalRecords.getPatientMother());
        assertEquals("CPF", medicalRecords.getPatientIdentificator());
        assertEquals("data", medicalRecords.getPatientData());
        assertEquals(stMarys, medicalRecords.getRequestHospital());
        assertEquals(charite, medicalRecords.getReceiverHospital());
    }
}