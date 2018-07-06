package org.eclipse.keyple.plugin.android.omapi.SeService;

import android.app.Application;

import org.simalliance.openmobileapi.SEService;

/**
 * Created by bonitasoft on 05/07/2018.
 */

public interface SeServiceFactory {

    SEService connectToSe(SEService.CallBack callBack);
}
