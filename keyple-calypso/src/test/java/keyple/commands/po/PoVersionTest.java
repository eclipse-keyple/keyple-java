/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.po;


import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.transaction.PoSecureSession;

// @RunWith(MockitoJUnitRunner.class)
public class PoVersionTest {

    // @InjectMocks
    // ResponseUtils responseUtils;

    // @Mock
    @Test
    public void computePoRevision() {

        // 01h to 04h Calypso Rev 1 or Rev 2 (depending on Application Subtype)
        // 06h to 1Fh Calypso Rev 2
        // 20h to 7Fh Calypso Rev.3
        Assert.assertEquals(PoSecureSession.computePoRevision((byte) 0x01), PoRevision.REV2_4);
        Assert.assertEquals(PoSecureSession.computePoRevision((byte) 0x04), PoRevision.REV2_4);
        Assert.assertEquals(PoSecureSession.computePoRevision((byte) 0x06), PoRevision.REV2_4);
        Assert.assertEquals(PoSecureSession.computePoRevision((byte) 0x1F), PoRevision.REV2_4);
        Assert.assertEquals(PoSecureSession.computePoRevision((byte) 0x20), PoRevision.REV3_1);
        Assert.assertEquals(PoSecureSession.computePoRevision((byte) 0x7F), PoRevision.REV3_2);

    }

}
