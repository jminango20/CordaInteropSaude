package com.template.contracts;

import com.template.states.MedicalRecordsState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import java.util.ArrayList;
import java.util.Arrays;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class MedicalRecordsContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.MedicalRecordsContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {

        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        ArrayList<String> rejectCountries = new ArrayList<String>(Arrays.asList("RU"));
        if (command.getValue() instanceof Commands.Create) {
            requireThat(require -> {
                require.using("Request should have no inputs", tx.getInputs().size() == 0);
                require.using("Request should have one output", tx.getOutputs().size() == 1);
                return null;
            });

        } else if (command.getValue() instanceof Commands.Request) {
            MedicalRecordsState ms = (MedicalRecordsState) tx.getOutputs().get(0).getData();
            String requesterCountry = ms.getParticipants().get(0).nameOrNull().getCountry();
            requireThat(require -> {
                require.using("Initiator should be not from rejected countries.", !(rejectCountries.contains(requesterCountry)));
                require.using("Initiating hospital should sign request", command.getSigners().contains(ms.getRequestHospital().getOwningKey()));
                require.using("Receiving hospital should sign request", command.getSigners().contains(ms.getReceiverHospital().getOwningKey()));
                return null;
            });
        } else {
            throw new IllegalArgumentException("Invalid command");
        }
    }
    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Create implements Commands {}
        class Request implements Commands {}
    }
}