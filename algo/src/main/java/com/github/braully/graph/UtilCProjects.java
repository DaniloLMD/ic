package com.github.braully.graph;

import com.github.braully.graph.operation.IGraphOperation;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Braully Rocha
 */
public class UtilCProjects {

    public static final String DEFAULT_C_SUPPROJECTS = "c-projects";
    public static final String DEFAULT_FILE_NAME_DESCRIPTOR = "Descriptor";
    public static final String DEFAULT_FILE_NAME_LAST_COMPILATION = "last-compilation.log";
    private static final String OBS = "*";

    public static void main(String... args) {
        String comand = "";
        if (args != null && args.length > 0) {
            comand = args[0];
        }
        switch (comand) {
            default:
                makeAllProjects();
                System.out.println("Make all c-projects...Done");
                break;
        }
        listOperations();
    }

    public static void makeAllProjects() {
        File dir = new File(DEFAULT_C_SUPPROJECTS);
        File[] filesList = dir.listFiles();
        try {
            for (File file : filesList) {
                try {
                    if (file.isDirectory()) {
                        System.out.println(file.getAbsolutePath());
                        int ret = execMakeProject(file);
                        if (ret == 0) {
                            System.out.print("Compile: " + file.getAbsolutePath() + "... Ok");
                        } else {
                            System.err.print("Compile: " + file.getAbsolutePath() + "... Error");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int execMakeProject(File directory) {
        int ret = -1;
        if (directory != null && directory.isDirectory()) {
            Process process = null;
            FileWriter fileLog = null;
            try {
                fileLog = new FileWriter(directory.getAbsoluteFile() + File.separator + DEFAULT_FILE_NAME_LAST_COMPILATION);
                String commandToExecute = "make --directory=" + directory.getAbsolutePath();
                fileLog.write("Command: " + commandToExecute);

                process = Runtime.getRuntime().exec(commandToExecute);

                InputStreamReader input = new InputStreamReader(process.getInputStream());
                BufferedReader reader = new BufferedReader(input);

                String line = "";
                while ((line = reader.readLine()) != null) {
                    fileLog.write(line);
                    System.out.println(line);
                }
                ret = process.waitFor();
            } catch (Exception ex) {
                try {
                    fileLog.write("Error: " + ex.getLocalizedMessage());
                } catch (Exception e) {
                }
            } finally {
                try {
                    fileLog.close();
                } catch (Exception e) {
                }
            }
        }
        return ret;
    }

    static Collection<IGraphOperation> listOperations() {
        List<IGraphOperation> operations = new ArrayList<>();
        try {
            String dirpath = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_C_SUPPROJECTS).getPath();
            File dir = new File(dirpath);
            File[] filesList = dir.listFiles();
            for (File file : filesList) {
                try {
                    File[] listFiles = file.listFiles();
                    for (File content : listFiles) {
                        if (content.isFile() && content.getName().equalsIgnoreCase(DEFAULT_FILE_NAME_DESCRIPTOR)) {
                            File binaryDir = new File(file, "bin");
                            if (!binaryDir.exists()) {
                                binaryDir = new File(file, "dist");
                            }
                            if (!binaryDir.exists()) {
                                binaryDir = new File(file, "build");
                            }
                            if (!binaryDir.exists()) {
                                binaryDir = file;
                            }
                            Iterator<File> iterateFiles = FileUtils.iterateFiles(binaryDir, null, true);
                            File binaryExec = iterateFiles.next();
                            while (!binaryExec.canExecute() && iterateFiles.hasNext()) {
                                binaryExec = iterateFiles.next();
                            }
                            BufferedReader frConReader = new BufferedReader(new FileReader(content));
                            String type = frConReader.readLine();
                            while (type != null && type.startsWith("#")) {
                                type = frConReader.readLine();
                            }
                            String operation = frConReader.readLine();
                            while (operation != null && operation.startsWith("#")) {
                                operation = frConReader.readLine();
                            }
                            String format = frConReader.readLine();
                            while (format != null && format.startsWith("#")) {
                                format = frConReader.readLine();
                            }
                            if (binaryExec != null && !Strings.isNullOrEmpty(type) && !Strings.isNullOrEmpty(operation) && !Strings.isNullOrEmpty(format)) {
                                operation = OBS + operation;
                                CBInaryOperation op = new CBInaryOperation(binaryExec.getAbsolutePath(), type, operation, format);
                                operations.add(op);
                                System.out.println("Operção criada: " + op);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {

        }
        return operations;
    }
}
