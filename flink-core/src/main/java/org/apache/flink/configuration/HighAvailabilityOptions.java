/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.configuration;

import org.apache.flink.annotation.docs.Documentation;
import org.apache.flink.configuration.description.Description;
import org.apache.flink.configuration.description.TextElement;

import static org.apache.flink.configuration.ConfigOptions.key;

/** The set of configuration options relating to high-availability settings. */
public class HighAvailabilityOptions {

    // ------------------------------------------------------------------------
    //  Required High Availability Options
    // ------------------------------------------------------------------------

    /**
     * Defines high-availability mode used for the cluster execution. A value of "NONE" signals no
     * highly available setup. To enable high-availability, set this mode to "ZOOKEEPER". Can also
     * be set to FQN of HighAvailability factory class.
     */
    @Documentation.Section(Documentation.Sections.COMMON_HIGH_AVAILABILITY)
    public static final ConfigOption<String> HA_MODE =
            key("high-availability")
                    .defaultValue("NONE")
                    .withDeprecatedKeys("recovery.mode")
                    .withDescription(
                            "Defines high-availability mode used for the cluster execution."
                                    + " To enable high-availability, set this mode to \"ZOOKEEPER\" or specify FQN of factory class.");

    /**
     * The ID of the Flink cluster, used to separate multiple Flink clusters Needs to be set for
     * standalone clusters, is automatically inferred in YARN.
     */
    @Documentation.Section(Documentation.Sections.COMMON_HIGH_AVAILABILITY)
    public static final ConfigOption<String> HA_CLUSTER_ID =
            key("high-availability.cluster-id")
                    .defaultValue("/default")
                    .withDeprecatedKeys(
                            "high-availability.zookeeper.path.namespace",
                            "recovery.zookeeper.path.namespace")
                    .withDescription(
                            "The ID of the Flink cluster, used to separate multiple Flink clusters from each other."
                                    + " Needs to be set for standalone clusters but is automatically inferred in YARN.");

    /** File system path (URI) where Flink persists metadata in high-availability setups. */
    @Documentation.Section(Documentation.Sections.COMMON_HIGH_AVAILABILITY)
    public static final ConfigOption<String> HA_STORAGE_PATH =
            key("high-availability.storageDir")
                    .noDefaultValue()
                    .withDeprecatedKeys(
                            "high-availability.zookeeper.storageDir",
                            "recovery.zookeeper.storageDir")
                    .withDescription(
                            "File system path (URI) where Flink persists metadata in high-availability setups.");

    // ------------------------------------------------------------------------
    //  Recovery Options
    // ------------------------------------------------------------------------

    /** Optional port (range) used by the job manager in high-availability mode. */
    @Documentation.Section(Documentation.Sections.EXPERT_HIGH_AVAILABILITY)
    public static final ConfigOption<String> HA_JOB_MANAGER_PORT_RANGE =
            key("high-availability.jobmanager.port")
                    .stringType()
                    .defaultValue("0")
                    .withDeprecatedKeys("recovery.jobmanager.port")
                    .withDescription(
                            "The port (range) used by the Flink Master for its RPC connections in highly-available setups. "
                                    + "In highly-available setups, this value is used instead of '"
                                    + JobManagerOptions.PORT.key()
                                    + "'."
                                    + "A value of '0' means that a random free port is chosen. TaskManagers discover this port through "
                                    + "the high-availability services (leader election), so a random port or a port range works "
                                    + "without requiring any additional means of service discovery.");

    // ------------------------------------------------------------------------
    //  ZooKeeper Options
    // ------------------------------------------------------------------------

    /**
     * The ZooKeeper quorum to use, when running Flink in a high-availability mode with ZooKeeper.
     */
    @Documentation.Section(Documentation.Sections.COMMON_HIGH_AVAILABILITY_ZOOKEEPER)
    public static final ConfigOption<String> HA_ZOOKEEPER_QUORUM =
            key("high-availability.zookeeper.quorum")
                    .noDefaultValue()
                    .withDeprecatedKeys("recovery.zookeeper.quorum")
                    .withDescription(
                            "The ZooKeeper quorum to use, when running Flink in a high-availability mode with ZooKeeper.");

    /** The root path under which Flink stores its entries in ZooKeeper. */
    @Documentation.Section(Documentation.Sections.COMMON_HIGH_AVAILABILITY_ZOOKEEPER)
    public static final ConfigOption<String> HA_ZOOKEEPER_ROOT =
            key("high-availability.zookeeper.path.root")
                    .defaultValue("/flink")
                    .withDeprecatedKeys("recovery.zookeeper.path.root")
                    .withDescription(
                            "The root path under which Flink stores its entries in ZooKeeper.");

    /** ZooKeeper root path (ZNode) for job graphs. */
    @Documentation.Section(Documentation.Sections.EXPERT_ZOOKEEPER_HIGH_AVAILABILITY)
    public static final ConfigOption<String> HA_ZOOKEEPER_JOBGRAPHS_PATH =
            key("high-availability.zookeeper.path.jobgraphs")
                    .defaultValue("/jobgraphs")
                    .withDeprecatedKeys("recovery.zookeeper.path.jobgraphs")
                    .withDescription("ZooKeeper root path (ZNode) for job graphs");

    // ------------------------------------------------------------------------
    //  ZooKeeper Client Settings
    // ------------------------------------------------------------------------

    @Documentation.Section(Documentation.Sections.EXPERT_ZOOKEEPER_HIGH_AVAILABILITY)
    public static final ConfigOption<Integer> ZOOKEEPER_SESSION_TIMEOUT =
            key("high-availability.zookeeper.client.session-timeout")
                    .defaultValue(60000)
                    .withDeprecatedKeys("recovery.zookeeper.client.session-timeout")
                    .withDescription(
                            "Defines the session timeout for the ZooKeeper session in ms.");

    @Documentation.Section(Documentation.Sections.EXPERT_ZOOKEEPER_HIGH_AVAILABILITY)
    public static final ConfigOption<Integer> ZOOKEEPER_CONNECTION_TIMEOUT =
            key("high-availability.zookeeper.client.connection-timeout")
                    .defaultValue(15000)
                    .withDeprecatedKeys("recovery.zookeeper.client.connection-timeout")
                    .withDescription("Defines the connection timeout for ZooKeeper in ms.");

    @Documentation.Section(Documentation.Sections.EXPERT_ZOOKEEPER_HIGH_AVAILABILITY)
    public static final ConfigOption<Integer> ZOOKEEPER_RETRY_WAIT =
            key("high-availability.zookeeper.client.retry-wait")
                    .defaultValue(5000)
                    .withDeprecatedKeys("recovery.zookeeper.client.retry-wait")
                    .withDescription("Defines the pause between consecutive retries in ms.");

    @Documentation.Section(Documentation.Sections.EXPERT_ZOOKEEPER_HIGH_AVAILABILITY)
    public static final ConfigOption<Integer> ZOOKEEPER_MAX_RETRY_ATTEMPTS =
            key("high-availability.zookeeper.client.max-retry-attempts")
                    .defaultValue(3)
                    .withDeprecatedKeys("recovery.zookeeper.client.max-retry-attempts")
                    .withDescription(
                            "Defines the number of connection retries before the client gives up.");

    @Documentation.Section(Documentation.Sections.EXPERT_ZOOKEEPER_HIGH_AVAILABILITY)
    public static final ConfigOption<String> ZOOKEEPER_RUNNING_JOB_REGISTRY_PATH =
            key("high-availability.zookeeper.path.running-registry")
                    .defaultValue("/running_job_registry/");

    @Documentation.Section(Documentation.Sections.EXPERT_ZOOKEEPER_HIGH_AVAILABILITY)
    public static final ConfigOption<String> ZOOKEEPER_CLIENT_ACL =
            key("high-availability.zookeeper.client.acl")
                    .defaultValue("open")
                    .withDescription(
                            "Defines the ACL (open|creator) to be configured on ZK node. The configuration value can be"
                                    + " set to “creator” if the ZooKeeper server configuration has the “authProvider” property mapped to use"
                                    + " SASLAuthenticationProvider and the cluster is configured to run in secure mode (Kerberos).");

    @Documentation.Section(Documentation.Sections.EXPERT_ZOOKEEPER_HIGH_AVAILABILITY)
    public static final ConfigOption<Boolean> ZOOKEEPER_TOLERATE_SUSPENDED_CONNECTIONS =
            key("high-availability.zookeeper.client.tolerate-suspended-connections")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            Description.builder()
                                    .text(
                                            "Defines whether a suspended ZooKeeper connection will be treated as an error that causes the leader "
                                                    + "information to be invalidated or not. In case you set this option to %s, Flink will wait until a "
                                                    + "ZooKeeper connection is marked as lost before it revokes the leadership of components. This has the "
                                                    + "effect that Flink is more resilient against temporary connection instabilities at the cost of running "
                                                    + "more likely into timing issues with ZooKeeper.",
                                            TextElement.code("true"))
                                    .build());

    // ------------------------------------------------------------------------
    //  Deprecated options
    // ------------------------------------------------------------------------

    /**
     * The time before a JobManager after a fail over recovers the current jobs.
     *
     * @deprecated Don't use this option anymore. It has no effect on Flink.
     */
    @Deprecated
    public static final ConfigOption<String> HA_JOB_DELAY =
            key("high-availability.job.delay")
                    .noDefaultValue()
                    .withDeprecatedKeys("recovery.job.delay")
                    .withDescription(
                            "The time before a JobManager after a fail over recovers the current jobs.");

    /**
     * Safety hatch to fallback to the old ha services implementations.
     *
     * <p>Ideally, we can remove this option together with the old implementations in the next
     * release.
     *
     * @see <a href="https://issues.apache.org/jira/browse/FLINK-25806">FLINK-25806</a>
     */
    @Documentation.Section(Documentation.Sections.EXPERT_HIGH_AVAILABILITY)
    public static final ConfigOption<Boolean> USE_OLD_HA_SERVICES =
            key("high-availability.use-old-ha-services")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            Description.builder()
                                    .text(
                                            "Use this option to disable the new HA service implementations for ZooKeeper and K8s. This is a safety hatch in case that the new ha services are buggy.")
                                    .build());

    // ------------------------------------------------------------------------

    /** Not intended to be instantiated. */
    private HighAvailabilityOptions() {}
}
