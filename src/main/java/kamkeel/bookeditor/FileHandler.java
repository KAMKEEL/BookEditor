package kamkeel.bookeditor;

/**
 * Handles reading and writing books to disk in both the custom GHB format and
 * basic text files. Also manages file browsing logic used by the GUI.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import kamkeel.bookeditor.book.Book;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.book.Page;

public class FileHandler {
    private File defaultPath;

    private File bookSavePath;

    private File signaturePath;

    public File currentPath;

    private List<File> lastListing = new ArrayList<File>();

    private String lastCheckedPath = "";

    public FileHandler() {
        String path = (Minecraft.getMinecraft()).mcDataDir.getAbsolutePath();
        if (path.endsWith("."))
            path = path.substring(0, path.length() - 2);
        this.defaultPath = new File(path, "mods" + File.separator + "Books");
        if (!this.defaultPath.exists())
            this.defaultPath.mkdirs();
        this.bookSavePath = new File(this.defaultPath, "SavedBooks");
        if (!this.bookSavePath.exists())
            this.bookSavePath.mkdirs();
        this.signaturePath = new File(this.defaultPath, "Signatures");
        if (!this.signaturePath.exists())
            this.signaturePath.mkdirs();
    }

    public List<File> getValidRoots() {
        List<File> outList = new ArrayList<File>();
        for (File root : File.listRoots()) {
            if (root.listFiles() != null)
                outList.add(root);
        }
        return outList;
    }

    public void navigateUp() {
        for (File root : File.listRoots()) {
            if (this.currentPath.equals(root))
                return;
        }
        this.currentPath = this.currentPath.getParentFile();
    }

    public List<File> listFiles(File path) {
        if (!path.getAbsolutePath().equals(this.lastCheckedPath)) {
            this.lastCheckedPath = path.getAbsolutePath();
            this.lastListing.clear();
            File[] newList = path.listFiles();
            if (newList == null)
                newList = new File[0];
            List<File> files = new ArrayList<File>();
            for (File f : newList) {
                if (f.isDirectory()) {
                    this.lastListing.add(f);
                } else {
                    files.add(f);
                }
            }
            this.lastListing.addAll(files);
        }
        return this.lastListing;
    }

    public File getDefaultPath() {
        return this.defaultPath;
    }

    public File getSignaturePath() {
        return this.signaturePath;
    }

    public Book loadBook(File filePath) {
        if (filePath.getName().endsWith(".txt")) {
            System.out.println("Loading bookworm book...");
            return loadBookwormBook(filePath);
        }
        if (filePath.getName().endsWith(".ghb")) {
            System.out.println("Loading GHB book..." + filePath);
            return loadBookFromGHBFile(filePath);
        }
        return null;
    }

    private Book loadBookwormBook(File filePath) {
        List<String> f = readFile(filePath);
        if (f.size() >= 4 && StringUtils.isNumeric(f.get(0))) {
            Book loadedBook = new Book();
            loadedBook.clear();
            loadedBook.title = Book.truncateStringChars(f.get(1), "..", 16, false);
            loadedBook.author = Book.truncateStringChars(f.get(2), "..", 16, false);
            String bookText = f.get(f.size() - 1);
            String[] largePages = bookText.split("(\\s::){2,}");
            for (String largePage : largePages) {
                if (loadedBook.totalPages() > 0) {
                    Page currPage = loadedBook.pages.get(loadedBook.totalPages() - 1);
                    if (!currPage.asString().isEmpty())
                        currPage = Page.pad(currPage);
                }
                loadedBook.addText(loadedBook.totalPages(), 0, 0, largePage.replaceAll("\\s*::\\s*", "\n  "), true);
            }
            return loadedBook;
        }
        return null;
    }

    public List<String> readFile(File path) {
        BufferedReader br;
        List<String> out = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            Printer.gamePrint(Printer.RED + "File not found! " + path.getAbsolutePath());
            return null;
        }
        try {
            String line = br.readLine();
            while (line != null) {
                out.add(line.replace("Â\u00a7", "\u00a7"));
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Printer.gamePrint(Printer.RED + "Error reading file! " + path.getAbsolutePath());
            return null;
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return out;
    }

    public boolean writeFile(List<String> toWrite, File filePath) {
        boolean failedFlag = false;
        File path = filePath.getParentFile();
        if (!path.exists() &&
                !path.mkdirs())
            failedFlag = true;
        if (!failedFlag)
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                for (String s : toWrite)
                    out.println(s);
                out.close();
            } catch (IOException e) {
                failedFlag = true;
                System.out.println("Book Editor: Write failed!");
                System.out.println(e.getMessage());
                return false;
            }
        if (failedFlag) {
            Printer.gamePrint(Printer.RED + "WRITING TO DISK FAILED!");
            return false;
        }
        return true;
    }

    public static String cleanGHBString(String strIn) {
        strIn = strIn.replaceAll("(?s)//.*?((\\n)|(\\r\\n)|(\\Z))", "\n");
        strIn = strIn.replaceAll("(?s)((/\\*).*?((\\*/)|(\\Z)))|(((/\\*)|(\\A)).*?(\\*/))", "");
        strIn = strIn.replaceAll("[\\t\\r\\n ]+(##|>>>>)", "$1");
        strIn = strIn.replaceAll("[\\r\\n]", "");
        return strIn;
    }

    private Book loadBookFromGHBFile(File filePath) {
        Book loadedBook = new Book();
        loadedBook.clear();
        List<String> rawFile = readFile(filePath);
        if (rawFile == null || rawFile.isEmpty())
            return null;
        String concatFile = "";
        for (String line : rawFile) {
            if (line.toLowerCase().startsWith("title:") && loadedBook.title.isEmpty()) {
                if (line.length() >= 7) {
                    loadedBook.title = Book.truncateStringChars(cleanGHBString(line.substring(6)).trim(), "..", 16, false);
                    if (line.contains("/*"))
                        concatFile = concatFile + line.substring(line.indexOf("/*")) + "\n";
                }
                continue;
            }
            if (line.toLowerCase().startsWith("author:") && loadedBook.author.isEmpty()) {
                if (line.length() >= 8) {
                    loadedBook.author = Book.truncateStringChars(cleanGHBString(line.substring(7)).trim(), "..", 16, false);
                    if (line.contains("/*"))
                        concatFile = concatFile + line.substring(line.indexOf("/*")) + "\n";
                }
                continue;
            }
            concatFile = concatFile + line + "\n";
        }
        concatFile = cleanGHBString(concatFile);
        concatFile = concatFile.replaceAll("##", "\n");
        String[] pageBroken = concatFile.split(">>>>");
        for (String largePage : pageBroken) {
            if (loadedBook.totalPages() > 0) {
                Page currPage = loadedBook.pages.get(loadedBook.totalPages() - 1);
                if (!currPage.asString().isEmpty())
                    currPage = Page.pad(currPage);
            }
            loadedBook.addText(loadedBook.totalPages(), 0, 0, largePage, true);
        }
        return loadedBook;
    }

    public boolean saveBookToGHBFile(Book book) {
        Printer.gamePrint(Printer.GRAY + "Saving book to file...");
        List<String> toWrite = new ArrayList<String>();
        String utcTime = getUTCString();
        toWrite.add("//Book saved in GHB format at " + utcTime);
        if (!book.title.isEmpty())
            toWrite.add("title:" + book.title);
        if (!book.author.isEmpty())
            toWrite.add("author:" + book.author);
        toWrite.add("//=======================================\n");
        String pageMarker = "/////////// Page %d: ///////////";
        for (int i = 0; i < book.totalPages(); i++) {
            toWrite.add(String.format(pageMarker, new Object[]{Integer.valueOf(i + 1)}));
            for (Line line : ((Page) book.pages.get(i)).lines)
                toWrite.add(line.text.replaceAll("\n", "##"));
            if (i < book.totalPages() - 1)
                toWrite.add(">>>>\n");
        }
        String title = book.title.trim().replaceAll(" ", ".").replaceAll("[^a-zA-Z0-9\\.]", "");
        String author = book.author.trim().replaceAll(" ", ".").replaceAll("[^a-zA-Z0-9\\.]", "");
        if (title.isEmpty()) {
            title = "notitle";
            if (author.isEmpty())
                author = "noauthor";
        }
        File saveFile = new File(this.bookSavePath, title + "_" + author + "_" + utcTime + ".ghb");
        if (writeFile(toWrite, saveFile)) {
            Printer.gamePrint(Printer.GREEN + "Book saved to: " + saveFile);
            return true;
        }
        Printer.gamePrint(Printer.RED + "WRITING BOOK TO DISK FAILED!");
        return false;
    }

    public String getUTCString() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HHmm.S'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }
}
