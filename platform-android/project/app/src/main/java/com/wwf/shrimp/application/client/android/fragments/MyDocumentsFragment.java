package com.wwf.shrimp.application.client.android.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import com.wwf.shrimp.application.client.android.MyDocsFilterActivity;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wwf.shrimp.application.client.android.DocumentAttachingActivity;
import com.wwf.shrimp.application.client.android.DocumentLinkingActivity;
import com.wwf.shrimp.application.client.android.DocumentRecipientsActivity;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.adapters.DocumentCardItemAdapter;
import com.wwf.shrimp.application.client.android.dialogs.TabbedDocumentDialog;
import com.wwf.shrimp.application.client.android.listeners.ConnectivityReceiver;
import com.wwf.shrimp.application.client.android.models.dto.Document;
import com.wwf.shrimp.application.client.android.models.dto.DocumentType;
import com.wwf.shrimp.application.client.android.models.dto.Organization;
import com.wwf.shrimp.application.client.android.models.dto.User;
import com.wwf.shrimp.application.client.android.models.dto.search.SearchResult;
import com.wwf.shrimp.application.client.android.models.view.DocumentCardItem;
import com.wwf.shrimp.application.client.android.models.view.FilterDTO;
import com.wwf.shrimp.application.client.android.services.CameraIntentService;
import com.wwf.shrimp.application.client.android.services.dao.DocumentJSONService;
import com.wwf.shrimp.application.client.android.system.SessionData;
import com.wwf.shrimp.application.client.android.DocumentTaggingActivity;
import com.wwf.shrimp.application.client.android.utils.DocumentPOJOUtils;
import com.wwf.shrimp.application.client.android.utils.RESTUtils;
import com.wwf.shrimp.application.client.android.utils.dialogs.ErrorConnectingDialogUtility;
import com.wwf.shrimp.application.client.android.utils.listeners.ClickListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;




/**
 * Fragment which will hold the list of the user's own documents
 * @author argolite
 */
public class MyDocumentsFragment extends Fragment implements View.OnClickListener
                                        , ConnectivityReceiver.ConnectivityReceiverListener
                                         {

    // logging tag
    private static final String LOG_TAG = "List My Docs Activity";
    private static final int MY_DOCUMENTS_FRAGMENT_ACTIVITY_ID = 2;
    // GPS Functionality
    private FusedLocationProviderClient fusedLocationClient;

    // session data access
    public static final String FRAGMENT_TAG = "List My Docs Activity";
    public static final String ADAPTER_DATA_TAG = "Adapter List  My Docs Activity";

    //
    // UI elements
    //
    private SwipeRefreshLayout mSwipeRefreshLayout;
    Menu menu;

    // The Dimmer View
    View backgroundDimmer;
    //
    // UI Data

    // adapters
    public static final String RECYCLER_ADAPTER_KEY = "MyDocumentsFragment";
    private RecyclerView recyclerView;
    private DocumentCardItemAdapter mAdapter;
    private List<DocumentCardItem> documentList = new ArrayList<DocumentCardItem>();
    private List<DocumentCardItem> syncedInstanceCardItems = null;

    // global session data
    private SessionData globalVariable;

    // local services for offline data storage
    private DocumentJSONService documentService;
    // the adapter used for holding the GUI card items
    public DocumentCardItemAdapter getAdapter() {
        return mAdapter;
    }

    public MyDocumentsFragment() {
        // Required empty public constructor
    }

    /**
     * Get the instance of this fragment
     * @return - instance of the fragment
     */
    public static MyDocumentsFragment getInstance() {
        MyDocumentsFragment fragment = new MyDocumentsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Instance creation with global data access initialization
     * @param savedInstanceState - the saved instance data from previous instance.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "CREATE Call for MyDocs Doc");
        super.onCreate(savedInstanceState);
        globalVariable  = (SessionData) getContext().getApplicationContext();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.mydocs_page, menu);
         this.menu = menu;
         super.onCreateOptionsMenu(menu, inflater);
     }

     @Override
     public void onPrepareOptionsMenu(Menu menu) {

         MenuItem settingsItem = menu.findItem(R.id.action_doc_filter);
         // set your desired icon here based on a flag if you like
         //
         // check the menu items syatus
         if(globalVariable.getMyDocsFilter().getStatus() == FilterDTO.FILTER_STATUS_CLEAR){
             settingsItem.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_filter_no_checkmark));
         } else if(globalVariable.getMyDocsFilter().getStatus() == FilterDTO.FILTER_STATUS_SET){
             settingsItem.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_filter_and_checkmark));
         }
          super.onPrepareOptionsMenu(menu);
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.action_doc_filter:
                 Toast.makeText(getActivity(), "Filter Click", Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(getContext(), MyDocsFilterActivity.class);
                 startActivityForResult(intent, MyDocsFilterActivity.MY_DOCUMENTS_FILTER_ACTIVITY_ID);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }

    /**
     * Instance creation with global data access initialization
     */
    @Override
    public void onStart() {
        Log.i(LOG_TAG, "START Call for MyDocs Doc");
        super.onStart();
    }

    /**
     * Infate and initialize the view of the fragment
     * @param inflater - inflater to use
     * @param container - the container view
     * @param savedInstanceState - any saved instance of the pervious instance data
     * @return - the created and initialized view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "CREATE VIEW Call for MyDocs Doc");

        if (globalVariable.getDocumentLocalMap().get(MyDocumentsFragment.ADAPTER_DATA_TAG) != null
                && savedInstanceState != null) {

            Log.i(LOG_TAG, "RESTORE INSTANCE Call for MyDocs Doc" +  ((mAdapter == null) ? "Adapter is *null*" : "Adapter is NOT *null*"));
            Log.i(LOG_TAG, "RESTORE INSTANCE Call for MyDocs Doc" +  ((globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CREATION) ? "DOCUMENT CREATION" : "DOCUMENT EDIT"));

                syncedInstanceCardItems = globalVariable.getDocumentLocalMap().get(MyDocumentsFragment.ADAPTER_DATA_TAG);
                savedInstanceState.putString(MyDocumentsFragment.FRAGMENT_TAG, null);
        }


        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_my_documents, container, false);

        // Refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        final FloatingActionsMenu menuFABAction = (FloatingActionsMenu)  view.findViewById(R.id.fab_menu_document_types);
        backgroundDimmer = view.findViewById(R.id.background_dimmer);
        backgroundDimmer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        globalVariable.setFabMenu(MyDocumentsFragment.FRAGMENT_TAG, menuFABAction);
        menuFABAction.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                backgroundDimmer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                backgroundDimmer.setVisibility(View.GONE);
            }
        });

        //
        // Fab buttons
        for(int i=0; i < globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().size(); i++){
            // change the colors and the visibility
            if(globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().get(i).getDocumentDesignation().equals("Passthrough")){
                FloatingActionButton button = createFABBUtton(globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().get(i));
                menuFABAction.addButton(button);
            }

        }

        wireFABDocTypeButtons();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission to save videos in external storage
            ActivityCompat.requestPermissions(
                    getActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, CameraIntentService.PERMISSION_RQ);
        }

        // Recycle view for the card data
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_documents);

        // continue with the adapter
        //
        mAdapter = new DocumentCardItemAdapter(documentList, getContext().getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new MyDocumentsFragment.RecyclerTouchListener(getContext().getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            /**
             * Simple tap/click override for each card.
             * Currently shows the full doc summary with edit capabilities.
             * This action does nto depend on nextDocument in global session
             *
             */
            public void onClick(View view, final int position) {
                // set the position in the global sessions
                globalVariable.setLongClickPositionMyDocument(position);
                Fragment parentFragment = null;
                DocumentCardItem documentCardItem = null;

                // Values are passing to activity & to fragment as well
                parentFragment = globalVariable.getCurrFragment();
                if(!(parentFragment instanceof MyDocumentsFragment))
                    return;
                if(parentFragment != null && ((MyDocumentsFragment)parentFragment).getAdapter() != null) {
                    documentCardItem = ((MyDocumentsFragment)parentFragment).getAdapter().getDataSet().get(position);
                }else{
                    documentCardItem = getAdapter().getDataSet().get(position);
                }
                // set the data doc mode
                globalVariable.setDocumentCreationStatus(SessionData.DOCUMENT_EDIT);
                // display the image
                showFullDocumentPagedDialog(documentCardItem, TabbedDocumentDialog.DIALOG_MODE_READ_ONLY);
            }

            @Override
            /**
             * Long click contextual handler.
             * Will show options of what can be done with each specific document.
             *    1. Currently only delete will be implemented. Which will delete from both the server and the client device.
             */
            public void onLongClick(View view, int position) {
                //
                // Commit the data to REST Service
                final String deleteUrl = globalVariable.getConfigurationData().getServerURL()
                        + globalVariable.getConfigurationData().getApplicationInfixURL()
                        + globalVariable.getConfigurationData().getRestDocumentDeleteURL();
                final String idDeletionKey = "DELETE Document";

                // set the position in the global sessions
                globalVariable.setLongClickPositionMyDocument(position);
                final DocumentCardItem documentCardItem = globalVariable
                        .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                        .getDataSet()
                        .get(globalVariable.getLongClickPositionMyDocument());
                // <TODO> Removed the Queue doc

                PopupMenu popup = new PopupMenu(getContext(), view);
                // force icons
                try {
                    Field[] fields = popup.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popup);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper
                                    .getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod(
                                    "setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Internal issue with context based icons");
                    e.printStackTrace();
                }


                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.document_context_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //
                        // Manage tags for documents
                        if(getResources().getString(R.string.document_tag_management).equals(item.getTitle())){
                            //
                            // Place the document in the queue
                            globalVariable.setNextDocument(documentCardItem);

                            // Process
                            globalVariable.setDocumentCreationStatus(SessionData.DOCUMENT_CONTEXT_EDIT);
                            Intent intent = new Intent(getActivity(), DocumentTaggingActivity.class);
                            startActivityForResult(intent, DocumentTaggingActivity.EDIT_TAGS_FOR_DOCUMENT_REQUEST);
                            return true;
                        }

                        //
                        // Manage links for documents
                        if(getResources().getString(R.string.document_linked_document_management).equals(item.getTitle())){
                            //
                            // Place the document in the queue
                            globalVariable.setNextDocument(documentCardItem);

                            // Process
                            globalVariable.setDocumentCreationStatus(SessionData.DOCUMENT_CONTEXT_EDIT);
                            Intent intent = new Intent(getActivity(), DocumentLinkingActivity.class);
                            startActivity(intent);
                            return true;
                        }

                        //
                        // Manage attachements for documents
                        if(getResources().getString(R.string.document_attached_document_management).equals(item.getTitle())){
                            //
                            // Place the document in the queue
                            globalVariable.setNextDocument(documentCardItem);

                            // Process
                            globalVariable.setDocumentCreationStatus(SessionData.DOCUMENT_CONTEXT_EDIT);
                            Intent intent = new Intent(getActivity(), DocumentAttachingActivity.class);
                            startActivity(intent);
                            return true;
                        }

                        //
                        // Manage recipients for documents
                        if(getResources().getString(R.string.document_recipients_document_management).equals(item.getTitle())){
                            //
                            // Place the document in the queue
                            globalVariable.setNextDocument(documentCardItem);

                            // Process
                            globalVariable.setDocumentCreationStatus(SessionData.DOCUMENT_CONTEXT_EDIT);
                            Intent intent = new Intent(getActivity(), DocumentRecipientsActivity.class);
                            startActivity(intent);
                            return true;
                        }

                        /**
                         * Manage deletion of documents
                         */
                        if(getResources().getString(R.string.document_context_menu_delete).equals(item.getTitle())){
                            //
                            // delete the element from local database
                            //

                            // get the element id first
                            DocumentCardItem documentCardItem = globalVariable
                                    .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                                    .getDataSet()
                                    .get(globalVariable.getLongClickPositionMyDocument());

                            // make sure that the user can delete those
                            // only DRAFT and REJECTED can be removed
                            if(!(documentCardItem.getStatus().equals(Document.STATUS_DRAFT)
                                    || documentCardItem.getStatus().equals(Document.STATUS_REJECTED))){
                                //
                                // User cannot delete those
                                Toast.makeText(getContext(), "Cannot Delete.", Toast.LENGTH_SHORT).show();
                                Toast.makeText(getContext(), "You can only Delete DRAFT or REJECTED Documents.", Toast.LENGTH_LONG).show();
                                return true;
                            }

                            // Try to get data from the database
                            documentService = new DocumentJSONService(getContext());
                            documentService.open();
                                documentService.deleteDocument(documentCardItem.getSyncID());
                            documentService.close();
                            // remove it from the adapter as well
                            globalVariable
                                    .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                                    .getDataSet().remove(globalVariable.getLongClickPositionMyDocument());
                            globalVariable
                                    .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                                    .notifyDataSetChanged();

                            RESTUtils.executeDELETERequest(deleteUrl,documentCardItem.getSyncID(), globalVariable, idDeletionKey);
                            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        }));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItemsRemoteData();

            }
        });

        // place the adapter in global session
        globalVariable.setDocumentAdapter(RECYCLER_ADAPTER_KEY, mAdapter);

        //
        // Refresh the data items, which will fill in the documentList variable
        if(syncedInstanceCardItems == null){
            refreshItemsRemoteData();
        }else{
            refreshItemsLocalData();
        }


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    /**
     * Creation of a document through the FAB button
     */
    public void onClick(View v) {
        final DocumentCardItem document = new DocumentCardItem();
        document.setCreationTimestamp(new Date());
        document.setUsername((globalVariable.getCurrentUser().getCredentials()).getUsername());
        DocumentType documentType = new DocumentType();
        User user = globalVariable.getCurrentUser();
        long organizationId=-1;
        long groupId=-1;

        if(user.getUserOrganizations().size() > 0) {
            organizationId = user.getUserOrganizations().get(0).getId();
            groupId = user.getUserGroups().get(0).getId();
        }
        // get the name of the organization
        document.setGroupType(getGroupTypeName(organizationId, groupId));


        int id = v.getId();
        boolean filterisOn = false;

        Log.d("WWF POC", "FAB Clicked");
        documentType = getDocTypeByFABId(id);
        // snapCameraPhoto(getContext());
        document.setType(documentType.getValue());
        document.setName("Document");
        document.setDocumentType(documentType);
        document.setTypeHEXColor(documentType.getHexColorCode());
        // globalVariable.setNextDocument(document);
        globalVariable.setDocumentCreationStatus(SessionData.DOCUMENT_CREATION);

        if(!filterisOn) {
            Log.d(LOG_TAG, "The Done Mode is *Set* to DIALOG_MODE_WRITE");
            globalVariable.setDocumentCreationStatus(SessionData.DOCUMENT_CREATION);
            showFullDocumentPagedDialog(document, TabbedDocumentDialog.DIALOG_MODE_WRITE);

            globalVariable.getFabMenu(MyDocumentsFragment.FRAGMENT_TAG).collapse();
        }else{
            Log.d(LOG_TAG, "The Done Mode is *NOT Set* to DIALOG_MODE_WRITE");
        }

        Toast.makeText(globalVariable, "Connected: " + RESTUtils.checkConnection(), Toast.LENGTH_SHORT).show();

    }

    public void refreshList(){
        Log.i(LOG_TAG, "REFRESH LIST Call for MyDocs Doc");
        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void refreshListAtIndex(final int index){
        Log.i(LOG_TAG, "REFRESH LIST Call for MyDocs Doc");
        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                getAdapter().notifyItemChanged(index);
            }
        });
    }



    private Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
                // avoid double click signal
                return true;
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /**     * Will show the paged dialog to the user
     * @param document
     */
    protected void showFullDocumentPagedDialog(final DocumentCardItem document, int mode) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(TabbedDocumentDialog.FRAGMENT_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // set profile docs
        document.setAttachedDocuments(globalVariable.getProfileDocs());
        // set the document to be worked on the session
        globalVariable.setNextDocument(document);


        // Create and show the dialog.
        TabbedDocumentDialog dialogFragment = new TabbedDocumentDialog();
        dialogFragment.setDialogMode(mode);
        dialogFragment.setContainerFragment(this);
        globalVariable.setCurrFragment(this);
        // remove the local for now
        // dialogFragment.setDataMode(TabbedDocumentDialog.DATA_MODE_LOCAL);
        // switch to remote
        // dialogFragment.setDataMode(TabbedDocumentDialog.DATA_MODE_REMOTE);

        // experimental
        dialogFragment.setDataMode(TabbedDocumentDialog.DATA_MODE_REMOTE);
        dialogFragment.show(ft,TabbedDocumentDialog.FRAGMENT_TAG);
    }

    private long getDocTypeIdFromValue(String value){
        long result=0;
        for(int i=0; i< globalVariable.getDocumentTypes().size(); i++){
            if(value.equals(globalVariable.getDocumentTypes().get(i).getValue())){
                result = globalVariable.getDocumentTypes().get(i).getId();
                return result;
            }
        }
        return result;

    }

    private String[] convertDocTypeData(List<DocumentType> docTypes){
        String[] result = new String[docTypes.size() + 1];
        result[0] = getResources().getString(R.string.document_filter_all_doctypes_options);

        for(int i=0; i< docTypes.size(); i++){
           //  String documentType = getResources().getString(MappingUtilities.documentTypeIDToRTypeMap.get(docTypes.get(i).getId()));
            //int docTypeID = this.getResources().
            //        getIdentifier(docTypes.get(i).getName(), "string", getActivity().getPackageName());
            //String documentType = getResources().getString(docTypeID);

            //
            // Get the resource data
            String documentType = globalVariable.getInternationalizedResourceString(docTypes.get(i).getName());
            result[i+1] = documentType;
        }

        return result;
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "RESUME Call for MyDocs Doc");
        super.onResume();

        /**if(globalVariable.getNextDocumentStackSize() > 0){
            mAdapter = globalVariable.getDocumentAdapter(MyDocumentsFragment.RECYCLER_ADAPTER_KEY);
            getAdapter().notifyDataSetChanged();
        }
         */
        if(globalVariable.getDocumentCreationStatus() == SessionData.DOCUMENT_CONTEXT_EDIT
                && globalVariable.getNextDocumentStackSize() ==1) {
            Log.i(LOG_TAG, "RESUME Replacing Item for Click Call for MyDocs Doc");
            getAdapter().replaceCardItem(globalVariable.getNextDocument());
            getAdapter().notifyItemChanged(globalVariable.getLongClickPositionMyDocument());
            globalVariable.popNextDocument();
        }
        //refreshUI();

        // register connection status listener
        SessionData.getInstance().setConnectivityListener(this);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        Log.i(LOG_TAG, "INSTANCE SAVE Call for MyDocs Doc");
        //
        // save the adapter data
        // TODO create parcelable

        //String instanceState = RESTUtils.getJSON(getAdapter().getDataSet());
        globalVariable.getDocumentLocalMap().put(MyDocumentsFragment.ADAPTER_DATA_TAG, getAdapter().getDataSet());
        //savedInstanceState.putString(FRAGMENT_TAG, instanceState);
    }



    @Override
    public void onStop(){
        super.onStop();
        Log.i(LOG_TAG, "OnSTOP Call for MyDocs Doc");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(LOG_TAG, "ON PAUSE Call for MyDocs Doc");
    }

    /**
     * Re-Fetch the data from the back-end and the local offline database
     */
    void refreshItemsRemoteData() {
        if(RESTUtils.checkConnection() == false){
            if(globalVariable.isConnected() == false){
                // do nothing
            }else{
                globalVariable.setConnected(false);
                Toast.makeText(getContext(), "LOST Internet Connectivity", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "LOST Internet Connectivity");
            }


            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        if(RESTUtils.checkConnection() == true){
            if(globalVariable.isConnected() == false){
                globalVariable.setConnected(true);
                Toast.makeText(getContext(), "Internet connectivity is back ON.", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "Internet connectivity is back ON.");
            }
        }

        mSwipeRefreshLayout.setRefreshing(true);

        /**
         * Get documents from the back-end
         */
        String url = globalVariable.getConfigurationData().getServerURL()
                + globalVariable.getConfigurationData().getApplicationInfixURL()
                + globalVariable.getConfigurationData().getRestDocumentFetchAllURL();
        Log.i(LOG_TAG, "REST - get all documents - call: " + url);

        // Create the handler
        MyDocumentsFragment.OkHttpDocumentDataHandler okHttpHandler= new MyDocumentsFragment.OkHttpDocumentDataHandler();

        // execute the call synchronously and process it in {@link OkHttpDocumentDataHandler#onPostExecute(String) onPostExecute} method
        okHttpHandler.execute(url);

    }

    /**
     * Re-Fetch the data from the back-end and the local offline database
     */
    void refreshItemsLocalData() {
        //
        // Try to get data from the local database
        //documentService = new DocumentService(getContext());
        documentService.open();
        //    documentList = documentService.getAllMyDocuments((globalVariable.getCurrentUser().getCredentials()).getUsername());
        //documentService.close();
        if(globalVariable.isDocumentSearchFilterState(SessionData.DOCUMENT_FILTER_MY_DOCS)){
            // TODO bypass remote search for now
            DocumentPOJOUtils.filterDocuments(globalVariable, MyDocumentsFragment.RECYCLER_ADAPTER_KEY);
            // get the number of records
            DocumentCardItemAdapter adapter = globalVariable.getDocumentAdapter(RECYCLER_ADAPTER_KEY);
            adapter.getDataSet().size();

            return;
        }

        applyFilters(syncedInstanceCardItems);
    }

    void initializeDocumentFilterBottomSheet(){
        List<DocumentType> docTypes;
        String[] docTypesList;
        // add lookup data to the session
        docTypes = globalVariable.getDocumentTypes();
        docTypesList = convertDocTypeData(docTypes);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, docTypesList);
    }

    /**
     * Inner class to handle the back-end communication to get the data from the db
     */
    public class OkHttpDocumentDataHandler extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(globalVariable.getConfigurationData().getClientSocketTimeoutInSeconds(), TimeUnit.SECONDS)
                .build();


        boolean serverDownFlag = false;
        String requestURL;

        @Override
        protected String doInBackground(String... params) {

            Request request  = new Request.Builder()
                    .url(params[0])
                    .addHeader("user-name", globalVariable.getCurrentUser().getName())
                    .build();



            requestURL = request.url().toString();
            Log.d(LOG_TAG, "OkHTTP Request " + request.url());

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                Log.e(LOG_TAG, "OkHTTP Request Failed");
                e.printStackTrace();
                serverDownFlag = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(LOG_TAG, "FETCHING REMOTE DATA Call for MyDocs Doc" );


            //
            // preconditions
            // check that we got anything from the server
            if(serverDownFlag ==true){
                Log.d(LOG_TAG, "Server failed getting document data." );

                // set the dialog
                mSwipeRefreshLayout.setRefreshing(false);

                ErrorConnectingDialogUtility.showServerSystemErrorDialog(getContext());
                return;
            }

            //
            // extract the data for the GSON authentication

            // documents holder for fetching all documents
            ArrayList<Document> documents;
            ArrayList<DocumentCardItem> cardItems;
            // document holder for filtered documents
            SearchResult<Document> searchResult;
            // Users
            ArrayList<User> users;
            // document types
            ArrayList<DocumentType> docTypes;

            //
            // UI data

            // card items for documents
            List<Document> cachedDocs = null;

            // doc types
            String[] docTypesList;

            //
            // GSON parser
            Gson gson = new GsonBuilder()
                    .create();

            //
            // parse the JSON input into the specific class

            ///////////////////////////////////////////////////////////////////////////////////////
            // Getting documents from the backend
            //
            if(requestURL.contains(globalVariable.getConfigurationData().getRestDocumentFetchAllURL())
                    || requestURL.contains(globalVariable.getConfigurationData().getRestDocumentSearchURL().substring(0
                        , globalVariable.getConfigurationData().getRestDocumentSearchURL().length()-1))){
                Type listType = new TypeToken<ArrayList<Document>>(){}.getType();
                documents = gson.fromJson(s, listType);

                //
                // get any cached data
                Context myContext = getContext();
                if(myContext != null) {
                    documentService = new DocumentJSONService(myContext);
                    documentService.open();
                    // returns card items need to get documents
                    cachedDocs = documentService.getAllMyDocuments(globalVariable.getCurrentUser().getName());
                    documentService.close();
                }

                // sync
                if(documents != null && cachedDocs != null) {
                    documents.addAll(cachedDocs);
                    syncDocuments(documents);
                }

                // set the dialog
                mSwipeRefreshLayout.setRefreshing(false);
            }

            // refresh the adapter data
            /**
            globalVariable
                    .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                    .getDataSet().clear();
            globalVariable
                    .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                    .getDataSet().addAll(documentList);
            globalVariable
                    .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                    .notifyDataSetChanged();

            // get the number of records
            textViewNumberRecords.setText("" + documentList.size());
             */

        }
    }

    /**
     *
     * @param documents
     */
    private void syncDocuments(List<Document> documents){
        List<Document> filteredDocuments = new ArrayList<Document>();
        // card items for documents
        List<DocumentCardItem> filteredCardItems;
        Log.i(LOG_TAG, "Syncing Document Data " + documents);

        for(int i=0; i< documents.size(); i++){
            // if the owner is not current user then skip
            if(!documents.get(i).getOwner().equals(globalVariable.getCurrentUser().getCredentials().getUsername())
                    || documents.get(i).getType().getDocumentDesignation().equals("Profile")){
                continue;
            }

            // otherwise check if we have this document in the list
            // int docIndex = getIndexOfDocument(documents.get(i).getSyncID());
            // if (docIndex == -1) {
                // the doc is not in the list so we add it
                filteredDocuments.add(documents.get(i));
                // Log.i(LOG_TAG, "Adding a remote doc to the local docs list");
            // }

            //
            // Add tags
            /**
            if(documents.get(i).getTags().size() > 0) {
                int index = getIndexOfDocument(documents.get(i).getSyncID());
                if (index != -1) {
                    documentList.get(index).setTags(documents.get(i).getTags());
                    Log.i(LOG_TAG, "[FOUND TAGS] Syncing Local Doc Tags");
                }
            }

            //
            // add linked documents
            if(documents.get(i).getLinkedDocuments().size() > 0) {
                int index = getIndexOfDocument(documents.get(i).getSyncID());
                if (index != -1) {
                    documentList.get(index).setLinkedDocuments(documents.get(i).getLinkedDocuments());
                    Log.i(LOG_TAG, "[FOUND LINKED DOCS] Syncing Local Doc Links");
                }
            }

            //
            // add attached documents
            if(documents.get(i).getAttachedDocuments().size() > 0) {
                int index = getIndexOfDocument(documents.get(i).getSyncID());
                if (index != -1) {
                    documentList.get(index).setAttachedDocuments(documents.get(i).getAttachedDocuments());
                    Log.i(LOG_TAG, "[FOUND ATTACHED DOCS] Syncing Local Doc Attachments");
                }
            }
             **/
        }

        //
        // Convert documents to internal representation
        filteredCardItems = DocumentPOJOUtils.convertDocuments(filteredDocuments, globalVariable, MyDocumentsFragment.this, false);
        Log.d(LOG_TAG, "The cardItems: " + filteredCardItems);

        // sort the entities by their date
        Collections.sort(filteredCardItems, new Comparator<DocumentCardItem>() {
            @Override
            public int compare(DocumentCardItem lhs, DocumentCardItem rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getCreationTimestamp().after(rhs.getCreationTimestamp()) ? -1 : (lhs.getCreationTimestamp().before(rhs.getCreationTimestamp()) ) ? 1 : 0;
            }
        });

        applyFilters(filteredCardItems);

    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        // Toast.makeText(globalVariable, "Connected: " + isConnected, Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG, "Connectivity Changed!!! " + isConnected);
    }

    private int getIndexOfDocument(String syncId){
        int index = -1;

        for(int i=0; i<documentList.size(); i++){
            if(documentList.get(i).getSyncID().equals(syncId)){
                return i;
            }
        }
        return index;
    }

    private String getGroupTypeName(long orgId, long groupId){
        String result = "N/A";
        int orgIndex = -1;
        for(int i=0; i< globalVariable.getCurrentUser().getUserOrganizations().size(); i++){
            if(globalVariable.getCurrentUser().getUserOrganizations().get(i).getId() == orgId){
                orgIndex = i;
            }
        }
        Organization tempOrg = globalVariable.getCurrentUser().getUserOrganizations().get(orgIndex);
        for(int i=0; i < tempOrg.getSubGroups().size(); i++){
            if(tempOrg.getSubGroups().get(i).getId() == groupId){
                result = tempOrg.getSubGroups().get(i).getName();
            }
        }
        return result;
    }

    private void hideFABDocTypeButtons(){
        FloatingActionsMenu menuFABAction = globalVariable.getFabMenu(MyDocumentsFragment.FRAGMENT_TAG);
        for(int i=0; i<globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().size(); i++){
            FloatingActionButton button = (FloatingActionButton)  menuFABAction.getChildAt(i);
            button.setVisibility(View.GONE);
            // fabDocTypeButtons[i].setOnClickListener(this);
        }
    }

    private void showFABDocTypeButtons(){
        FloatingActionsMenu menuFABAction = globalVariable.getFabMenu(MyDocumentsFragment.FRAGMENT_TAG);
        for(int i=0; i<globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().size(); i++){
            FloatingActionButton button = (FloatingActionButton)  menuFABAction.getChildAt(i);
            button.setVisibility(View.VISIBLE);
            // fabDocTypeButtons[i].setOnClickListener(this);
        }
    }

    private void wireFABDocTypeButtons(){
        FloatingActionsMenu menuFABAction = globalVariable.getFabMenu(MyDocumentsFragment.FRAGMENT_TAG);

        int j=0;
        for(int i=0; i<globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().size(); i++){
            if(globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().get(i).getDocumentDesignation().equals("Passthrough")){
                FloatingActionButton button = (FloatingActionButton)  menuFABAction.getChildAt(j++);
                button.setOnClickListener(this);
            }

            // fabDocTypeButtons[i].setOnClickListener(this);
        }
    }

    private FloatingActionButton createFABBUtton(DocumentType docType){
        FloatingActionButton button = null;

        // prep some data
        //int resourceId = this.getResources()
        //        .getIdentifier(docType.getName(), "string", getActivity().getPackageName());
        //
        // Get the resource data
        String documentType = globalVariable.getInternationalizedResourceString(docType.getName());

        button =  new FloatingActionButton(getActivity());
        button.setColorNormal(Color.parseColor(docType.getHexColorCode()));
        button.setColorPressed(Color.parseColor(docType.getHexColorCode()));
        button.setSize(FloatingActionButton.SIZE_MINI);
        button.setTitle(documentType);
        button.setId((int)docType.getId());

        return button;

    }



    private void applyFilters(List<DocumentCardItem> filteredCardItems){
        // apply any filter data
        List<DocumentCardItem> tempDocumentList = null;
        if(globalVariable.isDocumentSearchFilterState(SessionData.DOCUMENT_FILTER_MY_DOCS)){
            DocumentType docType = globalVariable.getDocumentSearchFilterData(SessionData.DOCUMENT_FILTER_MY_DOCS).getDocType();
            tempDocumentList = new ArrayList<>();
            for(int i=0; i<filteredCardItems.size(); i++){
                if(filteredCardItems.get(i).getDocumentType().getId() == docType.getId()){
                    tempDocumentList.add(filteredCardItems.get(i));
                }
            }
        }
        if(tempDocumentList != null){
            filteredCardItems = tempDocumentList;
        }

        Log.i(LOG_TAG, "Applying Filters with SAVED STATE DATA for Click Call for MyDocs Doc");
        // set the new global data
        globalVariable.getDocumentLocalMap().put(MyDocumentsFragment.RECYCLER_ADAPTER_KEY, filteredCardItems);

        // refresh the adapter data
        globalVariable
                .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                .getDataSet().clear();
        globalVariable
                .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                .getDataSet().addAll(filteredCardItems);
        globalVariable
                .getDocumentAdapter(RECYCLER_ADAPTER_KEY)
                .notifyDataSetChanged();
    }

    private DocumentType getDocTypeByFABId(int fabId){
        for(int i=0; i < globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().size(); i++){
            // change the colors and the visibility
            if(globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().get(i).getId() == fabId){
                return  globalVariable.getCurrentUser().getUserGroups().get(0).getAllowedDocTypes().get(i);
            }
        }

        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (DocumentTaggingActivity.EDIT_TAGS_FOR_DOCUMENT_REQUEST) : {
                if (resultCode == Activity.RESULT_OK) {
                    // TODO Extract the data returned from the child Activity.
                    refreshList();
                }
                break;
            }
            case (MyDocsFilterActivity.MY_DOCUMENTS_FILTER_ACTIVITY_ID) : {
                //if (resultCode == Activity.RESULT_OK) {
                    // TODO Extract the data returned from the child Activity.
                    getActivity().invalidateOptionsMenu();
                //}
                break;
            }
        }
    }
}
