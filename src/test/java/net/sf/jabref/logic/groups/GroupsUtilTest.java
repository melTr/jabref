package net.sf.jabref.logic.groups;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;
import net.sf.jabref.importer.ParserResult;
import net.sf.jabref.importer.fileformat.BibtexParser;
import net.sf.jabref.model.database.BibDatabase;

public class GroupsUtilTest {

    @Before
    public void setup() {
        Globals.prefs = JabRefPreferences.getInstance(); // set preferences for this test
    }

    @After
    public void teardown() {
        Globals.prefs = null;
    }

    @Test
    public void test() throws IOException {
        try (FileReader fr = new FileReader("src/test/resources/testbib/testjabref.bib")) {

            ParserResult result = BibtexParser.parse(fr);

            BibDatabase db = result.getDatabase();

            List<String> fieldList = new ArrayList<>();
            fieldList.add("author");

            Set<String> authorSet = GroupsUtil.findAuthorLastNames(db, fieldList);
            assertTrue(authorSet.contains("Brewer"));
            assertEquals(15, authorSet.size());

            TreeSet<String> keywordSet = GroupsUtil.findDeliminatedWordsInField(db, "keywords", ";");
            assertTrue(keywordSet.contains("Brain"));
            assertEquals(60, keywordSet.size());

            TreeSet<String> wordSet = GroupsUtil.findAllWordsInField(db, "month", "");
            assertTrue(wordSet.contains("Feb"));
            assertTrue(wordSet.contains("Mar"));
            assertTrue(wordSet.contains("May"));
            assertTrue(wordSet.contains("Jul"));
            assertTrue(wordSet.contains("Dec"));
            assertEquals(5, wordSet.size());
        }
    }

}
