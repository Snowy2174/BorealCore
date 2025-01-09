package plugin.customcooking.wiki;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;

import java.io.*;
import java.util.*;

public class WikiManager extends Function {

    public static HashMap<String, Book> WIKI;

    @Override
    public void load() {
        WIKI = new HashMap<>();
        loadWiki("https://github.com/Snowy2174/BendingMC-Wiki.git");
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + (WIKI.size()) + " <gray>ingame wiki pages");
        AdventureUtil.consoleMessage(String.valueOf(WIKI.keySet()));
    }

    @Override
    public void unload() {
        if (WIKI != null) WIKI.clear();
    }

    private void loadWiki(String repoUrl) {
        File wiki_file = new File(CustomCooking.plugin.getDataFolder() + File.separator + "wiki");
        if (!wiki_file.exists() && !wiki_file.mkdir()) {
            System.out.println("Failed to create wiki folder");
            return;
        }
        try {
            downloadRepo(repoUrl, wiki_file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        loadEntryFromDirectory(wiki_file, "");
        }

        private static void loadEntryFromDirectory(File file, String path) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    loadEntryFromDirectory(f, path + f.getName() + "/");
                }
            } else {
                if (file.getName().endsWith(".md")) {
                    String id = path.replace("/",":").replace(".md:", "");
                    Book wiki = Book.book(Component.text(id), Component.text("Author"), getBookContent(file));
                    WIKI.put(id, wiki);
                }
            }
        }

        private static void downloadRepo(String repoUrl, File wiki_file) throws IOException {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", repoUrl, wiki_file.getAbsolutePath());
                processBuilder.inheritIO();
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.out.println("Git clone failed with exit code: " + exitCode);
                    return;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

        private static Collection<Component> getBookContent(File file) {
            List<Component> bookContent = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                String mainTitle = null;
                String currentSubsectionTitle = null;
                StringBuilder currentSubsectionContent = new StringBuilder();

                while ((line = reader.readLine()) != null) {

                    if (line.startsWith("# ")) {
                        mainTitle = line.substring(1).trim();
                        continue;
                    }

                    if (line.startsWith("## ")) {
                        if (currentSubsectionTitle != null) {
                            appendBookContent(bookContent, mainTitle, currentSubsectionTitle, currentSubsectionContent);
                        }
                        currentSubsectionTitle = line.substring(2).trim();
                        currentSubsectionContent.setLength(0);
                    } else if (currentSubsectionTitle != null) {
                        currentSubsectionContent.append(line).append(System.lineSeparator());
                    }

                    if (currentSubsectionTitle != null) {
                        appendBookContent(bookContent, mainTitle, currentSubsectionTitle, currentSubsectionContent);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bookContent;
        }

        private static void appendBookContent(List<Component> bookContent, String mainTitle, String subsectionTitle, StringBuilder subsectionContent) {
            String contentString = AdventureUtil.replaceMarkdown(subsectionContent.toString().trim());
            for (int i = 0; i < contentString.length(); i += 200) {
                String chunk = contentString.substring(i, Math.min(contentString.length(), i + 200));
                Component page = Component.text("= " + mainTitle).appendNewline().appendNewline()
                        .append(Component.text("= " + subsectionTitle)).appendNewline().appendNewline()
                        .append(AdventureUtil.getComponentFromMiniMessage(chunk));
                System.out.println(chunk);
                bookContent.add(page);
            }
            subsectionContent.setLength(0);
        }

        public static void openBook(Player player, String id) {
            Book book = WIKI.get(id);
            if (book == null) {
                AdventureUtil.playerMessage(player, "This wiki page does not exist");
                return;
            }
            AdventureUtil.playerBook(player, book);
        }


}


