package com.template.states

import com.template.contracts.ChainContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(ChainContract::class)
data class ChainState(val Root: String,
                      val ParentName: String,
                      val Parent: String,
                      val Block_Name: String,
                      val Reward: String, val party: Party) : ContractState {

    override val participants: List<AbstractParty> = listOf(party);
    //To change initializer of created properties use File | Settings | File Templates.

}