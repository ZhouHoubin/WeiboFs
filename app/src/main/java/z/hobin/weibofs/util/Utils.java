package z.hobin.weibofs.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static String trimHtml(String html) {
        html = html.replaceAll("\n", "");
        html = html.replaceAll("<br />", "\r\n");
        StringBuilder htmlBuilder = new StringBuilder();
        Document document = Jsoup.parse(html);
        System.out.println();
        Element body = document.getElementsByTag("body").get(0);
        List<Node> nodes = body.childNodes();
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                htmlBuilder.append(textNode.text());
            } else if (node instanceof Element) {
                Element element = (Element) node;
                htmlBuilder.append(element.text());
                Elements imgElements = ((Element) node).getElementsByTag("img");
                for (int i = 0; i < imgElements.size(); i++) {
                    String alt = imgElements.get(i).attr("alt");
                    htmlBuilder.append(alt);
                }
            }
        }
        return htmlBuilder.toString();
    }
}
