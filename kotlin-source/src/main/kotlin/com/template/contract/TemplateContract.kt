package com.template.contract

import com.template.state.TemplateState
import net.corda.core.contracts.*
import net.corda.core.contracts.TransactionForContract
import net.corda.core.crypto.SecureHash

/**
 * Define your contract here.
 */
class TemplateContract : Contract {

    // Command.
    class Send : TypeOnlyCommandData()

    // Legal prose.
    override val legalContractReference: SecureHash = SecureHash.sha256("Yo!")

    // Contract code.
    override fun verify(tx: TransactionForContract) = requireThat {
        val command = tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when Yo'ing other parties." using (tx.inputs.isEmpty())
        "There must be one output: The Yo!" using (tx.outputs.size == 1)
        val yo = tx.outputs.single() as TemplateState
        "No sending Yo's to yourself!" using (yo.target != yo.origin)
        "The Yo! must be signed by the sender." using (yo.origin.owningKey == command.signers.single())
    }
}
