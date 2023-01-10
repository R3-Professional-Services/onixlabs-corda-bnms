package io.onixlabs.corda.bnms.contract.membership.general

import io.onixlabs.corda.bnms.contract.DECENTRALIZED_NETWORK
import io.onixlabs.corda.bnms.contract.IDENTITY_A
import io.onixlabs.corda.bnms.contract.membership.Membership
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class MembershipRoleTests {

    @Test
    fun `Membership hasRole should return true when the expected role is present`() {

        // Arrange
        val membership = Membership(DECENTRALIZED_NETWORK, IDENTITY_A.party).configure { addRoles("Test") }

        // Act
        val result = membership.configuration.hasRole("Test")

        // Assert
        assertTrue(result)
    }
}
