# ilp-connector-java
An Interledger connector implemented in Java.

BIG SCARY WARNING: These classes provide a baseline for a Connector implementation, but be warned most of the functionality is still "to be implemented".

#Current Status
This code is currently at a pre-alpha state.  The main purpose of this code is to prove-out Connector functionality using a suite of in-memory unit tests that simulate all remote connections to any ledgers, and excercise various scenarios involving multiple combinations of ILP source/destination address, connector hops, routing variables, and more.  See the section below called _Test Scenarios_ for more details.

#ILP Core X Classes (org.interledgerx)
There is currently a Github repository in the Interledger organization ([java-ilp-core](https://github.com/interledger/java-ilp-core)) 
that hosts a set of proposed classes and interfaces that can be considered "core" to all Interledger java implementations.  Some of these classes and interfaces have been slightly modified and/or extended by this project (`ilp-connector-java`) and can be found in the `org.interledgerx.ilp` package.  These extensions _may_ or _may not_ be integrated into the actual ILP Core project at some point in the future.

#ILP Ledger
One of the core goals of ILP is to connect disparate ledgers, even ledgers that may not operate upon assets of the same type (e.g., ledgers tracking two different currencies).  To model this, this project uses an in-memory implementation of `org.interledgerx.ilp.core.Ledger` that tracks simluated accounts and provides an interface for clients to manipulate accounts and perform various ILP operations.  
   
Reference `money.fluid.ilp.ledger.inmemory.InMemoryLedger` for more details.

#ILP Connector
The main thrust of this project is an ILP Connector, which can orchestrate payments between two ledgers in an effort to complete an ILP transaction that might involve other Connectors and Ledgers.  This is modeled by the interface `money.fluid.ilp.connector.Connector`, with a default implementation found in `money.fluid.ilp.connector.DefaultConnector`.  

# ILP Ledger Client
In order for a Connector to participate in an ILP transaction, it must connect to a Ledger and listen for various events that the Ledger will emit in response to ILP activity.  To facilitate this connection, this project defines a LedgerClient, which is meant to be operated in _Connector Space_ and act as a bridge between Ledger and Connector.  Each instantiation of a `LedgerClient` involves a single Ledger<-->Connector connection. 

This project models a LedgerClient via `money.fluid.ilp.ledgerclient.LedgerClient`, with am implementation found in `money.fluid.ilp.ledger.inmemory.InMemoryLedgerClient` that is meant to simulate a remote connection between Ledger and Client, but without using any actual remote facilities.  In other words, for simulation purposes, `InMemoryLedgerClient` is basically just a LedgerClient that acts upon in-process pointers to a `Ledger` and a `Connector` object.  

Additionally, the `LedgerClient` interface provides a defined way for a Connector to talk to any Ledger, with any implementation of a LedgerClient handling the actual details of how that connection should operate.  This segmentation effectively separates the _manner_ in which a Connector communicates to a Ledger from the actual functionality of the Connector itself.

#Test Harness
In order to simulate and test various ledger/connector scenarios, the `money.fluid.ilp.connector.IlpInMemoryTestHarness` was created.  This is effectively a JUnit test that creates in-memory instances of various simulated Ledgers and Connectors, and executes ILP transactions to test and model ILP interactions.

At a high-level, the harness utilizes simulated account holders `Alice` and `Bob`, and gives them accounts on 2 different simulated ledgers with distinct asset types, each supporting conditional holds.  

The first simulated ledger is called the "Sand Ledger", which is denominated in granules of Silica Sand, like what you would find at 
  the beach.  The currency code for this ledger is SND, with a currency symbol of (S).  Both Alice and Bob start off with SND 500.

The second simulated ledger is called the "Dirt Ledger", which is denominated in granules of dirt, like what you would find on a baseball field or on a farm.  The currency code for this ledger is DRT, with a currency symbol of (D).  Both Alice and Bob start off with DRT 500.

Last but not least are two connectors which have accounts on each ledger and can participate in ILP transactions.

##Test Cases
The following scenarios are enivisioned to be covered by `IlpInMemoryTestHarness`, with likely many more to come.

* **SAME_LEDGER__ACCEPTED**
A payment from one account to another on the same ledger where the recipient accepts the payment.  Since the transfer is performed on the same ledger, ILP is not strictly required.  However, a real ILP sender (e.g., a user in a browser) may not know if a Connector is involved or not since they will likely just be specifying an ILP destination address and expect their Ledger to figure things out).
  * -[x] Optimistic Mode
  * No Universal Mode (This type of ILP transaction is covered above using Optimistic Mode, and will not require fulfillments).
* **SAME_LEDGER__REJECTED**
A payment from one account to another on the same ledger where the recipient rejects the payment.
  * -[x] Optimistic Mode
  * No Universal Mode (This type of ILP transaction is covered above using Optimistic Mode, and will not require fulfillments).
* **DIFFERENT_LEDGERS__SAME_ASSET_TYPE__ONE_CONNECTOR__ACCEPTED**
A payment from one account to another on different ledgers that have same asset type, and where the transfer involves one Connector, and where the recipient accepts the payment.  An example of this might be an ILP transfer between two U.S. banks.  Both banks (and their ledgers) will deal with USD currency, but it might require a Connector with ledger accounts at each bank to fulfill the transfer.  Also notice that no FX conversion is required since the two coordinated ledgers are using the same asset type (i.e., USD)
  * -[x] Optimistic Mode
  * -[ ] Universal Mode
* **DIFFERENT_LEDGERS__SAME_ASSET_TYPE__ONE_CONNECTOR__REJECTED**
A payment from one account to another on different ledgers that have same asset type, and where the transfer involves one Connector, and where the recipient rejects the payment.
  * -[x] Optimistic Mode
  * -[ ] Universal Mode
* **DIFFERENT_LEDGERS__SAME_ASSET_TYPE__MULTIPLE_CONNECTORS__ACCEPTED**
A payment from one account to another on different ledgers that have the same asset type, and where the transfer involves more than one Connector, and where recipient accepts the payment.
  * -[x] Optimistic Mode
  * -[ ] Universal Mode
* **DIFFERENT_LEDGERS__SAME_ASSET_TYPE__MULTIPLE_CONNECTORS__REJECTED**
A payment from one account to another on different ledgers that have same asset type, and where the transfer involves more than one Connector, and where the recipient rejects the payment.
  * -[ ] Optimistic Mode
  * -[ ] Universal Mode
* **DIFFERENT_LEDGERS__DIFFERENT_ASSET_TYPE__ONE_CONNECTOR__ACCEPTED**
A payment from one account to another on different ledgers that have different asset types, and where the transfer involves one Connector, and where the recipient accepts the payment.  An example of this might be an ILP transfer from a U.S. bank and an European bank.  One bank (and its ledger) will deal with USD currency, while the other will likely deal with EUR currency.  These tests involving proper routing based upon transaction feed quoting.
  * -[ ] Optimistic Mode
  * -[ ] Universal Mode
* **DIFFERENT_LEDGERS__DIFFERENT_ASSET_TYPE__ONE_CONNECTOR__REJECTED**
A payment from one account to another on different ledgers that have different asset types, and where the transfer involves one Connector, and where the recipient rejects the payment.
  * -[ ] Optimistic Mode
  * -[ ] Universal Mode
* **DIFFERENT_LEDGERS__DIFFERENT_ASSET_TYPES__MULTIPLE_CONNECTORS__ACCEPTED**
A payment from one account to another on different ledgers that have different asset types, and where the transfer involves more than one Connector, and where the recipient accepts the payment.  An example of this might be an ILP transfer from a U.S. bank and an European bank.  One bank (and its ledger) will deal with USD currency, while the other will likely deal with EUR currency.  These tests involving proper routing based upon transaction feed quoting.
  * -[ ] Optimistic Mode
  * -[ ] Universal Mode
* **DIFFERENT_LEDGERS__DIFFERENT_ASSET_TYPES__MULTIPLE_CONNECTORS__REJECTED**
A payment from one account to another on different ledgers that have different asset types, and where the transfer involves more than one Connector, and where the recipient rejects the payment.
  * -[ ] Optimistic Mode
  * -[ ] Universal Mode
  
### A note on Optimistic Mode
_Optimistic mode in ILP simply means that a transfer is being sent from one ILP address to another with no fulfilment conditions.  It is
a mode of ILP that is intended to be used with smaller payments under the assumption that most ILP participants will be "good actors",
so sending small amounts of money might occur more quickly if fulfilments aren't used.  In this mode, it's possible for a Connector
to pass the equivalent of a forged fulfilment (though no fulfilments are actually used), so Connectors operating in this mode should 
generally only do so with trusted connectors._


### Invocations and how to RUN
Start using Application.java
Check if running - http://localhost:8080/health

 * -[ ] QuoteController has been commented - its a TODO for future.
 * -[ ] GCPController - Use for Google Cloud
 * -[ ] ILPController - To implement in immediate future.
