package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class InvalidateCmdBuildTest {
    private static final byte[] APDU_ISO_INVALIDATE = ByteArrayUtil.fromHex("0004000000");

    @Test
public void invalidate() {
    InvalidateCmdBuild builder = new InvalidateCmdBuild(PoClass.ISO);
    byte[] apduRequestBytes = builder.getApduRequest().getBytes();
    assertThat(apduRequestBytes).isEqualTo(APDU_ISO_INVALIDATE);
}
}
