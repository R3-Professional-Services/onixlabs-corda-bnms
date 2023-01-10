/*
 * Copyright 2020-2022 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.workflow

import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents a progress tracker step indicating that a membership transaction is being sent.
 */
object SendMembershipStep : Step("Sending membership transaction.")

/**
 * Represents a progress tracker step indicating that a membership transaction is being received.
 */
object ReceiveMembershipStep : Step("Receiving membership transaction.")

/**
 * Represents a progress tracker step indicating that a membership attestatopm transaction is being sent.
 */
object SendMembershipAttestationStep : Step("Sending membership attestation transaction.")

/**
 * Represents a progress tracker step indicating that a membership attestation transaction is being received.
 */
object ReceiveMembershipAttestationStep : Step("Receiving membership attestation transaction.")

/**
 * Represents a progress tracker step indicating that the local node's membership and attestations are being sent.
 */
object SendMembershipAndAttestationsStep : Step("Sending our (local) membership and attestations.")

/**
 * Represents a progress tracker step indicating that a counter-party node's membership and attestations are being received.
 */
object ReceiveMembershipAndAttestationsStep : Step("Receiving their (counter-party) membership and attestations.")

/**
 * Represents a progress tracker step indicating that counter-party nodes should check for membership and attestation states.
 */
object SendCheckMembershipInstructionStep : Step("Sending check membership instruction.")

/**
 * Represents a progress tracker step indicating that the local node should check for membership and attestation states.
 */
object ReceiveCheckMembershipInstructionStep : Step("Receiving check membership instruction.")

/**
 * Represents a progress tracker step indicating that the current node should check for membership and attestation states.
 */
object CheckMembershipStep : Step("Checking for membership and attestation states.")
