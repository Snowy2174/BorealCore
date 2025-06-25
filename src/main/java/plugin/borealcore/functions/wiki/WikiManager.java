package plugin.borealcore.functions.wiki;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WikiManager extends Function {

    public static HashMap<String, Book> WIKI;
    public static List<String> CATEGORY;
    final String WIKI_URL = "https://github.com/Snowy2174/BendingMC-Wiki.git";

    private static void loadEntryFromDirectory(File file, String path) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                loadEntryFromDirectory(f, path + f.getName() + "/");
            }
        } else {
            if (file.getName().endsWith(".md")) {
                String id = path.replace("/", ":").replace(".md:", "");
                Book wiki = getBookContent(file);
                WIKI.put(id, wiki);
                if (id.contains(":")) {
                    String category = id.split(":")[0];
                    if (!CATEGORY.contains(category)) {
                        CATEGORY.add(category);
                    }
                }
            }
        }
    }

    public static void downloadRepo(String repoUrl, File wiki_file, Boolean force) throws IOException {
        try {
            if (force && wiki_file.exists()) {
                wiki_file.delete();
            }
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

    private static Book getBookContent(File file) {
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
            return Book.book(Component.text(mainTitle), Component.text("Author"), bookContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Book.book(Component.text("Title"), Component.text("Author"), bookContent);
    }

    private static void appendBookContent(List<Component> bookContent, String mainTitle, String subsectionTitle, StringBuilder subsectionContent) {
        String contentString = AdventureUtil.replaceMarkdown(subsectionContent.toString().trim());
        for (int i = 0; i < contentString.length(); i += 200) {
            String chunk = contentString.substring(i, Math.min(contentString.length(), i + 200));
            Component page = Component.text("= " + mainTitle).appendNewline().appendNewline()
                    .append(Component.text("= " + subsectionTitle)).appendNewline().appendNewline()
                    .append(AdventureUtil.getComponentFromMiniMessage(chunk));
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

    public static void openCategory(Player player, String category) {
        List<Component> categoryContent = new ArrayList<>();
        TextComponent.Builder page = Component.text().content("Category: " + category).appendNewline().appendNewline();
        for (String id : WIKI.keySet()) {
            if (id.startsWith(category + ":")) {
                page.append(WIKI.get(id).title().clickEvent(ClickEvent.runCommand("/wiki open " + id))).hoverEvent(HoverEvent.showText(Component.text("Click to open the entry " + id))).appendNewline();
            }
        }
        page.append(Component.text("Back").clickEvent(ClickEvent.runCommand("/wiki")));
        categoryContent.add(page.build());
        AdventureUtil.playerBook(player, Book.book(Component.text("Category: " + category), Component.text("Author"), categoryContent));
    }

    @Override
    public void load() {
        WIKI = new HashMap<>();
        CATEGORY = new ArrayList<>();
        loadWiki(WIKI_URL);
        AdventureUtil.consoleMessage("Loaded <green>" + (WIKI.size()) + " <gray>ingame wiki pages");
    }

    @Override
    public void unload() {
        if (WIKI != null) WIKI.clear();
    }

    private void loadWiki(String repoUrl) {
        File wiki_file = new File(BorealCore.plugin.getDataFolder() + File.separator + "wiki");
        if (!wiki_file.exists() && !wiki_file.mkdir()) {
            System.out.println("Failed to create wiki folder");
            return;
        }
        try {
            if (wiki_file.listFiles() == null || wiki_file.listFiles().length == 0) {
                downloadRepo(repoUrl, wiki_file, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        loadEntryFromDirectory(wiki_file, "");
    }


}


