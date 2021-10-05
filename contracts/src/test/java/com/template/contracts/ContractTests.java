package com.template.contracts;

import com.template.states.MedicalRecordsState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity stMarys = new TestIdentity(new CordaX500Name("CONSULTORIO", "London", "GB"));
    private final TestIdentity charite = new TestIdentity(new CordaX500Name("IJC", "Berlin", "DE"));
    private final TestIdentity bolnitsa = new TestIdentity(new CordaX500Name("APAE", "Moscow", "RU"));

    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "", "GB")));

    private MedicalRecordsState medicalRecords = new MedicalRecordsState(1,"Alexey", "NOME DA MAE", "CPF" , " HISTORICO DO PACIENTE", stMarys.getParty(), charite.getParty());
    private MedicalRecordsState medicalRecords2 = new MedicalRecordsState(2,"Alexey", "NOME DA MAE","CPF", "HISTORICO DO PACIENTE", bolnitsa.getParty(), charite.getParty());

    @Test
    public void tokenContractImplementsContract() {
        assert(new MedicalRecordsContract() instanceof Contract);
    }

    @Test
    public void tokenContractRequiresZeroInputsInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Country is in the list, will fail.
            tx.output(MedicalRecordsContract.ID, medicalRecords2);
            tx.command(Arrays.asList(bolnitsa.getPublicKey(), charite.getPublicKey()), new MedicalRecordsContract.Commands.Request());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Country not from the list, will verify.
            tx.output(MedicalRecordsContract.ID, medicalRecords);
            tx.command(Arrays.asList(stMarys.getPublicKey(), charite.getPublicKey()), new MedicalRecordsContract.Commands.Request());
            tx.verifies();
            return null;
        });
    }
}
