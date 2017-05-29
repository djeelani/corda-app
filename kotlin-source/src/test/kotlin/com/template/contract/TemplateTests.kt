package com.template.contract

import com.template.contract.TemplateContract
import com.template.state.TemplateState
import com.template.flow.TemplateFlow

import net.corda.core.contracts.*
import net.corda.core.crypto.CompositeKey
import net.corda.core.getOrThrow
import net.corda.core.node.services.unconsumedStates
import net.corda.core.utilities.ALICE
import net.corda.core.utilities.BOB
import net.corda.testing.*
import net.corda.testing.node.MockNetwork
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class TemplateTests {
    lateinit var net: MockNetwork
    lateinit var a: MockNetwork.MockNode
    lateinit var b: MockNetwork.MockNode

    @Before
    fun setup() {
        net = MockNetwork()
        val nodes = net.createSomeNodes(2)
        a = nodes.partyNodes[0]
        b = nodes.partyNodes[1]
        net.runNetwork()
    }

    @After
    fun tearDown() {
        net.stopNodes()
    }

    @Test
    fun yoTransactionMustBeWellFormed() {
        // A pre-made Yo to Bob.
        val yo = TemplateState(ALICE, BOB)
        // A pre-made dummy state.
        val dummyState = object : ContractState {
            override val contract get() = DUMMY_PROGRAM_ID
            override val participants: List<CompositeKey> get() = listOf()
        }
        // A pre-made dummy command.
        class DummyCommand : TypeOnlyCommandData()
        // Tests.
        ledger {
            // input state present.
            transaction {
                input { dummyState }
                command(ALICE_PUBKEY) { TemplateContract.Send() }
                output { yo }
                this.failsWith("There can be no inputs when Yo'ing other parties.")
            }
            // No command.
            transaction {
                output { yo }
                this.failsWith("")
            }
            // Wrong command.
            transaction {
                output { yo }
                command(ALICE_PUBKEY) { DummyCommand() }
                this.failsWith("")
            }
            // Command signed by wrong key.
            transaction {
                output { yo }
                command(MINI_CORP_PUBKEY) { TemplateContract.Send() }
                this.failsWith("The Yo! must be signed by the sender.")
            }
            // Sending to yourself is not allowed.
            transaction {
                output { TemplateState(ALICE, ALICE) }
                command(ALICE_PUBKEY) { TemplateContract.Send() }
                this.failsWith("No sending Yo's to yourself!")
            }
            transaction {
                output { yo }
                command(ALICE_PUBKEY) { TemplateContract.Send() }
                this.verifies()
            }
        }
    }

    // @Test
    // fun flowWorksCorrectly() {
    //     val yo = TemplateState(a.info.legalIdentity, b.info.legalIdentity)
    //     val flow = TemplateFlow(b.info.legalIdentity)
    //     val future = a.services.startFlow(flow).resultFuture
    //     net.runNetwork()
    //     val stx = future.getOrThrow()
    //     // Check yo transaction is stored in the storage service and the state in the vault.
    //     val bTx = b.storage.validatedTransactions.getTransaction(stx.id)
    //     assertEquals(bTx, stx)
    //     print("$bTx == $stx")
    //     val bYo = b.vault.unconsumedStates<TemplateState>().single().state.data
    //     // Strings match but the linearId's will differ.
    //     assertEquals(bYo.toString(), yo.toString())
    //     print("$bYo == $yo")
    // }
}