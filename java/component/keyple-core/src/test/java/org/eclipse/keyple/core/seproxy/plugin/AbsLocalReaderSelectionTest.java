package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Se Selection Test for AbstractLocalReader
 */
public class AbsLocalReaderSelectionTest extends CoreBaseTest {


    private static final Logger logger = LoggerFactory.getLogger(AbsLocalReaderSelectionTest.class);


    final String PLUGIN_NAME = "AbstractLocalReaderTestP";
    final String READER_NAME = "AbstractLocalReaderTest";

    final byte[] AID = ByteArrayUtil.fromHex("00 00 00 00 00");
    final Set<Integer> STATUS_CODE = new HashSet(Arrays.asList(1, 2));
    final byte[] SUCCESS = ByteArrayUtil.fromHex("90 00");
    final byte[] FAIL = ByteArrayUtil.fromHex("00 00");



    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }


    /*
     * Select by ATR
     */

    @Test
    public void select_byAtr_success() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        //mock ATR
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex("0000"));

        SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter("0000");

        SeSelector seSelector = new SeSelector(
                null,
                atrFilter,
                null,
                "extraInfo");

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(true, status.hasMatched());

    }

    @Test
    public void select_byAtr_fail() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        //mock ATR
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex("1000"));

        SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter("0000");

        SeSelector seSelector = new SeSelector(
                null,
                atrFilter,
                null,
                "extraInfo");

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(false, status.hasMatched());

    }

    @Test(expected = KeypleIOReaderException.class)
    public void select_byAtr_null() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        //mock ATR
        when(r.getATR()).thenReturn(null);

        SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter("0000");

        SeSelector seSelector = new SeSelector(
                null,
                atrFilter,
                null,
                "extraInfo");

        r.openLogicalChannel(seSelector);
        //expected exception
    }

    /*
     * Select by AID
     */

    @Test
    public void select_byAid_success() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        when(r.transmitApdu(any(byte[].class))).thenReturn(SUCCESS);

        SeSelector.AidSelector aidSelector = new SeSelector.AidSelector(
                new SeSelector.AidSelector.IsoAid(AID),
                STATUS_CODE
        );

        SeSelector seSelector = new SeSelector(
                null,
                null,
                aidSelector,
                "extraInfo");

        SelectionStatus status = r.openLogicalChannel(seSelector);
        Assert.assertEquals(true, status.hasMatched());
    }

    @Test
    public void select_byAid_fail() throws Exception {
        AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        when(r.transmitApdu(any(byte[].class))).thenReturn(FAIL);

        SeSelector.AidSelector aidSelector = new SeSelector.AidSelector(
                new SeSelector.AidSelector.IsoAid(AID),
                STATUS_CODE
        );

        SeSelector seSelector = new SeSelector(
                null,
                null,
                aidSelector,
                "extraInfo");

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

        when(r.openChannelForAid(any(SeSelector.AidSelector.class))).thenReturn(new ApduResponse(SUCCESS, STATUS_CODE));
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex("0000"));
        when(r.transmitApdu(any(byte[].class))).thenReturn(SUCCESS);

        SeSelector.AidSelector aidSelector = new SeSelector.AidSelector(
                new SeSelector.AidSelector.IsoAid(AID),
                STATUS_CODE
        );

        SeSelector seSelector = new SeSelector(
                null,
                null,
                aidSelector,
                "extraInfo");

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
        when(r.transmitApdu(any(byte[].class))).thenReturn(SUCCESS);

        SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter("0000");
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

}
