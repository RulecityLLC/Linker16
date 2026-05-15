package com.rulecity.parse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.rulecity.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration: parses every real .OBJ file in {@code Artifacts/TOOLS/OBJ/}
 * through the full DI-assembled parser graph. Asserts that no warnings are
 * emitted by the parser layer.
 */
public class AllObjsIT
{
    private static final Path OBJ_DIR = Paths.get("Artifacts/TOOLS/OBJ");
    private static final List<String> OBJ_FILES = List.of(
            "ASMLIB.OBJ",
            "CLIB.OBJ",
            "DL2PROD2.OBJ",
            "SERIAL.OBJ",
            "ENTERI.OBJ",
            "EEPROTS2.OBJ",
            "DL2DATA.OBJ",
            "STATS2.OBJ"
    );

    private ListAppender<ILoggingEvent> appender;
    private Logger parserLogger;

    @BeforeEach
    public void attachAppender()
    {
        parserLogger = (Logger) LoggerFactory.getLogger(OMFParserImpl.class);
        appender = new ListAppender<>();
        appender.start();
        parserLogger.addAppender(appender);
    }

    @AfterEach
    public void detachAppender()
    {
        parserLogger.detachAppender(appender);
        appender.stop();
    }

    @Test
    public void parseAllEightObjs_ZeroWarnings_ZeroExceptions() throws IOException
    {
        OMFParser parser = Main.buildParser();

        for (String name : OBJ_FILES)
        {
            Path p = OBJ_DIR.resolve(name);
            byte[] data = Files.readAllBytes(p);
            List<OMFItem> items = parser.parseBinary(data);
            assertNotNull(items, name + ": parser returned null");
        }

        List<String> warnings = appender.list.stream()
                .filter(e -> e.getLevel().isGreaterOrEqual(Level.WARN))
                .map(e -> e.getLoggerName() + " [" + e.getLevel() + "] " + e.getFormattedMessage())
                .collect(Collectors.toList());

        assertEquals(List.of(), warnings,
                "Parser emitted warnings while parsing the 8 OBJs:\n  " + String.join("\n  ", warnings));
    }
}
