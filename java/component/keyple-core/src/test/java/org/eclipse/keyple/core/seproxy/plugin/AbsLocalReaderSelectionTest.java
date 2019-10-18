package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Se Selection Test for AbstractLocalReader
 */
public class AbsLocalReaderSelectionTest extends CoreBaseTest {


    private static final Logger logger = LoggerFactory.getLogger(AbsLocalReaderSelectionTest.class);


    final String PLUGIN_NAME = "AbstractLocalReaderTestP";
    final String READER_NAME = "AbstractLocalReaderTest";

    static final String AID = "A000000291A000000191";
    static final Set<Integer> STATUS_CODE = new HashSet(Arrays.asList(1, 2));

    static final String ATR = "0000";


    final byte[] RESP_SUCCESS = ByteArrayUtil.fromHex("90 00");
    final byte[] RESP_FAIL = ByteArrayUtil.fromHex("00 00");



    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }


    /** ==== Card presence management ====================================== */

    @Test
    public void isSePresent_false() throws Exception, NoStackTraceThrowable {
        AbstractLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        when(r.checkSePresence()).thenReturn(false);
        //test
        Assert.assertFalse(r.isSePresent());
    }

    @Test
    public void isSePresent_true() throws Exception, NoStackTraceThrowable {
        AbstractLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        when(r.checkSePresence()).thenReturn(true);
        //test
        Assert.assertTrue(r.isSePresent());
    }




    /*
     * Select by ATR
     */

    @Test
    public void select_byAtr_success() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        //mock ATR
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex(ATR));

        SeSelector seSelector = getAtrSelector();

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(true, status.hasMatched());

        //TODO OD : hard to understand why isLogicalChannelOpen is false after openLogicalChannel
        Assert.assertEquals(false, r.isLogicalChannelOpen()); //channel is open only when processSeRequest
    }

    @Test
    public void select_byAtr_fail() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        //mock ATR to fail
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex("1000"));

        SeSelector seSelector = getAtrSelector();

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(false, status.hasMatched());

    }

    @Test(expected = KeypleIOReaderException.class)
    public void select_byAtr_null() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        //mock ATR
        when(r.getATR()).thenReturn(null);

        SeSelector seSelector = getAtrSelector();

        r.openLogicalChannel(seSelector);
        //expected exception
    }

    /*
     * Select by AID
     */

    @Test
    public void select_byAid_success() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        when(r.transmitApdu(any(byte[].class))).thenReturn(RESP_SUCCESS);

        SeSelector seSelector = getAidSelector();

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(true, status.hasMatched());
    }

    @Test
    public void select_byAid_fail() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        when(r.transmitApdu(any(byte[].class))).thenReturn(RESP_FAIL);

        SeSelector seSelector = getAidSelector();

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(false, status.hasMatched());
    }

    /*
     *  Select by AID -- Smart Selection interface
     */
    @Test
    public void select_bySmartAid_success() throws Exception {
        //use a SmartSelectionReader object
        BlankSmartSelectionReader r = getSmartSpy(PLUGIN_NAME, READER_NAME);

        when(r.openChannelForAid(any(SeSelector.AidSelector.class))).thenReturn(new ApduResponse(RESP_SUCCESS, STATUS_CODE));
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex(ATR));
        when(r.transmitApdu(any(byte[].class))).thenReturn(RESP_SUCCESS);

        SeSelector seSelector = getAidSelector();

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(true, status.hasMatched());
    }

    /*
     * Select by Atr and Aid
     */

    //atr fail, aid success
    //TODO OD : check this, it seems that this case is no treated
    //@Test
    public void select_byAtrAndAid_success() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        //mock ATR to fail
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex("1000"));
        //mock aid to success
        when(r.transmitApdu(any(byte[].class))).thenReturn(RESP_SUCCESS);

        SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter(ATR);
        SeSelector.AidSelector aidSelector = new SeSelector.AidSelector(
                new SeSelector.AidSelector.IsoAid(AID),
                STATUS_CODE
        );

        //select both
        SeSelector seSelector = new SeSelector(
                null,
                atrFilter,
                aidSelector,
                "extraInfo");

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(true, status.hasMatched());
    }

    /*
     * Select by null null
     */

    //TODO OD:is this normal ? check this
    @Test
    public void select_no_param() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);

        SeSelector seSelector = new SeSelector(
                null,
                null,
                null,
                "extraInfo");

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(true, status.hasMatched());
    }



    /*
     * open logical channel
     */

    @Test(expected = KeypleApplicationSelectionException.class)
    public void open_channel_null() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        r.openLogicalChannelAndSelect(null);
        //expected exception
    }

    @Test
    public void open_logical_channel_success() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex(ATR));
        when(r.isLogicalChannelOpen()).thenReturn(true);
        when(r.isPhysicalChannelOpen()).thenReturn(true);

        SeSelector seSelector = getAtrSelector();

        r.openLogicalChannelAndSelect(seSelector);
        verify(r, times(1)).openLogicalChannel(seSelector);
    }

    @Test(expected = KeypleChannelControlException.class)
    public void open_channel_fail() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex(ATR));
        when(r.isLogicalChannelOpen()).thenReturn(false);
        when(r.isPhysicalChannelOpen()).thenReturn(false);//does not open

        SeSelector seSelector = getAtrSelector();

        r.openLogicalChannelAndSelect(seSelector);
        verify(r, times(0)).openLogicalChannel(seSelector);

    }

    /*
     * add Se Protocol Setting
     */

    @Test
    public void add_SeProtocolSetting() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        String protocolRule = "any";
        r.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4, protocolRule);
        Assert.assertEquals(protocolRule, r.protocolsMap.get(SeCommonProtocols.PROTOCOL_ISO14443_4));
    }

    @Test
    public void set_SeProtocolSetting() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        String protocolRule = "any";
        Map protocols = new HashMap();
        protocols.put(SeCommonProtocols.PROTOCOL_ISO14443_4, protocolRule);
        r.setSeProtocolSetting(protocols);
        Assert.assertEquals(protocolRule, r.protocolsMap.get(SeCommonProtocols.PROTOCOL_ISO14443_4));
    }



    /*
        HELPERS
     */


    /**
     * Return a basic spy reader
     * @param pluginName
     * @param readerName
     * @return  basic spy reader
     * @throws KeypleReaderException
     */
    static public AbstractLocalReader getSpy(String pluginName, String readerName) throws KeypleReaderException {
        AbstractLocalReader r =  Mockito.spy(new BlankAbstractLocalReader(pluginName,readerName));
        return  r;
    }

    static public BlankSmartSelectionReader getSmartSpy(String pluginName, String readerName) throws KeypleReaderException {
        BlankSmartSelectionReader r =  Mockito.spy(new BlankSmartSelectionReader(pluginName,readerName));
        return  r;
    }

    static public SeSelector getAidSelector(){
        SeSelector.AidSelector aidSelector = new SeSelector.AidSelector(
                new SeSelector.AidSelector.IsoAid(AID),
                STATUS_CODE
        );

        return new SeSelector(
                null,
                null,
                aidSelector,
                "aidSelector : " + AID);
    }

    static public SeSelector getAtrSelector(){

        SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter(ATR);

        return new SeSelector(
                null,
                atrFilter,
                null,
                "atrFilter");
    }


}
