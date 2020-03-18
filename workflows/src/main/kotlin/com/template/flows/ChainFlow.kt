package com.template.flows

import ExternalDB.CryptoValuesDatabaseService
import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.ChainContract
import com.template.states.ChainState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.contracts.requireThat
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import javax.swing.plaf.nimbus.State

// *********
// * Flows *
// *********

@InitiatingFlow
@StartableByRPC
class ChainFlow constructor( var Root:String,var ParentName: String,
                            var Parenttx : String ,var Block_name: String, var Reward : String,  var party:Party): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call() : SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val database = serviceHub.cordaService(CryptoValuesDatabaseService::class.java)

        var namestate = ChainState(Root, "","",Block_name, "",party)


        if(serviceHub.vaultService.queryBy(ChainState::class.java).states.isEmpty()){
            namestate = ChainState(Root, ParentName,Parenttx,Block_name, "",party)
        }
        else {
            var statelist = serviceHub.vaultService.queryBy(ChainState::class.java).states
            val InState = serviceHub.toStateAndRef<ChainState>(statelist.get(statelist.size-1).ref)
            val txhash=database.queryTokenValue(ParentName)
            namestate = ChainState(Root,ParentName,txhash,Block_name, Reward,party)

        }

        val command = Command(ChainContract.Commands.Create(), listOf(party).map { it.owningKey })

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(namestate, ChainContract.ID)
                .addCommand(command)
        txBuilder.verify(serviceHub)
        val tx = serviceHub.signInitialTransaction(txBuilder)

        val sessions = (namestate.participants - ourIdentity).map { initiateFlow(it as Party) }
        val stx = subFlow(CollectSignaturesFlow(tx, sessions))

        val tx1= subFlow(FinalityFlow(stx, sessions))

        database.addTokenValue(Block_name,tx1.toString())


        return tx1
    }

}

@InitiatedBy(ChainFlow::class)
class ChainFlow_R(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data

            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}


@InitiatingFlow
@StartableByRPC
class Transactions : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : String {
        var Secure = SecureHash.randomSHA256()
        return Secure.toString()
    }
}

@InitiatingFlow
@StartableByRPC
class Check_State(var Block_name: String,var tx1 : String) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : String {

        val database = serviceHub.cordaService(CryptoValuesDatabaseService::class.java)
         database.addTokenValue(Block_name,tx1)

        var tx1=database.queryTokenValue(Block_name)
        return tx1

    }
}

@InitiatingFlow
@StartableByRPC
class PrintWholeTable() : FlowLogic<ArrayList<String>>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : ArrayList<String>  {

        val database = serviceHub.cordaService(CryptoValuesDatabaseService::class.java)
        var tx1=  database.querywholetable()
        val AddTransaction  : ArrayList<String> = ArrayList<String>()

        var number = tx1.size-1
        while (number >= 0){
           var data=  tx1.get(number).toString()
            AddTransaction.add(data)
            number--
        }
        return AddTransaction
    }
}

@InitiatingFlow
@StartableByRPC
class Alltransaction : FlowLogic<ArrayList<String>>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : ArrayList<String> {
      var ar=serviceHub.vaultService.queryBy(ChainState::class.java).states.size

      var statelist=serviceHub.vaultService.queryBy(ChainState::class.java).states
      val AddTransaction  : ArrayList<String> = ArrayList<String>()


        while(ar!=0){

           var Instate= serviceHub.toStateAndRef<ChainState>(statelist.get(ar-1).ref)
            var add= Instate.ref.txhash
            AddTransaction.add(add.toString())
            ar--
        }

        return AddTransaction
    }
}