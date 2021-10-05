package com.template;

import com.google.common.collect.ImmutableList;
import com.template.flows.FillMedicalRecords;
import com.template.flows.RequestPatientRecords;
import com.template.flows.ResponderPatientRecords;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;
    private StartedMockNode c;


    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows"))));
        a = network.createPartyNode(new CordaX500Name("StMarysHospital", "London", "GB"));
        b = network.createPartyNode(new CordaX500Name("CharitéHospital", "Berlin", "DE"));
        c = network.createPartyNode(new CordaX500Name("Bolnitsa", "Moscow", "RU"));
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        for (StartedMockNode node : ImmutableList.of(a, b, c)) {
            node.registerInitiatedFlow(ResponderPatientRecords.class);
        }
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void createRecordTest() throws ExecutionException, InterruptedException {
        CordaFuture<SignedTransaction> future = a.startFlow(new FillMedicalRecords(1,"Alexey", "data","MAE","CPF"));
        network.runNetwork();
        SignedTransaction ptx = future.get();
        assert(ptx.getTx().getInputs().isEmpty());
    }

    @Test
    public void exchangeOfRecordsTest() throws ExecutionException, InterruptedException {
        CordaFuture<SignedTransaction> future = a.startFlow(new FillMedicalRecords(1,"Alexey", "data","MAE","CPF"));
        network.runNetwork();
        SignedTransaction ptx = future.get();

        CordaFuture<SignedTransaction> future1 = b.startFlow(new RequestPatientRecords(a.getInfo().getLegalIdentities().get(0),"Alexey"));
        network.runNetwork();
        SignedTransaction ptx1 = future1.get();
        assert(!ptx1.getTx().getOutputs().isEmpty());
    }
}