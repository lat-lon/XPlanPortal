/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2013 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package de.latlon.xplanung.gwt.client.widget;

import java.util.List;
import java.util.ListIterator;

import org.geomajas.configuration.client.ClientVectorLayerInfo;
import org.geomajas.gwt.client.Geomajas;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.map.layer.RasterLayer;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.gwt.client.util.UrlBuilder;
import org.geomajas.gwt.client.util.WidgetLayout;
import org.geomajas.gwt.client.widget.Legend;
import org.geomajas.sld.FeatureTypeStyleInfo;
import org.geomajas.sld.RuleInfo;
import org.geomajas.sld.UserStyleInfo;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;

/**
 * <p>
 * Widget that shows the styles of the currently visible layers. For vector layers, there can be many styles. Note that
 * this widget will react automatically to the visibility status of the layers.
 * <p/>
 * Contains specific changes to the raster layer legend rendering for WMS Layers.
 * </p>
 * 
 * @author Alexander Erben
 * @author Dirk Stenger
 */
public class XPlanLegend extends Legend {

    public XPlanLegend( MapModel mapModel, boolean staticLegend ) {
        super( mapModel, staticLegend );
    }

    /**
     * A legend needs to be instantiated with the MapModel that contains (or will contain) the list of layers that this
     * legend should listen to.
     * 
     * @param mapModel
     *            map model
     */
    public XPlanLegend( MapModel mapModel ) {
        super( mapModel );
        setOverflow( Overflow.AUTO );
    }

    /**
     * Render the legend. This triggers a complete redraw.
     */
    public void render() {
        removeMembers( getMembers() );
        // traverse through the layer list in reverse to show the layers in the legend
        // in reversed order of renderings
        List<Layer<?>> layers = getMapModel().getLayers();
        ListIterator<Layer<?>> itr = layers.listIterator( layers.size() );
        while ( itr.hasPrevious() ) {
            Layer<?> layer = itr.previous();
            if ( isStaticLegend() || layer.isShowing() ) {
                // Go over every truly visible layer:
                if ( layer instanceof VectorLayer ) {
                    ClientVectorLayerInfo layerInfo = ( (VectorLayer) layer ).getLayerInfo();

                    // For vector layer; loop over the style definitions:
                    UserStyleInfo userStyle = layerInfo.getNamedStyleInfo().getUserStyle();
                    if ( userStyle != null && userStyle.getFeatureTypeStyleList() != null
                         && userStyle.getFeatureTypeStyleList().get( 0 ) != null ) {
                        FeatureTypeStyleInfo info = userStyle.getFeatureTypeStyleList().get( 0 );
                        List<RuleInfo> ruleList = info.getRuleList();
                        for ( RuleInfo rule : ruleList ) {
                            String title = determineStyleName( layerInfo, rule );
                            int index = ruleList.indexOf( rule );
                            addVector( (VectorLayer) layer, index, title );
                        }
                    }

                } else if ( layer instanceof RasterLayer ) {
                    addRaster( (RasterLayer) layer );
                }
            }
        }
    }

    private void addVector( VectorLayer layer, int ruleIndex, String title ) {
        HLayout layout = new HLayout( WidgetLayout.marginSmall );
        layout.setHeight( WidgetLayout.legendVectorRowHeight );
        UrlBuilder urlBuilder = new UrlBuilder( Geomajas.getDispatcherUrl() );
        urlBuilder.addPath( "legendgraphic" );
        urlBuilder.addPath( layer.getServerLayerId() );
        urlBuilder.addPath( layer.getLayerInfo().getNamedStyleInfo().getName() );
        urlBuilder.addPath( ruleIndex + ".png" );
        Img icon = new Img( urlBuilder.toString(), WidgetLayout.legendRasterIconWidth,
                            WidgetLayout.legendRasterIconHeight );
        icon.setLayoutAlign( Alignment.LEFT );
        layout.addMember( icon );
        Label label = new Label( title );
        label.setWrap( false );
        label.setLayoutAlign( Alignment.LEFT );
        layout.addMember( label );
        addMember( layout );
    }

    private void addRaster( RasterLayer layer ) {
        final HLayout hLayout = new HLayout( WidgetLayout.marginSmall );
        UrlBuilder urlBuilder = new UrlBuilder( Geomajas.getDispatcherUrl() );
        urlBuilder.addPath( "legendgraphic" );
        urlBuilder.addPath( layer.getServerLayerId() + ".png" );
        final Image icon = new Image( urlBuilder.toString() );
        icon.addLoadHandler( new LoadHandler() {

            @Override
            public void onLoad( LoadEvent event ) {
                hLayout.setHeight( icon.getHeight() );
                hLayout.setWidth( icon.getWidth() );
                markForRedraw();
            }
        } );
        hLayout.addMember( icon );
        addMember( hLayout );
    }

    private String determineStyleName( ClientVectorLayerInfo layerInfo, RuleInfo rule ) {
        // use title if present, name if not
        String title = ( rule.getTitle() != null ? rule.getTitle() : rule.getName() );
        // fall back to style name
        if ( title == null ) {
            title = layerInfo.getNamedStyleInfo().getName();
        }
        return title;
    }

}