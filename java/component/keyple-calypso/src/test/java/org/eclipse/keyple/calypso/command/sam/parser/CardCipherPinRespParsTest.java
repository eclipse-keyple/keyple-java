package org.eclipse.keyple.calypso.command.sam.parser;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.shouldHaveThrown;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.command.sam.parser.security.CardCipherPinRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class CardCipherPinRespParsTest {
    private static final String SW1SW2_KO = "6A82";
    private static final String SW1SW2_OK = "9000";
    private static final String CIPHERED_DATA = "1122334455667788";

    @Test
    public void cardCipherPinRespPars_goodStatus() {
        CardCipherPinRespPars parser = new CardCipherPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(CIPHERED_DATA + SW1SW2_OK), null), null);
        parser.checkStatus();
        assertThat(parser.getCipheredData()).isEqualTo(ByteArrayUtil.fromHex(CIPHERED_DATA));
    }

    @Test(expected = CalypsoSamCommandException.class)
    public void cardCipherPinRespPars_badStatus() {
        CardCipherPinRespPars parser = new CardCipherPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null), null);
        parser.checkStatus();
        shouldHaveThrown(CalypsoSamCommandException.class);
    }
}