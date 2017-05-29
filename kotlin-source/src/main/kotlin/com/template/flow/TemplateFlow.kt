package com.template.flow

import com.template.contract.TemplateContract
import com.template.state.TemplateState
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.crypto.Party
import net.corda.core.flows.FlowLogic
import net.corda.core.utilities.ProgressTracker
import net.corda.core.transactions.SignedTransaction
import net.corda.flows.FinalityFlow

/**
 * Define your flow here.
 */
class TemplateFlow(val target: Party): FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker = TemplateFlow.tracker()

    companion object {
        object CREATING : ProgressTracker.Step("Creating a new Yo!")
        object VERIFYING : ProgressTracker.Step("Verifying the Yo!")
        object SENDING : ProgressTracker.Step("Sending the Yo!")
        fun tracker() = ProgressTracker(CREATING, VERIFYING, SENDING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val me = serviceHub.myInfo.legalIdentity
        val notary = serviceHub.networkMapCache.notaryNodes.single().notaryIdentity

        progressTracker.currentStep = CREATING
        val signedYo = TransactionType.General.Builder(notary)
                .withItems(TemplateState(me, target), Command(TemplateContract.Send(), listOf(me.owningKey)))
                .signWith(serviceHub.legalIdentityKey)
                .toSignedTransaction(true)

        progressTracker.currentStep = VERIFYING
        signedYo.tx.toLedgerTransaction(serviceHub).verify()

        progressTracker.currentStep = SENDING
        return subFlow(FinalityFlow(signedYo, setOf(target))).single()
    }
}