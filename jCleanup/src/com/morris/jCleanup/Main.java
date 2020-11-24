package com.morris.jCleanup;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        printIntroduction();
        Scanner input = new Scanner(System.in);
        final int DAYS_TO_DELETE = getDaysToDelete(input);
        final Path PATH_TO_DIRECTORY = getPathToDirectory(input);
        LocalDate todayDate = LocalDate.now();
        Map<String, LocalDate> deletableFiles = streamDeletableFiles(PATH_TO_DIRECTORY);
        Map<String, LocalDate> validDeletableFiles = validateDeletableFiles(deletableFiles, DAYS_TO_DELETE, todayDate);
        boolean continueProcess = confirmFileDeletion(input, validDeletableFiles);
        if ( continueProcess ) {
            System.out.println("deleting files!");
            // delete files
        } else {
            System.exit(1);
        }
        input.close();
    }

    /**
     * This method utilizes the {@link DirectoryStream} to stream all the files in the user's
     * Desktop Directory. Returns a {@link Map} that contains a list of key: deletable file
     * and value: formatted date of deletable file. File is of type {@link String} and the
     * file's date is of type {@link LocalDate}.
     *
     * @param pathToDirectory : Path to user's Desktop Directory (/path/to/desktop).
     * @return {@link Map}
     * @author Wali Morris<walimmorris@gmail.com>
     */
    public static Map<String, LocalDate> streamDeletableFiles(Path pathToDirectory) {
        Map<String, LocalDate> deletableFiles = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pathToDirectory)) {
            for (Path file : stream) {
                BasicFileAttributes fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
                if (!fileAttributes.isDirectory()) {
                    deletableFiles.put(file.toString(), formatCreationTimeDate(fileAttributes.creationTime())); // example time: 2017-06-01T12:48:40Z
                }
            }
        } catch (IOException | DirectoryIteratorException er) {
            System.out.println(er);
        }
        return deletableFiles;
    }

    /**
     * Creation time example (2017-06-01T12:48:40Z). This method formats a creation times date and ignores
     * the actual time it was created. In the above example the time of creation (12:48:40) should be ignored
     * for the purpose of reading dates, excluding the time.
     * @param creationTime : {@link FileTime} Creation Time meta-data of a file.
     * @return {@link LocalDate}
     * @author Wali Morris<walimmorris@gmail.com>
     */
    public static LocalDate formatCreationTimeDate(FileTime creationTime) {
        StringBuilder formattedCreationTimeDate = new StringBuilder();
        int start = 0;
        while ( creationTime.toString().charAt(start) != 'T') {
            formattedCreationTimeDate.append(creationTime.toString().charAt(start));
            start = start + 1;
        }
        int year = Integer.parseInt(formattedCreationTimeDate.substring(0, 4));
        int month = Integer.parseInt(formattedCreationTimeDate.substring(5, 7));
        int day = Integer.parseInt(formattedCreationTimeDate.substring(8, 10));
        return LocalDate.of(year, month, day);
    }

    /**
     * This method adjusts today's date to the date which all files that falls on or before the adjusted
     * date should be deleted. Returns a {@link Map} containing the File and {@link LocalDate} of the
     * file that falls before or on the adjusted date.
     * @param deletableFiles : {@link Map} structure containing all files and their {@link LocalDate}
     * @param daysToDelete : Days to adjust today's date, the adjusted date determines which files are
     *                       validated for deletion.
     * @param todayDate : Today's date.
     * @return : {@link Map} containing path to file and {@link LocalDate}.
     * @author Wali Morris<walimmorris@gmail.com>
     */
    public static Map<String, LocalDate> validateDeletableFiles(Map<String, LocalDate> deletableFiles,
                                                                int daysToDelete, LocalDate todayDate) {
        Map<String, LocalDate> validDeletableFiles = new HashMap<>();
        LocalDate adjustedDate = todayDate.minus(Period.ofDays(daysToDelete));
        System.out.println("\tFiles on or after date ' " + adjustedDate.toString() + " ' will be deleted.");
        for ( String file : deletableFiles.keySet() ) {
            if (deletableFiles.get(file).compareTo(adjustedDate) <= 0) {
                validDeletableFiles.put(file, deletableFiles.get(file));
            }
        }
        return validDeletableFiles;
    }

    /**
     * Receives the number of days user would like to go adjust today's date. If user
     * submits '5' and today's date is 2020/11/25, then the date will be adjusted to
     * 2020/11/20.
     * @param input : {@link Scanner}
     * @return : int
     * @author Wali Morris<walimmorris@gmail.com>
     */
    public static int getDaysToDelete(Scanner input) {
        System.out.print("\tHow far should we go back, in days: ");
        return input.nextInt();
    }

    /**
     * Returns the user's wish to continue a process in the program.
     * @param input : {@link Scanner} reading user input.
     * @return : boolean
     * @author Wali Morris<walimmorris@gmail.com>
     */
    public static boolean continueProcess(Scanner input) {
        System.out.print("\tContinue? (yes / no) - q[quit]: ");
        boolean flag = false;
        String continueProcess = input.next();
        while (!flag) {
            switch (continueProcess) {
                case "Yes", "yes", "Y", "y", "No", "no", "N", "n" -> flag = true;
            }
            if (!flag) {
                if ( continueProcess.equals("q") || continueProcess.equals("quit")) {
                    System.out.println("\tFiles redeemed. Goodbye!");
                    System.exit(1);
                }
                System.out.print("\tPlease choose an option. (yes / no) - q[quit]: ");
                continueProcess = input.next();
            }
        }
        return continueProcess.equals("Yes") || continueProcess.equals("yes");
    }

    /**
     * Receives the path to the directory where files will be collected for deletion.
     * The file should be in a Unix-like format. Ex: '/path/to/directory'.
     * @param input : {@link Scanner}
     * @return {@link Path}
     * @author Wali Morris<walimmorris@gmail.com>
     */
    public static Path getPathToDirectory(Scanner input) {
        System.out.print("\tOkay, Where's your Desktop located (/path/to/desktop): ");
        String path = input.next();
        return Paths.get(path);
    }

    /**
     * reports a confirmed list of deletable files to the user and sends a warning.
     * @param input : {@link Scanner} object for user input.
     * @param validDeletableFiles : {@link Map} containing valid delatable files within {@link LocalDate}
     * @return boolean
     * @author Wali Morris<walimorris@gmail.com>
     */
    public static boolean confirmFileDeletion(Scanner input, Map<String, LocalDate> validDeletableFiles) {
        System.out.println("\n\tValid deletable files : ");
        for (String file : validDeletableFiles.keySet()) {
            System.out.print("\tFile: " + file);
            System.out.println("\tDate: " + validDeletableFiles.get(file));
        }
        System.out.println("\n\tWARNING ALL REPORTED FILES WILL BE DELETED!");
        return continueProcess(input);
    }

    /**
     * A quick introduction to jCleanup
     * @author Wali Morris<walimmorris@gmail.com>
     */
    public static void printIntroduction() {
        final String introduction =

                "\n\tWelcome to jCleanup, a friendly Java / Linux-y way to quickly clean up\n" +
                        "\tyour Directories of old and un-used files. Files are cleaned up based on your\n" +
                        "\trequirements. If you would like files older than 5 days from today's date\n" +
                        "\tto disappear just choose 5. Easy enough right? If you find this automation\n" +
                        "\tprogram useful, give it a like and tell your friends.\n" +
                        "\n\tThanks, \n" +
                        "\t\tWali.\n";

        System.out.println(introduction);
    }
}