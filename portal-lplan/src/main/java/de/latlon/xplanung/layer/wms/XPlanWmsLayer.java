package de.latlon.xplanung.layer.wms;

import org.geomajas.layer.LayerException;
import org.geomajas.layer.wms.WmsLayer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.vividsolutions.jts.geom.Coordinate;

public class XPlanWmsLayer extends WmsLayer {

    private static final String EMPTY_GFI_RESPONSE = "";

    public XPlanWmsLayer() {
    }

    @Override
    public String getFeatureInfoAsHtml( Coordinate coordinate, double layerScale, int pixelTolerance )
                            throws LayerException {
        String html = super.getFeatureInfoAsHtml( coordinate, layerScale, pixelTolerance );
        String tableContent = retrieveTableContentOf( html );
        if ( tableContent.length() > 0 ) {
            return html;
        } else {
            return EMPTY_GFI_RESPONSE;
        }
    }

    protected String retrieveTableContentOf( String html ) {
        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode rootNode = cleaner.clean( html );
        TagNode tableNode = (TagNode) rootNode.getElementListByName( "table", true ).get( 0 );
        String contentOfTable = tableNode.getText().toString();
        contentOfTable = contentOfTable.replaceAll( "\t", "" );
        contentOfTable = contentOfTable.replaceAll( "\n", "" );
        contentOfTable = contentOfTable.replaceAll( " ", "" );
        return contentOfTable;
    }

}