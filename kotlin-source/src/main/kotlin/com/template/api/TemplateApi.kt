package com.template.api

import com.template.flow.TemplateFlow
import com.template.contract.TemplateContract
import com.template.state.TemplateState
import net.corda.core.messaging.CordaRPCOps
import net.corda.client.rpc.notUsed
import net.corda.core.getOrThrow
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import rx.Observable
import javax.ws.rs.QueryParam

// This API is accessible from /api/template. The endpoint paths specified below are relative to it.
@Path("template")
class TemplateApi(val services: CordaRPCOps) {
    // Helper extension property to grab snapshot only.
    private val <A> Pair<A, Observable<*>>.justSnapshot: A get() {
        second.notUsed()
        return first
    }

    @GET
    @Path("yo")
    @Produces(MediaType.APPLICATION_JSON)
    fun yo(@QueryParam(value = "target") target: String): Response {
        val (status, message) = try {
            // Is the 'target' valid?
            val toYo = services.partyFromName(target) ?: throw IllegalArgumentException("$target is unknown.")
            // Start the flow.
            val flowHandle = services.startFlowDynamic(TemplateFlow::class.java, toYo)
            flowHandle.use { it.returnValue.getOrThrow() }
            // Return the response.
            Response.Status.CREATED to "Yo just send a Yo! to ${toYo.name}"
        } catch (e: Exception) {
            Response.Status.BAD_REQUEST to e.message
        }
        return Response.status(status).entity(message).build()
    }

    @GET
    @Path("yos")
    @Produces(MediaType.APPLICATION_JSON)
    fun yos() = services.vaultAndUpdates().justSnapshot.filter { it.state.data is TemplateState }

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun me() = mapOf("me" to services.nodeIdentity().legalIdentity.name)

    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun peers() = mapOf("peers" to services.networkMapUpdates().justSnapshot.map { it.legalIdentity.name })
}