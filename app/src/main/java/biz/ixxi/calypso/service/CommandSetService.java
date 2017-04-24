package biz.ixxi.calypso.service;

import java.util.List;

import cna.sdk.calypso.commandset.enumCmdReadRecords;
import cna.sdk.calypso.commandset.enumCmdWriteRecords;
import cna.sdk.calypso.commandset.enumSFI;
import cna.sdk.calypso.commandset.dto.Record;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.seproxy.ReaderException;

public interface CommandSetService {

	public List<String> openSession(byte sfiToSelect);

	public String closeSession() throws ReaderException;

	public String cancelSession() throws ReaderException;

	public List<Record> readRecord(PoRevision revision, byte recordNumber, enumSFI sfi, enumCmdReadRecords cmd) throws ReaderException;

	public PoRevision getVersion();

//	public void digestUpdate(byte[]bytes);

	public void updateRecord(String stringRecordToWrite, byte numberRec , enumSFI sfi, enumCmdWriteRecords cmd) throws ReaderException;

	public String isCardPresent();

}
