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
package org.eclipse.keyple.core.card.message;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * This POJO wraps the card's response to an ApduRequest.
 *
 * <p>The ApduResponse is built by a reader from an array of bytes received from the card in
 * response to an ApduRequest. The status code and the success status of the command are retrieved
 * from the data.
 *
 * @since 0.9
 */
public final class ApduResponse implements Serializable {

  private final byte[] bytes;
  private final int statusCode;
  private final boolean successful;

  /**
   * Constructor.
   *
   * <p>The internal successful status is determined by the current status code (sw1sw2) and the
   * optional successful status codes list.
   *
   * <p>The list of additional successful status codes is used to possibly set the successful flag
   * only if not equal to 0x9000.
   *
   * @param buffer A byte array (must be not null)
   * @param successfulStatusCodes An optional Set of Integer (may be null)
   * @since 0.9
   */
  public ApduResponse(byte[] buffer, Set<Integer> successfulStatusCodes) {

    this.bytes = buffer;
    if (buffer.length < 2) {
      throw new IllegalArgumentException(
          "Building an ApduResponse with a illegal buffer (length must be > 2): " + buffer.length);
    }
    statusCode =
        ((buffer[buffer.length - 2] & 0x000000FF) << 8) + (buffer[buffer.length - 1] & 0x000000FF);

    if (successfulStatusCodes != null) {
      this.successful = statusCode == 0x9000 || successfulStatusCodes.contains(statusCode);
    } else {
      this.successful = statusCode == 0x9000;
    }
  }

  /**
   * Tells if the current APDU is successful or not.
   *
   * @return True if the current APDU is successful, false if not.
   * @since 0.9
   */
  public boolean isSuccessful() {
    return successful;
  }

  /**
   * Gets the status code SW1SW2 of the APDU.
   *
   * @return A int
   * @since 0.9
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Gets all the data received from the card including SW1SW2.
   *
   * @return A not null byte array.
   * @since 0.9
   */
  public byte[] getBytes() {
    return this.bytes;
  }

  /**
   * Get the data received from the card excluding SW1SW2.
   *
   * @return A not null byte array.
   * @since 0.9
   */
  public byte[] getDataOut() {
    return Arrays.copyOfRange(this.bytes, 0, this.bytes.length - 2);
  }

  @Override
  public String toString() {
    String prefix;
    if (isSuccessful()) {
      prefix = "ApduResponse: SUCCESS, RAWDATA = ";
    } else {
      prefix = "ApduResponse: FAILURE, RAWDATA = ";
    }
    return prefix + ByteArrayUtil.toHex(this.bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ApduResponse that = (ApduResponse) o;

    if (successful != that.successful) return false;
    return Arrays.equals(bytes, that.bytes);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(bytes);
    result = 31 * result + (successful ? 1 : 0);
    return result;
  }
}
