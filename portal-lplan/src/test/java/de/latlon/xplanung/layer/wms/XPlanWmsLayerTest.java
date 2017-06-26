package de.latlon.xplanung.layer.wms;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XPlanWmsLayerTest {

    private static final String HTML_EXAMPLE_WITH_FILLED_TABLE = "<html><table>test</table></html>";

    private static final String HTML_EXAMPLE_WITH_EMPTY_TABLE = "<html><table>  \n \t</table></html>";

    @Test
    public void testHtmlParsingWithTable() {
        XPlanWmsLayer layer = new XPlanWmsLayer();
        String tableContent = layer.retrieveTableContentOf(HTML_EXAMPLE_WITH_FILLED_TABLE);
        assertEquals(tableContent, "test");
    }

    @Test
    public void testHtmlParsingWithEmptyTable() {
        XPlanWmsLayer layer = new XPlanWmsLayer();
        String tableContent = layer.retrieveTableContentOf(HTML_EXAMPLE_WITH_EMPTY_TABLE);
        assertEquals(0, tableContent.length());
    }
}
