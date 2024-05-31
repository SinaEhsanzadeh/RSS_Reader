import org.jsoup.Jsoup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static ArrayList<String> urls = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        System.out.println("Welcome to RSS Reader!");
        indexList();

        boolean operationContinue = true;

        File file = new File("data.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        while((line = bufferedReader.readLine()) != null)
            urls.add(line);
        bufferedReader.close();

        int input;

        while (operationContinue){
            input = scanner.nextInt();
            scanner.nextLine();
            switch (input) {
                case 1: {
                    System.out.println("Show updates for:");
                    System.out.println("[0] All websites");
                    for (int i = 1; i <= urls.size(); i++)
                        System.out.println("[" + i + "] " + extractPageTitle(fetchPageSource(urls.get(i - 1))));
                    System.out.println("Enter -1 to return");
                    urlChoose(scanner.nextInt());
                    indexList();
                    break;
                }

                case 2: {
                    System.out.println("Please enter website URL to add:");
                    String newUrl = scanner.nextLine();
                    int i;
                    for(i = 0; i < urls.size(); i++){
                        if (newUrl.equals(urls.get(i))) {
                            System.out.println(urls.get(i) + " already exists.");
                            indexList();
                            break;
                        }
                    }
                    if (i == urls.size()){
                        urls.add(newUrl);
                        System.out.println(newUrl + " added successfully.");
                        indexList();
                        break;
                    }
                    break;
                }

                case 3: {
                    System.out.println("Please enter website URL to remove:");
                    String deleteUrl = scanner.nextLine();
                    int i;
                    for(i = 0; i < urls.size(); i++){
                        if (deleteUrl.equals(urls.get(i))){
                            urls.remove(i);
                            System.out.println(deleteUrl + " removed successfully.");
                            break;
                        }
                    }
                    if (i == urls.size() && i > 1)
                        System.out.println("Couldn't find " + deleteUrl);
                    indexList();
                    break;

                }

                case 4: {
                    operationContinue = false;

                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                    for (String url : urls) {
                        bufferedWriter.write(url + "\n");
                    }
                    bufferedWriter.close();
                }
            }
        }
    }

    public static void indexList(){
        System.out.println("Type a valid number for your desired action:");
        System.out.println("[1] Show updates");
        System.out.println("[2] Add URL");
        System.out.println("[3] Remove URL");
        System.out.println("[4] Exit");
    }

    public static String fetchPageSource(String urlString) throws IOException, URISyntaxException {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());

    }

    public static String extractPageTitle(String html)
    {
        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e)
        {
            return "Error: no title tag found in page source!";
        }
    }

    public static void retrieveRssContent(String rssUrl) {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < 5; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    public static void urlChoose(int choice) throws Exception {
        scanner.nextLine();
        if (choice == 0) {
            for (String url : urls) retrieveRssContent(extractRssUrl(url));
        }
        else if (choice != -1){
            for (int i = 1; i <= urls.size(); i++)
                if (i == choice){
                    retrieveRssContent(extractRssUrl(urls.get(i - 1)));
                }
        }

    }

    public static String extractRssUrl (String url)throws IOException {
            org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
            return doc.select("[type='application/rss+xml']").attr("abs:href");
    }


    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);

        return stringBuilder.toString();
    }



}
