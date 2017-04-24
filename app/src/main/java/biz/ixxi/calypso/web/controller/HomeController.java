package biz.ixxi.calypso.web.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import biz.ixxi.calypso.service.CommandSetService;
import biz.ixxi.calypso.service.EFService;
import cna.sdk.calypso.commandset.enumCmdReadRecords;
import cna.sdk.calypso.commandset.enumCmdWriteRecords;
import cna.sdk.calypso.commandset.enumSFI;
import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.dto.EF;
import cna.sdk.calypso.commandset.dto.Record;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.seproxy.ReaderException;

@Controller
public class HomeController {

    private final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Inject
    CommandSetService commandSetService;

    @Inject
    EFService efService;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("test", "test");
        return "home";
    }

    @ResponseBody
    @RequestMapping(value = "/openSession/{sfiToSelect}", method = RequestMethod.GET)
    public List<String> openSession(@PathVariable String sfiToSelect) throws ReaderException {
        byte sfi = new Byte(sfiToSelect).byteValue();

        return commandSetService.openSession(sfi);
    }

    @ResponseBody
    @RequestMapping("/closeSession")
    public String closeSession(Model model) throws ReaderException {
        return commandSetService.closeSession();
    }

    @ResponseBody
    @RequestMapping("/cancelSession")
    public String cancelSession(Model model) throws ReaderException {
        return commandSetService.cancelSession();
    }

    @ResponseBody
    @RequestMapping(value = "/info/{file}/{numberRec}/{enumCmd}", method = RequestMethod.GET)
    public String getData(@PathVariable String file, @PathVariable String numberRec, @PathVariable String enumCmd)
            throws ReaderException {
        enumCmdReadRecords cmdEnum = enumCmdReadRecords.getCmdByName(enumCmd);
        enumSFI sfiEnum = getSFI(file);

        log.info("test getData: " + file);
        byte recNum = new Byte(numberRec).byteValue();

        List<Record> response = commandSetService.readRecord(PoRevision.REV2_4, recNum, sfiEnum, cmdEnum);
        return formatbyte(response);
    }

    private static String formatbyte(List<Record> data) {
        StringBuffer sb = new StringBuffer();
        for (Record record : data) {

            sb.append(record.toString() + " [" + new String(record.getData(), StandardCharsets.US_ASCII) + "]");
        }

        return sb.toString();
    }

    @ResponseBody
    @RequestMapping("/getVersion")
    public PoRevision getRevision(Model model) {
        return commandSetService.getVersion();
    }

    @ResponseBody
    @RequestMapping("/listEF")
    public List<Map<String, Object>> getEfList(Model model) {
        List<EF> list = efService.listEF();

        List<Map<String, Object>> ret = new ArrayList<>();
        for (EF ef : list) {
            Map<String, Object> m = new HashMap<>();
            // {"lid":[32,4],"sfi":"UNKNOWN_FILE","fileType":2,"recSize":16,"numberRec":1,"fileTypeName":"Linear
            // file"}
            m.put("lid", ef.getLid());
            m.put("sfi", ef.getSfi().getSfi());
            m.put("sfiName", ef.getSfi().getName());
            m.put("fileType", ef.getFileType());
            m.put("recSize", ef.getRecSize());
            m.put("numberRec", ef.getNumberRec());
            m.put("fileTypeName", ef.getFileTypeName());
            ret.add(m);
        }
        return ret;
    }

    @ResponseBody
    @RequestMapping(value = "/getData/{dataType}")
    public String getData(@PathVariable String dataType) {
        enumTagUtils data = enumTagUtils.getTagByName(dataType);

        return efService.getData(data);
    }

    @ResponseBody
    @RequestMapping(value = "/updateRecord/{stringRecordToWrite}/{file}/{numberRec}/{enumCmd}", method = RequestMethod.POST)
    public String updateRecord(@PathVariable String stringRecordToWrite, @PathVariable String file,
            @PathVariable String numberRec, @PathVariable String enumCmd) throws ReaderException {
        log.info("" + file);
        log.info("" + numberRec);
        byte recNum;
        String msg;
        enumSFI sfiEnum = getSFI(file);
        enumCmdReadRecords cmdEnum = enumCmdReadRecords.getCmdByName(enumCmd);
        enumCmdWriteRecords cmdWEnum = cmdEnum.equals(enumCmdReadRecords.READ_ONE_RECORD)
                || cmdEnum.equals(enumCmdReadRecords.READ_RECORDS) ? enumCmdWriteRecords.WRITE_RECORD
                        : enumCmdWriteRecords.WRITE_RECORD_USING_SFI;

        if (sfiEnum.equals(enumSFI.EVENT_LOG_FILE) && !numberRec.equals("01")) {
            recNum = 0x00;
            msg = "Command forbidden on cyclic files when the record exists and is not record 01h and on binary files.";
            log.info("error: cyclic file");
        } else {
            recNum = new Byte(numberRec).byteValue();
            commandSetService.updateRecord(stringRecordToWrite, recNum, sfiEnum, cmdWEnum);
            msg = "Card updated ";
        }
        return msg;
    }

    @ResponseBody
    @RequestMapping(value = "/ReadRecord", method = RequestMethod.POST)
    public List<Record> getReadRecord(EF file) {

        return efService.readRecords(file);
    }

    @ResponseBody
    @RequestMapping(value = "/ReadInSession", method = RequestMethod.POST)
    public String getReadInSession(HttpServletResponse response, String sfi) {
        long start = System.currentTimeMillis();
        commandSetService.openSession(enumSFI.getSfiByCode(Byte.decode("0x" + sfi)).getSfi());
        List<Record> record = efService.readAllRecords(enumSFI.getSfiByCode(Byte.decode("0x" + sfi)).getSfi());
        try {
            commandSetService.closeSession();
        } catch (ReaderException e) {
            return null;
        }

        response.addHeader("processTime", Long.toString(System.currentTimeMillis() - start));
        return formatbyte(record);
    }

    @ResponseBody
    @RequestMapping(value = "/WriteInSession", method = RequestMethod.POST)
    public String readWriteInSession(HttpServletResponse response, String sfi, String data) {
        enumSFI sfiEnum = enumSFI.getSfiByCode(Byte.decode("0x" + sfi));
        long start = System.currentTimeMillis();
        // commandSetService.
        commandSetService.openSession(sfiEnum.getSfi());
        List<Record> record = efService.readAllRecords(sfiEnum.getSfi());
        try {
            commandSetService.updateRecord(data, Byte.valueOf((byte) 0x01), sfiEnum,
                    enumCmdWriteRecords.WRITE_RECORD_USING_SFI);
            commandSetService.closeSession();
        } catch (ReaderException e) {
            return null;
        }

        response.addHeader("processTime", Long.toString(System.currentTimeMillis() - start));
        return formatbyte(record);
    }

    private enumSFI getSFI(String fileSfi) {
        return enumSFI.getSfiByCode(Byte.decode("0x" + fileSfi));
    }

    @ResponseBody
    @RequestMapping(value = "/isCardPresent", method = RequestMethod.POST)
    public String isCardPresent() {
        return commandSetService.isCardPresent();
    }

}
