/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.integration.poData;

import org.eclipse.keyple.integration.IntegrationUtils;

public class AccessConditions {

    public class AccessCondition {

        private String accessCondition;

        private String keyLevel;

        private String description;

        public AccessCondition(byte inAc, byte inKl) {

            accessCondition = String.format("%02X", inAc);

            keyLevel = String.format("%02X", inKl);

            description = IntegrationUtils.getAcName(accessCondition, keyLevel, true);
        }

        public String getAccessCondition() {
            return accessCondition;
        }

        public String getKeyLevel() {
            return keyLevel;
        }

        public String getDescription() {
            return description;
        }
    }

    private AccessCondition group0;

    private AccessCondition group1;

    private AccessCondition group2;

    private AccessCondition group3;

    public AccessConditions(byte[] accessConditions, byte[] keyLevels) {

        group0 = new AccessCondition(accessConditions[0], keyLevels[0]);

        group1 = new AccessCondition(accessConditions[1], keyLevels[1]);

        group2 = new AccessCondition(accessConditions[2], keyLevels[2]);

        group3 = new AccessCondition(accessConditions[3], keyLevels[3]);
    }

    public AccessCondition getGroup0() {
        return group0;
    }

    public AccessCondition getGroup1() {
        return group1;
    }

    public AccessCondition getGroup2() {
        return group2;
    }

    public AccessCondition getGroup3() {
        return group3;
    }
}
