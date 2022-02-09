<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

#BBChain: viabilidade da tecnologia DLT no ecossistema de saúde do estado de São Paulo Atividades Efetuadas

Plataforma segura para a gestão do ciclo de tratamento de doenças raras e a fim de reduzir o tempo de diagnóstico dessas doenças. 

A rede Corda proposta encontra-se conformada pelas seguientes participantes:

 Instituto Jô Clemente (IJC),
 Consultório Dra. Juliana,
 APAE.

Cada participante tem seu próprio nó hospedado em seu ambiente local. Esses nós serão integrados para a rede Corda o que permitirá a transferência de dados de
saúde entre os nós. Dessa forma as informações relevantes podem ser compartilhadas entre os nós criando eficiências e evitando duplicação.


## Usage

### Pre-requisites:

Ver https://docs.corda.net/getting-set-up.html.


### Executando os nós:

Abrir o terminal e ir à raiz do projeto e executar:
```
./gradlew clean deployNodes
```
Depois executar: 
```
./build/nodes/runnodes
```

Para interaturar com os CorDapps via o nó ' CRaSH shells.

Primeiramente, vamos para o shell CRaSH do nó IJC onde é gerado um registro médico para um paciente X ao executar o fluxo FillMedicalRecords.java da seguinte forma:
  
  flow start FillMedicalRecords patientEMR: 1, patientName: X, patientData: Examen de sangue, patientMother: Mae, patientIdentificator: 235.295.xxx-xx     

Podemos inspecionar o registro médico do paciente X no vault do nó IJC ao executar no shell o comando a seguir::

    run vaultQuery contractStateType: net.corda.core.contracts.ContractState

Se o nó Consultório deseja solicitar os registros médicos do paciente X, ele precisa solicitar os dados a partir do seu próprio shell. Assim, nos dirigimos para o
shell do nó Consultório e executamos o fluxo RequestPatientRecords.java indicando o nome do paciente e do nó de qual queremos solicitar os registros médicos da seguinte forma:

    flow start RequestPatientRecords from: IJC, patientName: X


