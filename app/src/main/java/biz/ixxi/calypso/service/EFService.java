package biz.ixxi.calypso.service;

import java.util.List;
import java.util.Map;

import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.dto.EF;
import cna.sdk.calypso.commandset.dto.Record;

public interface EFService {
    public List<EF> listEF();

    public List<Record> readRecords(EF file);

    public String getData(enumTagUtils dataType);

    public List<Record> readAllRecords(byte sfi);
}
