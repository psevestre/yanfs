/*
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.xfilechooser;

import javax.swing.*;
import javax.swing.filechooser.*;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * XFileSystemView class allows the XFileChooser to provide
 * XFile object data to the FileSystemView of the JFileChooser.
 * This class overrides the FileSystemView provided by JFileChooser.
 * Whenever an XFileChooser constructor is called the FileSystemView
 * that is set would be the XFileSystemView.
 */
public abstract class XFileSystemView extends FileSystemView {
    static FileSystemView windowsXFileSystemView = null;
    static FileSystemView unixXFileSystemView = null;
    static FileSystemView genericXFileSystemView = null;

    /**
     * Depending on type of operating system  (e.g. unix, windows, or generic)
     * it would return the file system view.
     * @return FileSystemView the operating system file system view
     */
    public static FileSystemView getFileSystemView() {
    if(File.separatorChar == '\\') {
        if(windowsXFileSystemView == null) {
        windowsXFileSystemView = new WindowsXFileSystemView();
        }
        return windowsXFileSystemView;
    }

    if(File.separatorChar == '/') {
        if(unixXFileSystemView == null) {
        unixXFileSystemView = new UnixXFileSystemView();
        }
        return unixXFileSystemView;
    }

    if(genericXFileSystemView == null) {
        genericXFileSystemView = new GenericXFileSystemView();
    }

    return genericXFileSystemView;
    }

    /**
     *  Creates a File object constructed from File obj and filename
     *  @param dir file object of directory
     *  @param filename name of file in directory
     *  @return File object created
     */
    @Override
    public File createFileObject(File dir, String filename) {

        if (dir == null)
            return new BeanXFile(filename);

    return new BeanXFile(dir, filename);
    }

    /**
     * Creates a file object constructed from give pathname
     * @return File object constructed from the given path string.
     */
    @Override
    public File createFileObject(String path) {
        return new BeanXFile(path);
    }

    /**
     * Returns the list of files in a directory
     * @param dir directory
     * @param useFileHiding flag to indicate to either show files hidden or not.
     * @return File[] array of files in the directory
     */
    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        Vector files = new Vector();

        // add all files in dir
        String[] names = dir.list();

        BeanXFile f;

        int nameCount = names == null ? 0 : names.length;

        for (int i = 0; i < nameCount; i++) {
            f = (BeanXFile)createFileObject(dir, names[i]);

        // If the object exists then add it.
        if (f.exists()) {
        if (useFileHiding) {
            if (!isHiddenFile(f))
            files.addElement(f);
        } else
            files.addElement(f);
        }
        }

        BeanXFile[] fileArray = new BeanXFile[files.size()];
        files.copyInto(fileArray);

        return fileArray;
    }


    /*
     * Providing default implemenations for the remaining methods
     * because most OS file systems will likely be able to use this
     * code. If a given OS can't, override these methods in its
     * implementation.
     */

    /**
     * Returns the user's home directory
     * @return File object of user's home directory
     */
    @Override
    public File getHomeDirectory() {
    return createFileObject(System.getProperty("user.home"));
    }

    /**
     * Returns the parent directory of specified directory/file object
     * @param dir directory
     * @return parent directory
     */
    @Override
    public File getParentDirectory(File dir)
    {
    String dirname = null;

        if (dir != null) {
        dirname = dir.getAbsolutePath();
        BeanXFile f = (BeanXFile) createFileObject(dirname);

            String parentFilename = f.getParent();

            if (parentFilename != null)
                return (new BeanXFile(parentFilename));
    }
        return null;
    }

    /**
     * Returns true if the given file object is root.
     * @param f file object to check if root
     * @return boolean value if file object is root (true) or not (false)
     */
    @Override
    public boolean isRoot(File f) {
    /* if the parentPath is null, return true */
        String parentPath = f.getParent();

        if (parentPath == null)
            return true;

    /*
     * if the parent.toString is equal to f.toString, then
     * it is root.
     */
    BeanXFile parent = new BeanXFile(parentPath);
    return parent.equals(f);
    }

}

/**
 * FileSystemView that handles some specific unix-isms.
 */
class UnixXFileSystemView extends XFileSystemView {
    /* For I18N */
    private static ResourceBundle rb =
    ResourceBundle.getBundle("com.sun.xfilechooser.EditorResource"/*NOI18N*/);

    /**
     * creates a new folder with a default folder name.
     */
    @Override
    public File createNewFolder(File containingDir) throws IOException {

    if (containingDir == null) {
        throw new IOException("Containing directory is null:");
    }

    BeanXFile newFolder = null;
    newFolder = (BeanXFile) createFileObject(containingDir, rb.getString("NewFolder"));
    int i = 1;

    while (newFolder.exists() && (i < 100)) {
        newFolder = (BeanXFile) createFileObject(containingDir, rb.getString("NewFolder") + "." + i);
        i++;
    }

    if(newFolder.exists())
        throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());

    newFolder.mkdirs();

    return newFolder;
    }

    /**
     * Returns whether a file is hidden or not. On Unix,
     * all files that begin with "." are hidden.
     */
    @Override
    public boolean isHiddenFile(File f) {
    if (f != null) {
        String filename = f.getName();

        if (filename.charAt(0) == '.')
        return true;
    }
    return false;
    }

    /**
     * Returns the root partitian on this system. On Unix, this is just "/".
     */
    @Override
    public File[] getRoots() {
    File[] roots = new BeanXFile[1];
    roots[0] = new BeanXFile("/");
    if (roots[0].exists() && roots[0].isDirectory()) {
        return roots;
    }
    return null;
    }

}


/**
 * FileSystemView that handles some specific windows concepts.
 */
class WindowsXFileSystemView extends XFileSystemView {
    /* For I18N */
    private static ResourceBundle rb =
    ResourceBundle.getBundle("com.sun.xfilechooser.EditorResource"/*NOI18N*/);

    /**
     * creates a new folder with a default folder name.
     */
    @Override
    public File createNewFolder(File containingDir) throws IOException {
    if (containingDir == null) {
        throw new IOException("Containing directory is null:");
    }
    BeanXFile newFolder = null;

    // Using NT's default folder name
    newFolder = (BeanXFile) createFileObject(containingDir, rb.getString("New Folder"));
    int i = 2;
    while (newFolder.exists() && (i < 100)) {
        newFolder = (BeanXFile) createFileObject(containingDir, rb.getString("New Folder") + "(" + i + ")");
        i++;
    }

    if (newFolder.exists())
        throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());

    newFolder.mkdirs();

    return newFolder;
    }

    /**
     * Returns whether a file is hidden or not. On Windows
     * there is currently no way to get this information from
     * io.File, therefore always return false.
     */
    @Override
    public boolean isHiddenFile(File f) {
    return false;
    }

    /**
     * Returns all root partitians on this system. On Windows, this
     * will be the A: through Z: drives.
     */
    @Override
    public File[] getRoots() {
    Vector rootsVector = new Vector();

    /* Create the A: drive whether it is mounted or not */
    XWindowsFloppy floppy = new XWindowsFloppy();
    rootsVector.addElement(floppy);

    /*
     * Run through all possible mount points and check
     * for their existance.
     */
    for (char c = 'C'; c <= 'Z'; c++) {
        char device[] = {c, ':', '\\'};
        String deviceName = new String(device);
        BeanXFile deviceFile = new BeanXFile(deviceName);
        if (deviceFile != null && deviceFile.exists()) {
        rootsVector.addElement(deviceFile);
        }
    }

    BeanXFile[] roots = new BeanXFile[rootsVector.size()];
    rootsVector.copyInto((Object[]) roots);
    return roots;
    }

    /**
     * Fake the floppy drive. There is no way to know whether
     * it is mounted or not, and doing a file.isDirectory or
     * file.exists() causes Windows to pop up the "Insert Floppy"
     * dialog. We therefore assume that A: is the floppy drive,
     * and force it to always return true for isDirectory()
     */
    class XWindowsFloppy extends BeanXFile {
    public XWindowsFloppy() {
        super("A" + ":" + "\\");
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
    }

}


/**
 * Fallthrough FileSystemView in case we can't determine the OS.
 */
class GenericXFileSystemView extends XFileSystemView {
    /* For I18N */
    private static ResourceBundle rb =
    ResourceBundle.getBundle("com.sun.xfilechooser.EditorResource"/*NOI18N*/);

    /**
     * creates a new folder with a default folder name.
     */
    @Override
    public File createNewFolder(File containingDir) throws IOException {
    if (containingDir == null) {
        throw new IOException("Containing directory is null:");
    }
    BeanXFile newFolder = null;

    newFolder = (BeanXFile) createFileObject(containingDir, rb.getString("NewFolder"));

    if (newFolder.exists())
        throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());

    newFolder.mkdirs();

    return newFolder;
    }

    /**
     * Returns whether a file is hidden or not. Since we don't
     * know the OS type, always return false
     */
    @Override
    public boolean isHiddenFile(File f) {
    return false;
    }

    /**
     * Returns all root partitians on this system. Since we
     * don't know what OS type this is, return a null file
     * list.
     */
    @Override
    public File[] getRoots() {
    BeanXFile[] roots = new BeanXFile[0];
    return roots;
    }

}

