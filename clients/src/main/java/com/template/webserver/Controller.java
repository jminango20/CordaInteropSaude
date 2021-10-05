package com.template.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.flows.FillMedicalRecords;
import com.template.flows.RequestPatientRecords;
import com.template.states.MedicalRecordsState;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
//import net.corda.core.identity.Party;
//import net.corda.core.transactions.SignedTransaction;
//import

import net.corda.core.transactions.SignedTransaction;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    private final CordaX500Name me;

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }


    /** Helpers for filtering the network map cache. */
    public String toDisplayString(X500Name name){
        return BCStyle.INSTANCE.toString(name);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNetworkMap(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @Configuration
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }


    @GetMapping(value = "/status", produces = TEXT_PLAIN_VALUE)
    private String status() {
        return "200";
    }

    @GetMapping(value = "/servertime", produces = TEXT_PLAIN_VALUE)
    private String serverTime() {
        return (LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC"))).toString();
    }

    @GetMapping(value = "/addresses", produces = TEXT_PLAIN_VALUE)
    private String addresses() {
        return proxy.nodeInfo().getAddresses().toString();
    }

    @GetMapping(value = "/identities", produces = TEXT_PLAIN_VALUE)
    private String identities() {
        return proxy.nodeInfo().getLegalIdentities().toString();
    }

    @GetMapping(value = "/platformversion", produces = TEXT_PLAIN_VALUE)
    private String platformVersion() {
        return Integer.toString(proxy.nodeInfo().getPlatformVersion());
    }

    @GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers() {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = proxy.networkMapSnapshot().stream()
                .filter(el -> !isNotary(el) && !isMe(el) && !isNetworkMap(el));
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @GetMapping(value = "/notaries", produces = APPLICATION_JSON_VALUE)
    private String notaries() {
        return proxy.notaryIdentities().toString();
    }

    @GetMapping(value = "/flows", produces = APPLICATION_JSON_VALUE)
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/states", produces = APPLICATION_JSON_VALUE)
    private String states() {
        return proxy.vaultQuery(ContractState.class).getStates().toString();
    }

    @GetMapping(value = "/me",produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami(){
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }

    @GetMapping(value = "/medical",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<MedicalRecordsState>> getMedicalRecordsState() {
        // Filter by state type: MedicalRecordsState
        return proxy.vaultQuery(MedicalRecordsState.class).getStates();
    }


    @PostMapping (value = "fill-medical-records" , produces =  TEXT_PLAIN_VALUE , headers =  "Content-Type=application/x-www-form-urlencoded" )
    public ResponseEntity<String> fillMedicalRecords(HttpServletRequest request) throws IllegalArgumentException {

        int patientEMR = Integer.valueOf(request.getParameter("patientEMR"));
        String patientName = request.getParameter("patientName");
        String patientData = request.getParameter("patientData");
        String patientMother = request.getParameter("patientMother");
        String patientIdentificator = request.getParameter("patientIdentificator");

        // Create a new MedicalRecordsState state using the parameters given.
        try {
            // Start the flow FillMedicalRecords. We block and waits for the flow to return.
            SignedTransaction result = proxy.startTrackedFlowDynamic(FillMedicalRecords.class, patientEMR,patientName, patientData, patientMother, patientIdentificator).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id "+ result.getId() +" committed to ledger.\n " + result.getTx().getOutput(0));
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping (value = "request-patient-records" , produces =  TEXT_PLAIN_VALUE , headers =  "Content-Type=application/x-www-form-urlencoded" )
    public ResponseEntity<String> requestPatientRecords(HttpServletRequest request) throws IllegalArgumentException {

        String patientName = request.getParameter("patientName");
        String party = request.getParameter("from");
        CordaX500Name partyX500Name = CordaX500Name.parse(party);
        Party from = proxy.wellKnownPartyFromX500Name(partyX500Name);

        // Transfer a PatientRecords of MedicalRecordsState state using the parameters given.
        try {
            // Start the flow RequestPatientRecords. We block and waits for the flow to return.
            SignedTransaction result = proxy.startTrackedFlowDynamic(RequestPatientRecords.class, from,patientName).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id "+ result.getId() +" transfer to ledger.\n " + result.getTx().getOutput(0));
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    /**
     * Displays all MedicalRecordsState states that only this node has been involved in.
     */
    @GetMapping(value = "my-medical-records-state",produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StateAndRef<MedicalRecordsState>>> getMyMedicalRecordsState() {
        List<StateAndRef<MedicalRecordsState>> myMedicalRecordsState = proxy.vaultQuery(MedicalRecordsState.class).getStates().stream().filter(
                it -> it.getState().getData().getRequestHospital().equals(proxy.nodeInfo().getLegalIdentities().get(0))).collect(Collectors.toList());
        return ResponseEntity.ok(myMedicalRecordsState);
    }

}