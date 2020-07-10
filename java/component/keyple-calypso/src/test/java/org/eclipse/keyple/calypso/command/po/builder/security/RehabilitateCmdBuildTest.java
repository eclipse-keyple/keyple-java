package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class RehabilitateCmdBuildTest {
    private static final byte[] APDU_ISO_REHABILITATE = ByteArrayUtil.fromHex("0044000000");

    @Test
public void rehabilitate() {
    RehabilitateCmdBuild builder = new RehabilitateCmdBuild(PoClass.ISO);
    byte[] apduRequestBytes = builder.getApduRequest().getBytes();
    assertThat(apduRequestBytes).isEqualTo(APDU_ISO_REHABILITATE);
}
}
