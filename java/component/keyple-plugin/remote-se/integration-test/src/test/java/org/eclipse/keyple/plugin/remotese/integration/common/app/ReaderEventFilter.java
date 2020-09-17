/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.integration.common.app;

import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientReaderEventFilter;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleDoNotPropagateEventException;
import org.eclipse.keyple.plugin.remotese.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;

public class ReaderEventFilter implements KeypleClientReaderEventFilter {

  public TransactionResult transactionResult;
  public UserInput user;

  public void setUser(UserInput user) {
    this.user = user;
  }

  @Override
  public Object beforePropagation(ReaderEvent event) throws KeypleDoNotPropagateEventException {
    switch (event.getEventType()) {
      case SE_MATCHED:
        return new UserInput().setUserId(user.getUserId());
      case SE_REMOVED:
      case SE_INSERTED:
      default:
        throw new KeypleDoNotPropagateEventException("only SE Matched is propagated");
    }
  }

  @Override
  public Class<? extends Object> getUserOutputDataClass() {
    return TransactionResult.class;
  }

  @Override
  public void afterPropagation(Object userOutputData) {
    transactionResult = (TransactionResult) userOutputData;
  }
};
