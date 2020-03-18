package com.template.contracts

import com.template.states.ChainState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction


class ChainContract : Contract {

    companion object{
        const val ID = "com.template.contracts.ChainContract"
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands.Create>()
        requireThat {
            "No Inputs should be empty" using (tx.inputs.isEmpty())
            "Only one output state should be created" using (tx.outputs.size ==1)
        }
    }

    interface Commands : CommandData {
        class Create : ChainContract.Commands;
    }
}
