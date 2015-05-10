package com.vtech.offers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity implements
ConnectionCallbacks, OnConnectionFailedListener{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;   
   
    
    protected static final String TAG = "main-activity";
    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.actions_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
               // R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
        
       buildGoogleApiClient();
    }
    
    protected synchronized void buildGoogleApiClient() {
    	mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    	if(mGoogleApiClient != null){
    		System.out.println("got mGoogleApiClient");
    		try {
    			mGoogleApiClient.connect();
    		}catch(Exception e){
    			System.out.println("got exception===>"+e.getMessage());
    		}
    		
    	} else {
    		System.out.println("mGoogleApiClient is null");
    	}
    	System.out.println("buildGoogleApiClient end ***********");    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_websearch:
            // create intent to perform web search for this planet
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class PlanetFragment extends Fragment {
        public static final String ARG_PLANET_NUMBER = "planet_number";

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_planet, container, false);
            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            String actionName = getResources().getStringArray(R.array.actions_array)[i];

            int imageId = getResources().getIdentifier(getImageId(actionName),
                            "drawable", getActivity().getPackageName());
            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
            getActivity().setTitle(actionName);
            return rootView;
        }
        
        public String getImageId(String actionName){
        	String imageId="";
        	
        	switch (actionName){
        		case "Home":
        			imageId = "home";
        			break;
        		case "Your Exclusive Offers":
        			imageId = "yourexclusiveoffers";
        			break;
        		case "Nearby Offers":
        			imageId = "nearbyoffers";
        			break;
        		case "Online Offers":
        			imageId = "onlineoffers";
        			break;
        		case "Offers by Location":
        			imageId = "offersbylocation";
        			break;
        		case "Offers by Category":
        			imageId = "offersbycategory";
        			break;
        		case "Invite a Friend":
        			imageId = "inviteafriend";
        			break;
        		case "Settings":
        			imageId = "settings";
        			break;
        		default:
        			imageId = "home";
        			break;
        	}
        	return imageId;
        }
    }
	

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		 mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
	                mGoogleApiClient);
	        if (mLastLocation != null) {	                    
	            System.out.println("latitude====>"+String.valueOf(mLastLocation.getLatitude()));
	            System.out.println("longitude====>"+String.valueOf(mLastLocation.getLongitude()));
	            displayAddress(mLastLocation);
	        }
	}
	
	public void displayAddress(Location location){
		
		List<Address> addresses = null;
		String errorMessage = "";

	    try {
	    	 Geocoder geocoder = new Geocoder(this, Locale.getDefault());
	        addresses = geocoder.getFromLocation(
	                location.getLatitude(),
	                location.getLongitude(),
	                // In this sample, get just a single address.
	                1);
	        if(!addresses.isEmpty()){
	        	Address address = addresses.get(0);
	        	String adminArea = address.getAdminArea();
	        	String subAdminArea = address.getSubAdminArea();
	        	String locality = address.getLocality();	        	
	        	Toast.makeText(this, adminArea+" "+subAdminArea+" "+locality, Toast.LENGTH_LONG).show();
	        }
	    } catch (IOException ioException) {
	        // Catch network or other I/O problems.
	        errorMessage = getString(R.string.service_not_available);
	        Log.e(TAG, errorMessage, ioException);
	    	
	    } catch (IllegalArgumentException illegalArgumentException) {
	        // Catch invalid latitude or longitude values.
	        errorMessage = getString(R.string.invalid_lat_long_used);
	        Log.e(TAG, errorMessage + ". " +
	                "Latitude = " + location.getLatitude() +
	                ", Longitude = " +
	                location.getLongitude(), illegalArgumentException);
	    	
	    }
	}

	
	
	@Override
    public void onConnectionFailed(ConnectionResult result) {       
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
	
	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		 Log.i(TAG, "Connection suspended");
	        mGoogleApiClient.connect();
	}
	

}
