<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

#Java Medical Records CorDapp

Welcome to the Java Medical Records CorDapp. The CorDapp has 3 nodes, which represent hospitals CharitéHospital (Berlin), StMarysHospital (London) and Bolnitsa (Moscow). 
Each hospital can save medical data on their vault and distribute to a particular hospital upon its request. 
But contact rejects request to share medical records for hospitals that locate in certain countries
This CorDapp utilises CordaServices to store vaultQueries.

Troca de nome dos Hospitais: CharitéHospital (Berlin) - IJC, StMarysHospital (London) - Consultório, Bolnitsa (Moscow) - APAE

## Usage

### Pre-requisites:

See https://docs.corda.net/getting-set-up.html.


### Running the nodes:

Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```

We will interact with this CorDapp via the nodes' CRaSH shells.

First, go the shell of CharitéHospital, and create a medical record for a Patient X with dataRecord value of 10:

    flow start FillMedicalRecords patientName: A, patientData: YYY       

We can now look at the medical record in the CharitéHospital's vault:

    run vaultQuery contractStateType: net.corda.core.contracts.ContractState

If StMarysHospital wants to request medical records of the patient's A, he needs to request data from the shell of StMarysHospital by running:

    flow start RequestPatientRecords from: "CharitéHospital", patientName: A


