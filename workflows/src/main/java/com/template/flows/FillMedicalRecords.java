package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MedicalRecordsContract;
import com.template.states.MedicalRecordsState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@StartableByRPC
public class FillMedicalRecords extends FlowLogic<SignedTransaction> {

    private final ProgressTracker progressTracker = new ProgressTracker();
    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    //private variables
    private Party requestHospital;
    private int patientEMR; //Id Paciente
    private String patientName;
    private String patientData;
    private String patientMother;
    private String patientIdentificator;

    //public constructor
    public FillMedicalRecords( int patientEMR, String patientName, String patientData, String patientMother, String patientIdentificator){
        this.patientEMR = patientEMR;
        this.patientName = patientName;
        this.patientData = patientData;
        this.patientMother = patientMother;
        this.patientIdentificator = patientIdentificator;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        //Hello World message
       // String msg = "Hello-World";
        this.requestHospital = getOurIdentity();

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        //Compose the State that carries patient's information
        final MedicalRecordsState output = new MedicalRecordsState(patientEMR,
                                                                   patientName,
                                                                   patientMother,
                                                                   patientIdentificator,
                                                                   patientData,
                                                                   requestHospital,
                                                                   requestHospital);

        // Step 3. Create a new TransactionBuilder object.
        final TransactionBuilder builder = new TransactionBuilder(notary);

        // Step 4. Add the output state, as well as a command to the transaction builder.
        builder.addOutputState(output);
        builder.addCommand(new MedicalRecordsContract.Commands.Create(), Arrays.asList(this.requestHospital.getOwningKey()) );


        // Step 5. Verify and sign it with our KeyPair.
        builder.verify(getServiceHub());
        final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);


        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
        otherParties.remove(getOurIdentity());
        otherParties.remove(getOurIdentity());
        List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());

        SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, sessions));

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow(new FinalityFlow(stx, sessions));
    }
}
