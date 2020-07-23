package org.eclipse.keyple.calypso.command.sam.exception;

import com.google.gson.Gson;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoAccessForbiddenException;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSamAnomalyException;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SamCommandExceptionTest {

    Gson parser;

    @Before
    public void setUp(){
        parser = KeypleJsonParser.getParser();
    }

    @Test
    public void serializeException(){
       CalypsoSamAccessForbiddenException source = new CalypsoSamAccessForbiddenException("message",
                CalypsoSamCommand.CARD_CIPHER_PIN, 2);
       String json = parser.toJson(source, KeypleSeCommandException.class);
        KeypleSeCommandException target = (KeypleSeCommandException) parser.fromJson(json, KeypleException.class);
        assertThat(target).isEqualToComparingFieldByFieldRecursively(source);
        assertThat(target.getCommand()).isEqualToComparingFieldByField(source.getCommand());
    }

}
