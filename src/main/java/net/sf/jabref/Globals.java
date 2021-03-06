/*  Copyright (C) 2003-2015 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref;

import net.sf.jabref.collab.FileUpdateMonitor;
import net.sf.jabref.exporter.AutoSaveManager;
import net.sf.jabref.gui.GlobalFocusListener;
import net.sf.jabref.gui.JabRefFrame;
import net.sf.jabref.gui.keyboard.KeyBindingPreferences;
import net.sf.jabref.importer.ImportFormatReader;
import net.sf.jabref.logic.error.StreamEavesdropper;
import net.sf.jabref.logic.logging.CacheableHandler;
import net.sf.jabref.logic.remote.server.RemoteListenerServerLifecycle;
import net.sf.jabref.logic.util.BuildInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Globals {

    public static final String FILE_FIELD = "file";
    public static final String FOLDER_FIELD = "folder";

    private static final Log LOGGER = LogFactory.getLog(Globals.class);
    // JabRef version info
    public static final BuildInfo BUILD_INFO = new BuildInfo();
    // Signature written at the top of the .bib file.
    public static final String SIGNATURE = "This file was created with JabRef";
    public static final String encPrefix = "Encoding: ";
    // Newlines
    // will be overridden in initialization due to feature #857 @ JabRef.java
    public static String NEWLINE = System.lineSeparator();

    // Remote listener
    public static RemoteListenerServerLifecycle remoteListener = new RemoteListenerServerLifecycle();

    public static final ImportFormatReader importFormatReader = new ImportFormatReader();

    public static CacheableHandler handler;

    public static final String FILETYPE_PREFS_EXT = "_dir";
    public static final String SELECTOR_META_PREFIX = "selector_";
    public static final String PROTECTED_FLAG_META = "protectedFlag";
    public static final String NONE = "_non__";
    public static final String FORMATTER_PACKAGE = "net.sf.jabref.exporter.layout.format.";

    // In the main program, this field is initialized in JabRef.java
    // Each test case initializes this field if required
    public static JabRefPreferences prefs;

    private static KeyBindingPreferences keyPrefs;
    public static KeyBindingPreferences getKeyPrefs() {
        if(keyPrefs == null) {
            keyPrefs = new KeyBindingPreferences(prefs);
        }
        return keyPrefs;
    }

    public static final String SPECIAL_COMMAND_CHARS = "\"`^~'c=";

    // Background tasks
    public static GlobalFocusListener focusListener;
    public static FileUpdateMonitor fileUpdateMonitor;
    public static StreamEavesdropper streamEavesdropper;


    public static void startBackgroundTasks() {
        Globals.focusListener = new GlobalFocusListener();

        Globals.streamEavesdropper = StreamEavesdropper.eavesdropOnSystem();

        Globals.fileUpdateMonitor = new FileUpdateMonitor();
        JabRefExecutorService.INSTANCE.executeWithLowPriorityInOwnThread(Globals.fileUpdateMonitor, "FileUpdateMonitor");
    }


    // Autosave manager
    public static AutoSaveManager autoSaveManager;


    public static void startAutoSaveManager(JabRefFrame frame) {
        Globals.autoSaveManager = new AutoSaveManager(frame);
        Globals.autoSaveManager.startAutoSaveTimer();
    }

    // Stop the autosave manager if it has been started
    public static void stopAutoSaveManager() {
        if (Globals.autoSaveManager != null) {
            Globals.autoSaveManager.stopAutoSaveTimer();
            Globals.autoSaveManager.clearAutoSaves();
            Globals.autoSaveManager = null;
        }
    }
}
