package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.List;

@InitiatedBy(RequestPatientRecords.class)
public class ResponderPatientRecords extends FlowLogic<SignedTransaction> {

    private FlowSession session;
    private Party requestHospital ;
    private Party receiverHospital;

    public ResponderPatientRecords(FlowSession session) {
        this.session = session;
        this.receiverHospital = this.session.getCounterparty();
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        //String requestedPatientId = session.receive(String.class).unwrap(it -> it);

        String requestedPatientId = (String) session.receive(Object.class).unwrap(it -> it);

        //query vault for the patient
        VaultQueries vaultQueriesService = getServiceHub().cordaService(VaultQueries.class);
        final List<String> medicalRecordsData = vaultQueriesService.queryVaultByPatient(requestedPatientId);

        if (medicalRecordsData == null) {
            session.send(null);
            return null;
        } else {
            session.send(medicalRecordsData);
        }
        //it send medical record to Initiator, but then it countinues to run the flow. But it should wait for
        // the tx from Initiator

        SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(session) {
            @Suspendable
            @Override
            protected void checkTransaction(SignedTransaction stx) throws FlowException {

            }
        });
        //Stored the transaction into data base.
        subFlow(new ReceiveFinalityFlow(session, signedTransaction.getId()));
        return null;
    }
}
