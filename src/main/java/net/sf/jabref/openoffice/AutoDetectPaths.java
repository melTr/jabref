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
package net.sf.jabref.openoffice;

import net.sf.jabref.Globals;
import net.sf.jabref.gui.worker.AbstractWorker;

import javax.swing.*;
import java.io.File;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import net.sf.jabref.logic.l10n.Localization;
import net.sf.jabref.logic.util.OS;

/**
 * Tools for automatically detecting jar and executable paths to OpenOffice and/or LibreOffice.
 */
public class AutoDetectPaths extends AbstractWorker {

    private boolean foundPaths;
    private boolean fileSearchCancelled;
    private JDialog prog;
    private final JDialog parent;


    public AutoDetectPaths(JDialog parent) {
        this.parent = parent;
    }

    public boolean runAutodetection() {
        try {
            if (AutoDetectPaths.checkAutoDetectedPaths()) {
                return true;
            }
            init();
            getWorker().run();
            update();
            return foundPaths;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        foundPaths = autoDetectPaths();
    }

    public boolean getResult() {
        return foundPaths;
    }

    public boolean cancelled() {
        return fileSearchCancelled;
    }

    @Override
    public void init() throws Throwable {
        prog = showProgressDialog(parent, Localization.lang("Autodetecting paths..."),
                Localization.lang("Please wait..."), true);
    }

    @Override
    public void update() {
        prog.dispose();
    }

    private boolean autoDetectPaths() {

        if (OS.WINDOWS) {
            List<File> progFiles = AutoDetectPaths.findProgramFilesDir();
            File sOffice = null;
            if (fileSearchCancelled) {
                return false;
            }
            List<File> sofficeFiles = new ArrayList<>();
            for (File dir : progFiles) {
                sOffice = findFileDir(dir, "soffice.exe");
                if (sOffice != null) {
                    sofficeFiles.add(sOffice);
                }
            }
            if (sOffice == null) {
                JOptionPane.showMessageDialog(parent,
                        Localization
                                .lang("Unable to autodetect OpenOffice/LibreOffice installation. Please choose the installation directory manually."),
                        Localization.lang("Could not find OpenOffice/LibreOffice installation"),
                        JOptionPane.INFORMATION_MESSAGE);
                JFileChooser jfc = new JFileChooser(new File("C:\\"));
                jfc.setDialogType(JFileChooser.OPEN_DIALOG);
                jfc.setFileFilter(new javax.swing.filechooser.FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return Localization.lang("Directories");
                    }
                });
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.showOpenDialog(parent);
                if (jfc.getSelectedFile() != null) {
                    sOffice = jfc.getSelectedFile();
                }
            }
            if (sOffice == null) {
                return false;
            }

            if (sofficeFiles.size() > 1) {
                // More than one file found
                DefaultListModel<File> mod = new DefaultListModel<>();
                for (File tmpfile : sofficeFiles) {
                    mod.addElement(tmpfile);
                }
                JList<File> fileList = new JList<>(mod);
                fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                fileList.setSelectedIndex(0);
                FormBuilder b = FormBuilder.create()
                        .layout(new FormLayout("left:pref", "pref, 2dlu, pref, 4dlu, pref"));
                b.add(Localization.lang("Found more than one OpenOffice/LibreOffice executable.")).xy(1, 1);
                b.add(Localization.lang("Please choose which one to connect to:")).xy(1, 3);
                b.add(fileList).xy(1, 5);
                int answer = JOptionPane.showConfirmDialog(null, b.getPanel(),
                        Localization.lang("Choose OpenOffice/LibreOffice executable"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.CANCEL_OPTION) {
                    return false;
                } else {
                    sOffice = fileList.getSelectedValue();
                }

            } else {
                sOffice = sofficeFiles.get(0);
            }
            Globals.prefs.put("ooExecutablePath", new File(sOffice, "soffice.exe").getPath());
            File unoil = findFileDir(sOffice.getParentFile(), "unoil.jar");
            if (fileSearchCancelled) {
                return false;
            }
            File jurt = findFileDir(sOffice.getParentFile(), "jurt.jar");
            if (fileSearchCancelled) {
                return false;
            }
            if ((unoil != null) && (jurt != null)) {
                Globals.prefs.put("ooUnoilPath", unoil.getPath());
                Globals.prefs.put("ooJurtPath", jurt.getPath());
                return true;
            } else {
                return false;
            }

        }
        else if (OS.OS_X) {
            File rootDir = new File("/Applications");
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && "OpenOffice.org.app".equals(file.getName())) {
                        rootDir = file;
                        //System.out.println("Setting starting dir to: "+file.getPath());
                        break;
                    }
                }
            }
            //System.out.println("Searching for soffice.bin");
            File sOffice = findFileDir(rootDir, "soffice.bin");
            //System.out.println("Found: "+(sOffice != null ? sOffice.getPath() : "-"));
            if (fileSearchCancelled) {
                return false;
            }
            if (sOffice != null) {
                Globals.prefs.put("ooExecutablePath", new File(sOffice, "soffice.bin").getPath());
                //System.out.println("Searching for unoil.jar");
                File unoil = findFileDir(rootDir, "unoil.jar");
                //System.out.println("Found: "+(unoil != null ? unoil.getPath(): "-"));
                if (fileSearchCancelled) {
                    return false;
                }
                //System.out.println("Searching for jurt.jar");
                File jurt = findFileDir(rootDir, "jurt.jar");
                //System.out.println("Found: "+(jurt != null ? jurt.getPath(): "-"));
                if (fileSearchCancelled) {
                    return false;
                }
                if ((unoil != null) && (jurt != null)) {
                    Globals.prefs.put("ooUnoilPath", unoil.getPath());
                    Globals.prefs.put("ooJurtPath", jurt.getPath());
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        else {
            // Linux:
            String usrRoot = "/usr/lib";
            File inUsr = findFileDir(new File("/usr/lib"), "soffice");
            if (fileSearchCancelled) {
                return false;
            }
            if (inUsr == null) {
                inUsr = findFileDir(new File("/usr/lib64"), "soffice");
                if (inUsr != null) {
                    usrRoot = "/usr/lib64";
                }
            }

            if (fileSearchCancelled) {
                return false;
            }
            File inOpt = findFileDir(new File("/opt"), "soffice");
            if (fileSearchCancelled) {
                return false;
            }
            if ((inUsr != null) && (inOpt == null)) {
                return setupPreferencesForOO(usrRoot, inUsr);
            }
            else if ((inOpt != null) && (inUsr == null)) {
                Globals.prefs.put("ooExecutablePath", new File(inOpt, "soffice.bin").getPath());
                File unoil = findFileDir(new File("/opt"), "unoil.jar");
                File jurt = findFileDir(new File("/opt"), "jurt.jar");
                if ((unoil != null) && (jurt != null)) {
                    Globals.prefs.put("ooUnoilPath", unoil.getPath());
                    Globals.prefs.put("ooJurtPath", jurt.getPath());
                    return true;
                } else {
                    return false;
                }
            }
            else if (inOpt != null) { // Found both
                JRadioButton optRB = new JRadioButton(inOpt.getPath(), true);
                JRadioButton usrRB = new JRadioButton(inUsr.getPath(), false);
                ButtonGroup bg = new ButtonGroup();
                bg.add(optRB);
                bg.add(usrRB);
                FormBuilder b = FormBuilder.create()
                        .layout(new FormLayout("left:pref", "pref, 2dlu, pref, 2dlu, pref "));
                b.add(Localization
                        .lang("Found more than one OpenOffice/LibreOffice executable. Please choose which one to connect to:"))
                        .xy(1, 1);
                b.add(optRB).xy(1, 3);
                b.add(usrRB).xy(1, 5);
                int answer = JOptionPane.showConfirmDialog(null, b.getPanel(),
                        Localization.lang("Choose OpenOffice/LibreOffice executable"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.CANCEL_OPTION) {
                    return false;
                } else {
                    if (optRB.isSelected()) {
                        return setupPreferencesForOO("/opt", inOpt);
                    }
                    else {
                        return setupPreferencesForOO(usrRoot, inUsr);
                    }

                }
            } else {
                return false;
            }
        }

    }

    private boolean setupPreferencesForOO(String usrRoot, File inUsr) {
        Globals.prefs.put("ooExecutablePath", new File(inUsr, "soffice.bin").getPath());
        File unoil = findFileDir(new File(usrRoot), "unoil.jar");
        if (fileSearchCancelled) {
            return false;
        }
        File jurt = findFileDir(new File(usrRoot), "jurt.jar");
        if (fileSearchCancelled) {
            return false;
        }
        if ((unoil != null) && (jurt != null)) {
            Globals.prefs.put("ooUnoilPath", unoil.getPath());
            Globals.prefs.put("ooJurtPath", jurt.getPath());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Search for Program files directory.
     * @return the File pointing to the Program files directory, or null if not found.
     *   Since we are not including a library for Windows integration, this method can't
     *   find the Program files dir in localized Windows installations.
     */
    private static java.util.List<File> findProgramFilesDir() {
        List<String> sourceList = new ArrayList<>();
        List<File> dirList = new ArrayList<>();

         // 64-bits first
        String progFiles = System.getenv("ProgramFiles");
        if (progFiles != null) {
            sourceList.add(progFiles);
        }

        // Then 32-bits
        progFiles = System.getenv("ProgramFiles(x86)");
        if (progFiles != null) {
            sourceList.add(progFiles);
        }

        for (String rootPath : sourceList) {
            File root = new File(rootPath);
            File[] dirs = root.listFiles(File::isDirectory);
            if (dirs != null) {
                Collections.addAll(dirList, dirs);
            }
        }
        return dirList;
    }

    private static boolean checkAutoDetectedPaths() {

        if (Globals.prefs.hasKey("ooUnoilPath") && Globals.prefs.hasKey("ooJurtPath")
                && Globals.prefs.hasKey("ooExecutablePath")) {
            return new File(Globals.prefs.get("ooUnoilPath"), "unoil.jar").exists()
                    && new File(Globals.prefs.get("ooJurtPath"), "jurt.jar").exists()
                    && new File(Globals.prefs.get("ooExecutablePath")).exists();
        } else {
            return false;
        }
    }

    /**
     * Search for a file, starting at the given directory.
     * @param startDir The starting point.
     * @param filename The name of the file to search for.
     * @return The directory where the file was first found, or null if not found.
     */
    private File findFileDir(File startDir, String filename) {
        if (fileSearchCancelled) {
            return null;
        }
        File[] files = startDir.listFiles();
        if (files == null) {
            return null;
        }
        File result = null;
        for (File file : files) {
            if (fileSearchCancelled) {
                return null;
            }
            if (file.isDirectory()) {
                result = findFileDir(file, filename);
                if (result != null) {
                    break;
                }
            } else if (file.getName().equals(filename)) {
                result = startDir;
                break;
            }
        }
        return result;
    }

    public JDialog showProgressDialog(JDialog progressParent, String title, String message, boolean includeCancelButton) {
        fileSearchCancelled = false;
        final JDialog progressDialog;
        JProgressBar bar = new JProgressBar(SwingConstants.HORIZONTAL);
        JButton cancel = new JButton(Localization.lang("Cancel"));
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                fileSearchCancelled = true;
                ((JButton) event.getSource()).setEnabled(false);
            }
        });
        progressDialog = new JDialog(progressParent, title, false);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bar.setIndeterminate(true);
        if (includeCancelButton) {
            progressDialog.add(cancel, BorderLayout.SOUTH);
        }
        progressDialog.add(new JLabel(message), BorderLayout.NORTH);
        progressDialog.add(bar, BorderLayout.CENTER);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(null);//parent);
        //SwingUtilities.invokeLater(new Runnable() {
        //    public void run() {
        progressDialog.setVisible(true);
        //    }
        //});
        return progressDialog;
    }
}
