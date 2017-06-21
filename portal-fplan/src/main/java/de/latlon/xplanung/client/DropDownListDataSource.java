package de.latlon.xplanung.client;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;

public class DropDownListDataSource extends DataSource {

    private static DropDownListDataSource instance = null;

    public DropDownListDataSource( String id ) {
        setID( id );
        setRecordXPath( "/List/entry" );
        DataSourceIntegerField pkField = new DataSourceIntegerField( "itemID" );
        pkField.setHidden( true );
        pkField.setPrimaryKey( true );

        DataSourceTextField labelField = new DataSourceTextField( "label", "Label", 128, true );
        DataSourceTextField linkField = new DataSourceTextField( "link", "Link", 128, true );

        setFields( labelField, linkField );
        setDataURL( "dropDownList.xml" );
        setClientOnly( true );
    }

    public static DropDownListDataSource getInstance() {
        if ( instance == null ) {
            instance = new DropDownListDataSource( "dropDownDs" );
        }
        return instance;
    }

}