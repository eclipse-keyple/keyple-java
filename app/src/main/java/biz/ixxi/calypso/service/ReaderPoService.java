package biz.ixxi.calypso.service;

import cna.sdk.seproxy.APDURequest;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ProxyReader;
import cna.sdk.seproxy.SERequest;
import cna.sdk.seproxy.SEResponse;

public interface ReaderPoService {

    public SEResponse transmit(SERequest seRequest);

    public APDUResponse transmit(APDURequest apduRequest);
    
    public ProxyReader getReader();
}
