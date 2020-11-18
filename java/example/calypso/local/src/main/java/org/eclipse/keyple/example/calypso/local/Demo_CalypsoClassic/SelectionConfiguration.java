package org.eclipse.keyple.example.calypso.local.Demo_CalypsoClassic;

import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.calypso.local.common.CalypsoClassicInfo;

public class SelectionConfiguration {

    static public CardSelection preparePoSelection() {
        /*
         * Initialize the selection process
         */
        CardSelection cardSelection = new CardSelection();

        /* operate multiple PO selections */
        String poFakeAid1 = "AABBCCDDEE"; // fake AID 1
        String poFakeAid2 = "EEDDCCBBAA"; // fake AID 2

        /*
         * Add selection case 1: Fake AID1, protocol ISO, target rev 3
         */
        cardSelection.prepareSelection(
                new PoSelectionRequest(
                        PoSelector.builder()
                                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poFakeAid1).build())
                                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                                .build()));

        /*
         * Add selection case 2: Calypso application, protocol ISO, target rev 2 or 3
         *
         * addition of read commands to execute following the selection
         */
        PoSelectionRequest poSelectionRequestCalypsoAid =
                new PoSelectionRequest(
                        PoSelector.builder()
                                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                                .invalidatedPo(PoSelector.InvalidatedPo.ACCEPT)
                                .build());

        poSelectionRequestCalypsoAid.prepareSelectFile(CalypsoClassicInfo.LID_DF_RT);

        poSelectionRequestCalypsoAid.prepareSelectFile(CalypsoClassicInfo.LID_EventLog);

        poSelectionRequestCalypsoAid.prepareReadRecordFile(
                CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

        cardSelection.prepareSelection(poSelectionRequestCalypsoAid);

        /*
         * Add selection case 3: Fake AID2, unspecified protocol, target rev 2 or 3
         */
        cardSelection.prepareSelection(
                new PoSelectionRequest(
                        PoSelector.builder()
                                .cardProtocol(ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name())
                                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poFakeAid2).build())
                                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                                .build()));

        /*
         * Add selection case 4: ATR selection, rev 1 atrregex
         */
        cardSelection.prepareSelection(
                new PoSelectionRequest(
                        PoSelector.builder()
                                .cardProtocol(ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name())
                                .atrFilter(new CardSelector.AtrFilter(CalypsoClassicInfo.ATR_REV1_REGEX))
                                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                                .build()));

        return cardSelection;
    }
}
