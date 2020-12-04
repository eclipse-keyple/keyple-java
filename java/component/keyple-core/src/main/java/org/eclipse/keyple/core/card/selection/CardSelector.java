/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.card.selection;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This POJO holds the information needed to select a particular card.
 *
 * <p>In addition to the card protocol provides two optional structure {@link AidSelector} and
 * {@link AtrFilter} to specify the expected card profile.
 *
 * @since 0.9
 */
public class CardSelector implements Serializable {
  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(CardSelector.class);

  private final String cardProtocol;
  private final AidSelector aidSelector;
  private final AtrFilter atrFilter;

  /**
   * Holds the AID selection data.
   *
   * <p>- AID’s bytes of the card application to select. In case the card application is currently
   * not selected, a logical channel is established and the corresponding card application is
   * selected by the card reader, otherwise keep the current channel. <br>
   *
   * <ul>
   *   <li>optional {@link FileOccurrence} and {@link FileControlInformation} defines selections
   *       modes according to ISO7816-4.
   *   <li>optional successfulSelectionStatusCodes define a list of accepted SW1SW2 codes (in
   *       addition to 9000). Allows, for example, to manage the selection of the invalidated cards.
   *   <li>AidSelector could be missing in CardSelector when operating a card which don’t support
   *       the Select Application command (as it is the case for SAM).
   * </ul>
   *
   * @since 0.9
   */
  public static final class AidSelector implements Serializable {
    public static final int AID_MIN_LENGTH = 5;
    public static final int AID_MAX_LENGTH = 16;

    /**
     * FileOccurrence indicates how to carry out the file occurrence in accordance with ISO7816-4
     *
     * <p>The getIsoBitMask method provides the bit mask to be used to set P2 in the select command
     * (ISO/IEC 7816-4.2)
     *
     * @since 0.9
     */
    public enum FileOccurrence {
      FIRST((byte) 0x00),
      LAST((byte) 0x01),
      NEXT((byte) 0x02),
      PREVIOUS((byte) 0x03);

      private final byte isoBitMask;

      FileOccurrence(byte isoBitMask) {
        this.isoBitMask = isoBitMask;
      }

      public byte getIsoBitMask() {
        return isoBitMask;
      }
    }

    /**
     * FileControlInformation indicates how to which template is expected in accordance with
     * ISO7816-4
     *
     * <p>The getIsoBitMask method provides the bit mask to be used to set P2 in the select command
     * (ISO/IEC 7816-4.2)
     *
     * @since 0.9
     */
    public enum FileControlInformation {
      FCI(((byte) 0x00)),
      FCP(((byte) 0x04)),
      FMD(((byte) 0x08)),
      NO_RESPONSE(((byte) 0x0C));

      private final byte isoBitMask;

      FileControlInformation(byte isoBitMask) {
        this.isoBitMask = isoBitMask;
      }

      public byte getIsoBitMask() {
        return isoBitMask;
      }
    }

    private final FileOccurrence fileOccurrence;
    private final FileControlInformation fileControlInformation;

    private final byte[] aidToSelect;

    /**
     * List of status codes in response to the select application command that should be considered
     * successful although they are different from 9000
     *
     * @since 0.9
     */
    private Set<Integer> successfulSelectionStatusCodes;

    /** Private constructor */
    private AidSelector(AidSelectorBuilder builder) {
      this.aidToSelect = builder.aidToSelect;
      this.fileOccurrence = builder.fileOccurrence;
      this.fileControlInformation = builder.fileControlInformation;
      this.successfulSelectionStatusCodes = null;
    }

    /**
     * Builder of {@link AidSelector}
     *
     * @since 0.9
     */
    public static class AidSelectorBuilder {
      private byte[] aidToSelect;
      private FileOccurrence fileOccurrence = FileOccurrence.FIRST;
      private FileControlInformation fileControlInformation = FileControlInformation.FCI;

      /** Private constructor */
      private AidSelectorBuilder() {}

      /**
       * Sets the AID
       *
       * @param aid the AID as an array of bytes
       * @return the builder instance
       * @since 0.9
       */
      public AidSelectorBuilder aidToSelect(byte[] aid) {
        if (aid.length < AID_MIN_LENGTH || aid.length > AID_MAX_LENGTH) {
          aidToSelect = null;
          throw new IllegalArgumentException(
              "Bad AID length: "
                  + aid.length
                  + ". The AID length should be "
                  + "between 5 and 15.");
        } else {
          aidToSelect = aid;
        }
        return this;
      }

      /**
       * Sets the AID
       *
       * @param aid the AID as an hex string
       * @return the builder instance
       * @since 0.9
       */
      public AidSelectorBuilder aidToSelect(String aid) {
        return this.aidToSelect(ByteArrayUtil.fromHex(aid));
      }

      /**
       * Sets the file occurence mode (see ISO7816-4)
       *
       * @param fileOccurrence the {@link FileOccurrence}
       * @return the builder instance
       * @since 0.9
       */
      public AidSelectorBuilder fileOccurrence(
          CardSelector.AidSelector.FileOccurrence fileOccurrence) {
        this.fileOccurrence = fileOccurrence;
        return this;
      }

      /**
       * Sets the file control mode (see ISO7816-4)
       *
       * @param fileControlInformation the {@link FileControlInformation}
       * @return the builder instance
       * @since 0.9
       */
      public AidSelectorBuilder fileControlInformation(
          CardSelector.AidSelector.FileControlInformation fileControlInformation) {
        this.fileControlInformation = fileControlInformation;
        return this;
      }

      /**
       * Build a new {@code AidSelector}.
       *
       * @return a new instance
       * @since 0.9
       */
      public CardSelector.AidSelector build() {
        return new CardSelector.AidSelector(this);
      }
    }

    /**
     * Gets a new builder.
     *
     * @return a new builder instance
     * @since 0.9
     */
    public static AidSelectorBuilder builder() {
      return new AidSelectorBuilder();
    }

    /**
     * Getter for the AID provided at construction time
     *
     * @return byte array containing the AID
     * @since 0.9
     */
    public byte[] getAidToSelect() {
      return aidToSelect;
    }

    /**
     * @return the file occurrence parameter
     * @since 0.9
     */
    public FileOccurrence getFileOccurrence() {
      return fileOccurrence;
    }

    /**
     * @return the file control information parameter
     * @since 0.9
     */
    public FileControlInformation getFileControlInformation() {
      return fileControlInformation;
    }

    /**
     * Gets the list of successful selection status codes
     *
     * @return the list of status codes
     * @since 0.9
     */
    public Set<Integer> getSuccessfulSelectionStatusCodes() {
      return successfulSelectionStatusCodes;
    }

    /**
     * Add as status code to be accepted to the list of successful selection status codes
     *
     * @param statusCode the status code to be accepted
     * @since 0.9
     */
    public void addSuccessfulStatusCode(int statusCode) {
      // the list is kept null until a code is added
      if (this.successfulSelectionStatusCodes == null) {
        this.successfulSelectionStatusCodes = new LinkedHashSet<Integer>();
      }
      this.successfulSelectionStatusCodes.add(statusCode);
    }

    /**
     * Print out the AID in hex
     *
     * @return a string
     * @since 0.9
     */
    @Override
    public String toString() {
      return "AidSelector{"
          + "aidToSelect="
          + ByteArrayUtil.toHex(aidToSelect)
          + ", fileOccurrence="
          + fileOccurrence
          + ", fileControlInformation="
          + fileControlInformation
          + ", successfulSelectionStatusCodes="
          + successfulSelectionStatusCodes
          + '}';
    }
  }

  /**
   * Static nested class to hold the data elements used to perform an ATR based filtering
   *
   * @since 0.9
   */
  public static final class AtrFilter implements Serializable {
    /**
     * Regular expression dedicated to handle the card logical channel opening based on ATR pattern
     */
    private String atrRegex;

    /**
     * Regular expression based filter
     *
     * @param atrRegex String hex regular expression
     * @since 0.9
     */
    public AtrFilter(String atrRegex) {
      this.atrRegex = atrRegex;
    }

    /**
     * Setter for the regular expression provided at construction time
     *
     * @param atrRegex expression string
     * @since 0.9
     */
    public void setAtrRegex(String atrRegex) {
      this.atrRegex = atrRegex;
    }

    /**
     * Getter for the regular expression provided at construction time
     *
     * @return Regular expression string
     * @since 0.9
     */
    public String getAtrRegex() {
      return atrRegex;
    }

    /**
     * Tells if the provided ATR matches the registered regular expression
     *
     * <p>If the registered regular expression is empty, the ATR is always matching.
     *
     * @param atr a buffer containing the ATR to be checked
     * @return a boolean true the ATR matches the current regex
     * @since 0.9
     */
    public boolean atrMatches(byte[] atr) {
      boolean m;
      if (atrRegex.length() != 0) {
        Pattern p = Pattern.compile(atrRegex);
        String atrString = ByteArrayUtil.toHex(atr);
        m = p.matcher(atrString).matches();
      } else {
        m = true;
      }
      return m;
    }

    /**
     * Print out the ATR regex
     *
     * @return a string
     * @since 0.9
     */
    @Override
    public String toString() {
      return "AtrFilter{" + "atrRegex='" + atrRegex + '\'' + '}';
    }
  }

  /**
   * Protected constructor
   *
   * @param builder the CardSelector builder
   * @since 0.9
   */
  protected CardSelector(CardSelectorBuilder builder) {
    this.cardProtocol = builder.cardProtocol;
    this.aidSelector = builder.aidSelector;
    this.atrFilter = builder.atrFilter;
    if (logger.isTraceEnabled()) {
      logger.trace(
          "Selection data: AID = {}, ATRREGEX = {}",
          (this.aidSelector == null || this.aidSelector.getAidToSelect() == null)
              ? "null"
              : ByteArrayUtil.toHex(this.aidSelector.getAidToSelect()),
          this.atrFilter == null ? "null" : this.atrFilter.getAtrRegex());
    }
  }

  /**
   * Create a CardSelector to perform the card selection<br>
   *
   * @since 0.9
   */
  public static class CardSelectorBuilder {
    private String cardProtocol;
    private CardSelector.AtrFilter atrFilter;
    private CardSelector.AidSelector aidSelector;

    /** Protected constructor */
    protected CardSelectorBuilder() {}

    /**
     * Sets the card protocol.
     *
     * @param cardProtocol A not empty String.
     * @return the builder instance
     * @since 0.9
     */
    public CardSelectorBuilder cardProtocol(String cardProtocol) {
      this.cardProtocol = cardProtocol;
      return this;
    }

    /**
     * Sets the card ATR Filter
     *
     * @param atrFilter the {@link AtrFilter} of the targeted card
     * @return the builder instance
     * @since 0.9
     */
    public CardSelectorBuilder atrFilter(CardSelector.AtrFilter atrFilter) {
      this.atrFilter = atrFilter;
      return this;
    }

    /**
     * Sets the card AID Selector
     *
     * @param aidSelector the {@link AidSelector} of the targeted card
     * @return the builder instance
     * @since 0.9
     */
    public CardSelectorBuilder aidSelector(CardSelector.AidSelector aidSelector) {
      this.aidSelector = aidSelector;
      return this;
    }

    /**
     * Build a new {@code CardSelector}.
     *
     * @return a new instance
     * @since 0.9
     */
    public CardSelector build() {
      return new CardSelector(this);
    }
  }

  /**
   * Gets a new builder.
   *
   * @return a new builder instance
   * @since 0.9
   */
  public static CardSelectorBuilder builder() {
    return new CardSelectorBuilder();
  }

  /**
   * Gets the card protocol name.
   *
   * @return the {@link String} provided at construction time
   * @since 0.9
   */
  public String getCardProtocol() {
    return cardProtocol;
  }

  /**
   * Gets the ATR filter
   *
   * @return The {@link AtrFilter} (can be null)
   * @since 0.9
   */
  public AtrFilter getAtrFilter() {
    return atrFilter;
  }

  /**
   * Gets the AID selector
   *
   * @return The {@link AidSelector} (can be null)
   * @since 0.9
   */
  public AidSelector getAidSelector() {
    return aidSelector;
  }

  @Override
  public String toString() {
    return "CardSelector{"
        + "cardProtocol="
        + cardProtocol
        + ", aidSelector="
        + aidSelector
        + ", atrFilter="
        + atrFilter
        + '}';
  }
}
