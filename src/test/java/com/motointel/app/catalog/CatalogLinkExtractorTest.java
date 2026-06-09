package com.motointel.app.catalog;

import com.motointel.app.catalog.CatalogLinkExtractor.DiscoveredModel;
import com.motointel.app.catalog.CatalogLinkExtractor.ManufacturerLink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogLinkExtractorTest {

    private static final String HOME_BASE = "https://www.motorcyclespecs.co.za/";
    private static final String MANUF_BASE = "https://www.motorcyclespecs.co.za/bikes/bmw.htm";

    private final CatalogLinkExtractor extractor = new CatalogLinkExtractor();

    private Document parse(String html, String baseUri) {
        return Jsoup.parse(html, baseUri);
    }

    @Test
    void manufacturerLinksKeepRealManufacturersAndDropNavAndCategories() {
        // Relative hrefs exactly as the live home page serves them (../../bikes/...).
        String html = """
                <html><body>
                <div class="subMenu">
                  <a href="../../index.htm">Home</a>
                  <a href="../../bikes/Classics.htm">Classics</a>
                  <a href="../../bikes/video_clips.htm">Video Clips</a>
                  <a href="../../bikes/Complete_Manufacturer_List.htm">Complete Manufacturer List</a>
                  <a href="../../bikes/AJP.htm">AJP</a>
                  <a href="../../bikes/bmw.htm">BMW</a>
                  <a href="../../bikes/Honda.html">Honda</a>
                </div>
                <a href="../../contact.htm">Contact</a>
                </body></html>
                """;
        List<ManufacturerLink> links = extractor.manufacturerLinks(parse(html, HOME_BASE));

        Map<String, String> byName = links.stream()
                .collect(Collectors.toMap(ManufacturerLink::name, ManufacturerLink::url));
        assertEquals(3, links.size(), "only AJP, BMW, Honda are real manufacturers");
        assertTrue(byName.containsKey("AJP"));
        assertTrue(byName.containsKey("BMW"));
        assertTrue(byName.containsKey("Honda"));
        // Relative ../../ is resolved to an absolute /bikes/ URL.
        assertTrue(byName.get("BMW").contains("/bikes/bmw.htm"), byName.get("BMW"));
    }

    @Test
    void modelLinksExtractNamesYearsAndDedupePreferringRealText() {
        String html = """
                <table>
                  <tr><td><a href="../model/bmw/bmw_r1250gs_19.html">R 1250 GS</a></td><td>2019 - 21</td></tr>
                  <tr><td><a href="../model/bmw/bmw_s1000rr.html"><img src="s1000rr.jpg"></a></td><td>2009</td></tr>
                  <tr><td><a href="../model/bmw/bmw_s1000rr.html">S 1000 RR</a></td><td>2009</td></tr>
                  <tr><td><a href="bmw2.html">Next</a></td><td></td></tr>
                </table>
                """;
        List<DiscoveredModel> models = extractor.modelLinks(parse(html, MANUF_BASE), "BMW");

        Map<String, DiscoveredModel> byName = models.stream()
                .collect(Collectors.toMap(DiscoveredModel::modelName, Function.identity()));
        assertEquals(2, models.size(), "the two distinct model URLs (Next is not a model link)");

        DiscoveredModel gs = byName.get("R 1250 GS");
        assertTrue(gs.url().contains("/model/bmw/bmw_r1250gs_19.html"), gs.url());
        assertEquals("BMW", gs.manufacturerName());
        assertEquals(2019, gs.startYear());
        assertEquals(2021, gs.endYear(), "two-digit end year is expanded against the start century");

        // The image link (no text) and the text link share a URL; the human name must win.
        assertTrue(byName.containsKey("S 1000 RR"), "real anchor text preferred over URL slug");
        assertEquals(2009, byName.get("S 1000 RR").startYear());
        assertNull(byName.get("S 1000 RR").endYear());
    }

    @Test
    void modelNameFallsBackToUrlSlugWhenAnchorHasNoText() {
        String html = """
                <table><tr><td>
                  <a href="../model/honda/honda-cbr600rr.html"><img src="x.jpg"></a>
                </td><td></td></tr></table>
                """;
        List<DiscoveredModel> models = extractor.modelLinks(parse(html, MANUF_BASE), "Honda");
        assertEquals(1, models.size());
        assertEquals("honda cbr600rr", models.get(0).modelName());
    }

    @Test
    void nextPageUrlResolvesRelativeToTheBikesDirectory() {
        String html = "<a href=\"bmw2.html\">Next</a>";
        Optional<String> next = extractor.nextPageUrl(parse(html, MANUF_BASE));
        assertTrue(next.isPresent());
        assertTrue(next.get().contains("/bikes/bmw2.html"), next.get());
    }

    @Test
    void nextPageUrlAbsentWhenNoNextLink() {
        String html = "<a href=\"bmw.htm\">Previous</a>";
        assertTrue(extractor.nextPageUrl(parse(html, MANUF_BASE)).isEmpty());
    }

    @Test
    void capacityDigitsAreNotMistakenForYears() {
        // "1000" (capacity) is a 4-digit number but not a plausible production year.
        String html = """
                <table><tr><td>
                  <a href="../model/bmw/bmw_s1000rr.html">S 1000 RR</a>
                </td><td>1000 cc</td></tr></table>
                """;
        List<DiscoveredModel> models = extractor.modelLinks(parse(html, MANUF_BASE), "BMW");
        assertEquals(1, models.size());
        assertNull(models.get(0).startYear(), "1000 is below the plausible-year floor");
    }
}
