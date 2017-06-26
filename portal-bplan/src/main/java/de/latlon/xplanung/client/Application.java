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
package de.latlon.xplanung.client;

import java.util.List;

import org.geomajas.configuration.client.ClientToolInfo;
import org.geomajas.gwt.client.gfx.style.ShapeStyle;
import org.geomajas.gwt.client.util.WidgetLayout;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.gwt.client.widget.OverviewMap;
import org.geomajas.widget.featureinfo.client.util.FitSetting;
import org.geomajas.widget.featureinfo.client.widget.DockableWindow;
import org.geomajas.widget.layer.client.widget.CombinedLayertree;
import org.geomajas.widget.layer.client.widget.DragAndDropLayerList;
import org.geomajas.widget.layer.client.widget.DragAndDropLayerListWindow;
import org.geomajas.widget.searchandfilter.client.widget.attributesearch.AttributeSearchCreator;
import org.geomajas.widget.searchandfilter.client.widget.geometricsearch.FreeDrawingSearch;
import org.geomajas.widget.searchandfilter.client.widget.geometricsearch.GeometricSearchCreator;
import org.geomajas.widget.searchandfilter.client.widget.geometricsearch.GeometricSearchPanel;
import org.geomajas.widget.searchandfilter.client.widget.geometricsearch.GeometricSearchPanelCreator;
import org.geomajas.widget.searchandfilter.client.widget.multifeaturelistgrid.MultiFeatureListGrid;
import org.geomajas.widget.searchandfilter.client.widget.search.CombinedSearchCreator;
import org.geomajas.widget.searchandfilter.client.widget.search.SearchEvent;
import org.geomajas.widget.searchandfilter.client.widget.search.SearchHandler;
import org.geomajas.widget.searchandfilter.client.widget.search.SearchWidgetRegistry;
import org.geomajas.widget.searchandfilter.client.widget.searchfavourites.SearchFavouritesListCreator;
import org.geomajas.widget.utility.common.client.ribbon.RibbonColumn;
import org.geomajas.widget.utility.gwt.client.action.map.RefreshLayersAction;
import org.geomajas.widget.utility.gwt.client.ribbon.RibbonBarLayout;
import org.geomajas.widget.utility.gwt.client.ribbon.RibbonButton;
import org.geomajas.widget.utility.gwt.client.ribbon.RibbonColumnRegistry;
import org.geomajas.widget.utility.gwt.client.ribbon.RibbonColumnRegistry.RibbonColumnCreator;
import org.geomajas.widget.utility.gwt.client.ribbon.map.DisplayCoordinatesRibbonColumn;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;

import de.latlon.xplanung.client.i18n.ApplicationMessages;
import de.latlon.xplanung.gwt.client.widget.XPlanLegend;

/**
 * Entry point and main class for GWT application. This class defines the layout and functionality of this application.
 * 
 * @author geomajas-gwt-archetype
 */
public class Application implements EntryPoint {

    private static final String APPLICATION_NAME = "app";

    private static final boolean NOT_RESIZABLE = false;

    private static final boolean IS_RESIZABLE = true;

    private final ApplicationMessages messages = GWT.create( ApplicationMessages.class );

    private Dictionary dict = Dictionary.getDictionary( "ProjectMessages" );

    private MultiFeatureListGrid searchResultGrid;

    static {
        WidgetLayout.legendVectorRowHeight = 10;
        WidgetLayout.legendRasterRowHeight = 10;
        FitSetting.featureinfoIncludeRasterLayer = true;
    }

    @Override
    public void onModuleLoad() {

        MapWidget map = new MapWidget( "mapMain", APPLICATION_NAME );
        // ---------------------------------------------------------------------
        // Create the left side (layer-tree)
        // ---------------------------------------------------------------------
        SectionStackSection layerTreeSection = createLayerTreeSection( map );
        IButton dragAndDropLayertreeButton = createDragAndDropLayerListButton( map );

        SectionStack leftSectionStack = createSectionStack( IS_RESIZABLE );
        leftSectionStack.addSection( layerTreeSection );
        leftSectionStack.addMember( dragAndDropLayertreeButton );

        // ---------------------------------------------------------------------
        // Create the center (map and ribbon toolbar)
        // ---------------------------------------------------------------------
        RibbonBarLayout centerRibbon = createCenterRibbon( map );
        VLayout centerLayout = createCenterLayout( map, centerRibbon, IS_RESIZABLE );

        // ---------------------------------------------------------------------
        // Create the right-side (overview map, scale, coordinates):
        // ---------------------------------------------------------------------
        SectionStackSection overviewMapSection = createOverviewMapSection( map );
        SectionStackSection scaleSection = createScaleSection( map );
        SectionStackSection coordinateSection = createCoordinateSection( map );
        SectionStackSection legendSection = createLegendSectionStack( map );

        SectionStack rightSectionStack = createSectionStack( NOT_RESIZABLE );
        rightSectionStack.addSection( overviewMapSection );
        rightSectionStack.addSection( scaleSection );
        rightSectionStack.addSection( coordinateSection );
        rightSectionStack.addSection( legendSection );

        // ---------------------------------------------------------------------
        // Create header row (banners and links)
        // ---------------------------------------------------------------------
        HLayout headerLayoutTopRow = createHeaderLayoutTopRow();
        HLayout headerLayoutBottomRow = createHeaderLayoutBottomRow();

        // ---------------------------------------------------------------------
        // Create main vertical and horizontal layout
        // ---------------------------------------------------------------------
        HLayout mainHLayout = createMainHLayout();
        mainHLayout.addMember( leftSectionStack );
        mainHLayout.addMember( centerLayout );
        mainHLayout.addMember( rightSectionStack );

        VLayout mainLayout = createMainLayout();
        mainLayout.addMember( headerLayoutTopRow );
        mainLayout.addMember( headerLayoutBottomRow );

        // ---------------------------------------------------------------------
        // Finally draw everything
        // ---------------------------------------------------------------------
        initializeRibbonColumns();
        mainLayout.addMember( mainHLayout );
        mainLayout.draw();

        // ---------------------------------------------------------------------
        // Create dataGrid where result will be shown
        // ---------------------------------------------------------------------
        final DockableWindow searchWindow = new DockableWindow();
        searchWindow.setTitle( "Suchergebnisse" );
        searchWindow.setWidth( 650 );
        searchWindow.setHeight( 300 );
        searchWindow.moveTo( 50, 75 );
        searchWindow.setKeepInParentRect( true );
        searchWindow.setCanDragResize( true );
        searchResultGrid = new MultiFeatureListGrid( map );
        searchResultGrid.setClearTabsetOnSearch( true );
        searchResultGrid.setShowDetailsOnSingleResult( true );
        searchResultGrid.setSortFeatures( true );
        searchWindow.addItem( searchResultGrid );

        // ---------------------------------------------------------------------
        // Create Searchpanels
        // ---------------------------------------------------------------------
        SearchWidgetRegistry.initialize( map, searchResultGrid );
        SearchWidgetRegistry.put( new AttributeSearchCreator() );
        SearchWidgetRegistry.put( new CombinedSearchCreator() );
        SearchWidgetRegistry.put( new SearchFavouritesListCreator() );
        SearchWidgetRegistry.put( new GeometricSearchCreator( new GeometricSearchPanelCreator() {
            @Override
            public GeometricSearchPanel createInstance( MapWidget mapWidget ) {
                GeometricSearchPanel gsp = new GeometricSearchPanel( mapWidget );
                gsp.addSearchMethod( new FreeDrawingSearch() );
                return gsp;
            }
        } ) );

        // -- Show the grid after new result has been retrieved
        SearchWidgetRegistry.addSearchHandler( new SearchHandler() {
            @Override
            public void onSearchStart( SearchEvent event ) {
            }

            @Override
            public void onSearchDone( SearchEvent event ) {
                // handled by featureListGrid, no need for us to do something
            }

            @Override
            public void onSearchEnd( SearchEvent event ) {
                searchWindow.show();
                searchWindow.bringToFront();
            }
        } );
    }

    private VLayout createMainLayout() {
        VLayout mainLayout = new VLayout();
        mainLayout.setWidth100();
        mainLayout.setHeight100();
        mainLayout.setBackgroundColor( "#dfe8f6" );
        return mainLayout;
    }

    private HLayout createMainHLayout() {
        HLayout mainHLayout = new HLayout();
        mainHLayout.setWidth100();
        mainHLayout.setHeight100();
        mainHLayout.setMembersMargin( 5 );
        mainHLayout.setMargin( 5 );
        mainHLayout.setPadding( 5 );
        return mainHLayout;
    }

    private HLayout createHeaderLayoutTopRow() {
        HLayout headerLayoutTopRow = new HLayout();
        headerLayoutTopRow.setHeight( "40" );
        headerLayoutTopRow.setMembersMargin( 5 );
        headerLayoutTopRow.setPadding( 10 );
        headerLayoutTopRow.setAlign( Alignment.CENTER );
        Label title = new Label( dict.get( "headerTitle" ) );
        title.setWidth100();
        title.setStyleName( "appTitle" );
        headerLayoutTopRow.addMember( title );
        return headerLayoutTopRow;
    }

    private HLayout createHeaderLayoutBottomRow() {

        final DynamicForm headerBarForm = new DynamicForm();
        headerBarForm.setWidth( 250 );
        SelectItem linkSelectItem = new SelectItem( "label", messages.planType() );
        linkSelectItem.setEmptyDisplayValue( messages.planTypeSelectionMessage() );
        linkSelectItem.setOptionDataSource( DropDownListDataSource.getInstance() );
        linkSelectItem.setDisplayField( "label" );
        linkSelectItem.setValueField( "link" );
        linkSelectItem.addChangeHandler( new ChangeHandler() {
            @Override
            public void onChange( ChangeEvent event ) {
                Window.Location.replace( event.getValue().toString() );
            }
        } );
        linkSelectItem.setWidth( 260 );
        headerBarForm.setFields( linkSelectItem );

        HLayout headerLayoutBottomRow = new HLayout();
        headerLayoutBottomRow.setHeight( "15" );
        headerLayoutBottomRow.setMembersMargin( 10 );
        headerLayoutBottomRow.setAlign( Alignment.LEFT );
        Canvas headerCanvas = new Canvas() {
            {
                addChild( headerBarForm );
            }
        };
        headerLayoutBottomRow.addMember( headerCanvas );

        return headerLayoutBottomRow;
    }

    private SectionStackSection createLayerTreeSection( MapWidget map ) {
        CombinedLayertree layerTree = new CombinedLayertree( map, true );
        SectionStackSection layerTreeSection = new SectionStackSection( messages.layerTreeTitle() );
        layerTreeSection.setExpanded( true );
        layerTreeSection.addItem( layerTree );
        return layerTreeSection;
    }

    private IButton createDragAndDropLayerListButton( final MapWidget map ) {
        IButton button = new IButton( messages.dragAndDropLayerListButtonTitle() );
        button.setHeight( 40 );
        button.setWidth100();
        button.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                DragAndDropLayerList layerList = new DragAndDropLayerList( map );
                new DragAndDropLayerListWindow( layerList );
            }
        } );
        return button;
    }

    private RibbonBarLayout createCenterRibbon( MapWidget map ) {
        RibbonBarLayout centerRibbon = new RibbonBarLayout( map, APPLICATION_NAME, "ribbon-bar-1" );
        centerRibbon.setWidth100();
        centerRibbon.setHeight( 45 );
        centerRibbon.setOverflow( Overflow.HIDDEN );
        centerRibbon.setMembersMargin( 0 );
        centerRibbon.setStyleName( "myRibbon" );
        return centerRibbon;
    }

    private SectionStack createSectionStack( boolean isResizable ) {
        final SectionStack sectionStack = new SectionStack();
        sectionStack.setBorder( "1px solid #99bbe8" );
        sectionStack.setStyleName( "round_corner" );
        sectionStack.setVisibilityMode( VisibilityMode.MULTIPLE );
        sectionStack.setCanReorderSections( true );
        sectionStack.setCanResizeSections( false );
        sectionStack.setSize( "240px", "100%" );
        sectionStack.setShowResizeBar( isResizable );
        return sectionStack;
    }

    private VLayout createCenterLayout( MapWidget map, RibbonBarLayout ribbon, boolean isResizable ) {
        VLayout mapLayout = new VLayout();
        mapLayout.addMember( ribbon );
        mapLayout.addMember( map );
        mapLayout.setHeight( "65%" );
        VLayout centerLayout = new VLayout();
        centerLayout.setBorder( "1px solid #99bbe8" );
        centerLayout.setStyleName( "round_corner" );
        centerLayout.addMember( mapLayout );
        centerLayout.setShowResizeBar( isResizable );
        centerLayout.setResizeBarTarget( "next" );
        return centerLayout;
    }

    private SectionStackSection createOverviewMapSection( MapWidget map ) {
        OverviewMap overviewMap = new OverviewMap( "mapOverview", APPLICATION_NAME, map, false, true );
        overviewMap.setTargetMaxExtentRectangleStyle( new ShapeStyle( "#888888", 0.3f, "#666666", 0.75f, 2 ) );
        overviewMap.setRectangleStyle( new ShapeStyle( "#6699FF", 0.3f, "#6699CC", 1f, 2 ) );
        overviewMap.setHeight( 200 );
        SectionStackSection overviewMapSection = new SectionStackSection( messages.overviewMapTitle() );
        overviewMapSection.setExpanded( true );
        overviewMapSection.addItem( overviewMap );
        return overviewMapSection;
    }

    private SectionStackSection createCoordinateSection( MapWidget map ) {
        RibbonBarLayout coordinateDisplayRibbonLayout = new RibbonBarLayout( map, APPLICATION_NAME,
                                                                             "ribbon-coorddisplay" );
        coordinateDisplayRibbonLayout.setHeight( 50 );
        coordinateDisplayRibbonLayout.setWidth100();
        coordinateDisplayRibbonLayout.setOverflow( Overflow.HIDDEN );
        coordinateDisplayRibbonLayout.setStyleName( "myRibbon" );
        coordinateDisplayRibbonLayout.setAlign( Alignment.CENTER );
        SectionStackSection coordinateSection = new SectionStackSection( messages.coordinatesTitle() );
        coordinateSection.setExpanded( true );
        coordinateSection.addItem( coordinateDisplayRibbonLayout );
        return coordinateSection;
    }

    private SectionStackSection createScaleSection( MapWidget map ) {
        RibbonBarLayout scale = new RibbonBarLayout( map, APPLICATION_NAME, "ribbon-scaleselector" );
        scale.setHeight( 35 );
        scale.setWidth100();
        scale.setOverflow( Overflow.HIDDEN );
        scale.setStyleName( "myRibbon" );
        scale.setAlign( Alignment.CENTER );
        SectionStackSection scaleSection = new SectionStackSection( messages.scaleTitle() );
        scaleSection.setExpanded( true );
        scaleSection.addItem( scale );
        return scaleSection;
    }

    private SectionStackSection createLegendSectionStack( MapWidget map ) {
        final XPlanLegend legend = new XPlanLegend( map.getMapModel() );
        legend.setHeight100();
        legend.setWidth100();
        SectionStackSection sectionlegend = new SectionStackSection( messages.legendTitle() );
        sectionlegend.setExpanded( true );
        sectionlegend.addItem( legend );
        return sectionlegend;
    }

    private void initializeRibbonColumns() {

        RibbonColumnRegistry.put( "MouseLocationRibbonColumn", new RibbonColumnCreator() {
            @Override
            public RibbonColumn create( List<ClientToolInfo> tools, MapWidget mapWidget ) {
                return new DisplayCoordinatesRibbonColumn( mapWidget );
            }
        } );
        RibbonColumnRegistry.put( RefreshLayersAction.IDENTIFIER, new RibbonColumnCreator() {
            @Override
            public RibbonColumn create( List<ClientToolInfo> tools, MapWidget mapWidget ) {
                RibbonColumn rc = new RibbonButton( new RefreshLayersAction( mapWidget ) );
                return rc;
            }
        } );
    }
}
