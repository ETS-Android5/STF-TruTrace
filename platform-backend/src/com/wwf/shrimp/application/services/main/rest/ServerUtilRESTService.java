package com.wwf.shrimp.application.services.main.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wwf.shrimp.application.models.AppResource;
import com.wwf.shrimp.application.models.DynamicFieldType;
import com.wwf.shrimp.application.models.LookupEntity;
import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.models.search.LookupDataSearchCriteria;
import com.wwf.shrimp.application.models.search.UserSearchCriteria;
import com.wwf.shrimp.application.services.main.BaseRESTService;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.LookupDataMySQLDao;
import com.wwf.shrimp.application.services.main.dao.impl.mysql.UserMySQLDao;
import com.wwf.shrimp.application.utils.DateUtility;
import com.wwf.shrimp.application.utils.RESTUtility;

/**
 * Server Utility Service which will provide information about the 
 * server itself such as if it is running or how long it has been 
 * running etc...
 * 
 * It is basically a collection of useful REST GET commands for the server
 * It is deployed to allow the pinging and checking of the server status and state.
 * 
 * <TODO> Change the verbs to be fully restful
 * 
 * @author argolite
 *
 */

@Path("/server")
public class ServerUtilRESTService extends BaseRESTService {
	
	public static final String SERVER_CODE_VERSION = "1.124";
	
	/**
	 * Services used by the implementation
	 */
	private LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria> lookupService = new LookupDataMySQLDao<LookupEntity, LookupDataSearchCriteria>();
	private UserMySQLDao<User, UserSearchCriteria> userService = new UserMySQLDao<User, UserSearchCriteria>();
	
	@GET
	@Path("/verify")
	@Produces(MediaType.TEXT_PLAIN)
	public Response verifyRESTService(InputStream incomingData) {
		String result = "/server/ com.wwf.shrimp.application is online...[" 
				+ DateUtility.simpleDateFormat(new Date(), DateUtility.FORMAT_DATE_AND_TIME) 
				+ "]";
		
		getLog().info("Got a call from the client: " 
				+ DateUtility.simpleDateFormat(new Date(), DateUtility.FORMAT_DATE_AND_TIME));
 
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(result).build();
	}
	
	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	/**
	 * 
	 * @param incomingData
	 * @return
	 */
	public Response versionRESTService(InputStream incomingData) {
		String result = "/server/version is " + SERVER_CODE_VERSION + " [Milestone#3]";
		 
		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(result).build();
	}
	
	@GET
	@Path("/languages")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * @param userName
	 * @return
	 */
	public Response getApplicationLanguages(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
			// results
			List<LookupEntity> allResources=null;
			User user = null;
			
			getLog().info("HEADER user-name: " + userName);
			
			//
			// Initialize services
			lookupService.init();
			userService.init();
			
			/**
			 * Process the request
			 */
			try {
				user = userService.getUserByName(userName);
				allResources = lookupService.getAllAppLanguagesForOrg(user.getUserOrganizations().get(0).getId()); 

			} catch (Exception e) {
				getLog().error("Error Fetching Documents: - " + e);
			}
			
			getLog().debug("FETCH ALL Resources: - Result" + allResources);
			// return HTTP response 200 in case of success
			return Response.status(200).entity(RESTUtility.getJSON(allResources)).build();
	}
	
	@GET
	@Path("/resources")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * @param userName
	 * @return
	 */
	public Response getApplicationResources(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
			// results
			List<AppResource> allResources=null;
			
			getLog().info("HEADER user-name: " + userName);
			
			//
			// Initialize service
			lookupService.init();
			
			/**
			 * Process the request
			 */
			try {
			
				allResources = lookupService.getAllAppResources(); 

			} catch (Exception e) {
				getLog().error("Error Fetching Documents: - " + e);
			}
			
			getLog().debug("FETCH ALL Resources: - Result" + allResources);
			// return HTTP response 200 in case of success
			return Response.status(200).entity(RESTUtility.getJSON(allResources)).build();
	}

	
	@GET
	@Path("/dynamicfieldtypes")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * @param userName
	 * @return
	 */
	public Response getDynamicFieldDefinitions(
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
			// results
			List<DynamicFieldType> allDynamicFieldTypes=null;
			
			getLog().info("HEADER user-name: " + userName);
			
			//
			// Initialize service
			lookupService.init();
			
			/**
			 * Process the request
			 */
			try {
			
				allDynamicFieldTypes = lookupService.getAllDynamicFieldTypes(); 

			} catch (Exception e) {
				getLog().error("Error Fetching Documents: - " + e);
			}
			
			getLog().debug("FETCH ALL Dynamic Field Types: - Result" + allDynamicFieldTypes);
			// return HTTP response 200 in case of success
			return Response.status(200).entity(RESTUtility.getJSON(allDynamicFieldTypes)).build();
	}
	
	@POST
	@Path("/resource")
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * @param userName
	 * @return
	 */
	public Response createApplicationResource(InputStream incomingData,
			@DefaultValue("") @HeaderParam("user-name") String userName) {
		
			// results
			AppResource resource=null;
			
			getLog().info("HEADER user-name: " + userName);
			
			//
			// Initialize service
			lookupService.init();
			
			/**
			 * Process the request
			 */
			try {
				
				// get the request reader ready 
				BufferedReader reader = new BufferedReader(new InputStreamReader(incomingData, StandardCharsets.UTF_8));
				
				//
				// get the parser ready
				Gson gson = new GsonBuilder()
			            .setDateFormat("YYYY-MM-DD HH:MM:SS")
			            .create();
				
				// parse the JSON input into the specific class
				// newDocument = gson.fromJson(reader, Document.class);
				resource = gson.fromJson(reader, AppResource.class);
				
				//
				// process the request
				
				// create a new doc type
				if(!lookupService.doesResourceExist(resource)){
					resource = lookupService.createAppResource(resource);
					getLog().error("[Creating] a new resource: - " + resource);
				}else{
					resource = lookupService.updateAppResource(resource);
					getLog().error("[Updating] an existing resource: - " + resource);
				}
				

			} catch (Exception e) {
				getLog().error("Error Creating a new resource: - " + e);
				// return HTTP response 200 in case of success
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal Error").build();
			}
			
			// return HTTP response 200 in case of success
			return Response.status(Status.OK).entity(RESTUtility.getJSON(resource)).build();
	}

	
	
	@DELETE
	@Path("/resource/{resourceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	/**
	 * This will mark a document'spages as deleted
	 * @param incomingData-  the incoming list of ids for the pages to be removed 
	 * @param userName - the user requesting the operation
	 * @param documentId - the id of the document's pages to delete
	 * @return - request response
	 */
	public Response deleteResource(
			@DefaultValue("") @HeaderParam("user-name") String userName,
			@PathParam("resourceId") long resourceId) {
		
		String result = "Delete of Resource with id: " + resourceId 
							+ " <SUCCESS>";
		
		//
		// Initialize service
		lookupService.init();
		
		/**
		 * Process the request
		 */
		try {
			// get the request reader ready 
		
			lookupService.deleteAppResource(resourceId);

		} catch (Exception e) {
			getLog().error("Error Deleting Resource with id: " + resourceId + " " + e);
		}
		
		System.out.println("DELETE Resource with id=" + resourceId);
		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
	}

	
	@GET
	@Path("/summary")
	@Produces(MediaType.TEXT_PLAIN)
	public Response versionRESTSummary(InputStream incomingData) {
		String result = "/server/version is " + SERVER_CODE_VERSION + "\r\n " + "\r\n "
						+ " - ver 1.18 ---------" + "\r\n "
						+ "   1. Added ability to audit Document Reads."
						+ " \r\n "
						+ " - ver 1.19 ---------" + "\r\n "
						+ "   1. Added Document search capabilities for (i) Doc Type (ii) owner (iii) date from  (iv) date to."
						+ " \r\n "
						+ " - ver 1.20 ---------" + "\r\n "
						+ "   1. Document Search Fixes"
						+ " \r\n "
						+ " - ver 1.21 ---------" + "\r\n "
						+ "   1. Profile Image Update and Fetch" + "\r\n "
						+ "      [Needs further work and testing]"
						+ " \r\n "
						+ " - ver 1.22 ---------" + "\r\n "
						+ "   1. Fatching all tags" + "\r\n "
						+ " \r\n "
						+ " - ver 1.23 ---------" + "\r\n "
						+ "   1. Generating small thumbnails for fast pre-fetch" + "\r\n "
						+ " \r\n "
						+ " - ver 1.24 ---------" + "\r\n "
						+ "   1. Tagging" + "\r\n "
						+ " \r\n "
						+ " - ver 1.25 ---------" + "\r\n "
						+ "   1. Doc Linking" + "\r\n "
						+ " \r\n "
						+ " - ver 1.26 ---------" + "\r\n "
						+ "   1. Doc Attaching" + "\r\n "
						+ " \r\n "
						+ " - ver 1.27 ---------" + "\r\n "
						+ "   1. Universal Date fix" + "\r\n "
						+ " \r\n "
						+ " - ver 1.28 ---------" + "\r\n "
						+ "   1. Generation of Organization and Group Data" + "\r\n "
						+ "   2. OrganizationRESTService added" + "\r\n "
						+ "   3. Changed/Added extra information to User authentication token pertaining to org, and groups" + "\r\n "
						+ " \r\n "
						+ " - ver 1.29 ---------" + "\r\n "
						+ "   1. Fixed issues with filtering data on groups and organizations" + "\r\n "
						+ " \r\n "
						+ " - ver 1.30 ---------" + "\r\n "
						+ "   1. Added the ability to fetch traceability data" + "\r\n "
						+ " \r\n "
						+ " - ver 1.31 ---------" + "\r\n "
						+ "   1. Fixed issues with authentication and token extraction" + "\r\n "
						+ " \r\n "
						+ " - ver 1.32 ---------" + "\r\n "
						+ "   1. Fixed issues with offline 'My Documnts' doc creation and loading" + "\r\n "
						+ " \r\n "
						+ " - ver 1.33 ---------" + "\r\n "
						+ "   1. Added sparse capabilities to the doc creation process" + "\r\n "
						+ " \r\n "
						+ " - ver 1.34 ---------" + "\r\n "
						+ "   1. Fixed All Documents Filter issue" + "\r\n "
						+ " \r\n "
						+ " - ver 1.35 ---------" + "\r\n "
						+ "   1. Fixed JSON misaligment issue [SearchResult]" + "\r\n "
						+ " \r\n "
						+ " - ver 1.36 ---------" + "\r\n "
						+ "   1. Added ability to audit doc creations" + "\r\n "
						+ " \r\n "
						+ " - ver 1.37 ---------" + "\r\n "
						+ "   1. Added Notifications for DOCUMENT_CREATE" + "\r\n "
						+ "   2. Added Notifications REST end points" + "\r\n "
						+ "   2. Added Audit DAO for additional audits" + "\r\n "
						+ " \r\n "
						+ " - ver 1.38 ---------" + "\r\n "
						+ "   1. Added the ability to set document recipients" + "\r\n "
						+ "   2. MOdified Document Add and Fetch" + "\r\n "
						+ " \r\n "
						+ " - ver 1.39 ---------" + "\r\n "
						+ "   1. Fixed some notifations" + "\r\n "
						+ " \r\n "
						+ " - ver 1.40 ---------" + "\r\n "
						+ "   1. Added Flyout menu definitions" + "\r\n "
						+ " \r\n "
						+ " - ver 1.41 ---------" + "\r\n "
						+ "   1. Added Edit functionality" + "\r\n "
						+ " \r\n "
						+ " - ver 1.42 ---------" + "\r\n "
						+ "   1. Fixed up issues with DocumentDataType for Linked and Attached docs" + "\r\n "
						+ " \r\n "
						+ " - ver 1.43 ---------" + "\r\n "
						+ "   1. Fixed up isues with image upscaling for doc pages" + "\r\n "
						+ " \r\n "
						+ " - ver 1.44 ---------" + "\r\n "
						+ "   1. Added the ability to store edited doc pages" + "\r\n "
						+ "   2. Fixed issues with Tag refresh for custom tags" + "\r\n "
						+ " \r\n "
						+ " - ver 1.45 ---------" + "\r\n "
						+ "   1. Fixed edited doc pages to provide ids back" + "\r\n "
						+ " \r\n "
						+ " - ver 1.46 ---------" + "\r\n "
						+ "   1. Fixed issues with being able to create new tags from new Document data" + "\r\n "	
						+ " \r\n "
						+ " - ver 1.47 ---------" + "\r\n "
						+ "   1. Changes to DB for Admin Portal" + "\r\n "
						+ "   2. Changes to REST Services for Admin Portal" + "\r\n "
						+ " \r\n "
						+ " - ver 1.48 ---------" + "\r\n "
						+ "   1. Added new flavor of fetch users API for a super-user" + "\r\n "
						+ " \r\n "
						+ " - ver 1.49 ---------" + "\r\n "
						+ "   1. Enhanced role management and retrieval" + "\r\n "
						+ " \r\n "
						+ " - ver 1.50 ---------" + "\r\n "
						+ "   1. Fixed up User Creation process" + "\r\n "			
						+ " \r\n "
						+ " - ver 1.51 ---------" + "\r\n "
						+ "   1. Fixed issues with attaching Organizations to users" + "\r\n "
						+ " \r\n "
						+ " - ver 1.52 ---------" + "\r\n "
						+ "   1. Added Status Management for Documents" + "\r\n "
						+ " \r\n "
						+ " - ver 1.53 ---------" + "\r\n "
						+ "   1. Fixed ability to write status changes for documents" + "\r\n "
						+ " \r\n "
						+ " - ver 1.54 ---------" + "\r\n "
						+ "   1. Fixed ability to read/write document Notes" + "\r\n "	
						+ " \r\n "
						+ " - ver 1.55 ---------" + "\r\n "
						+ "   1. Added ability to update document data" + "\r\n "
						+ " \r\n "
						+ " - ver 1.56 ---------" + "\r\n "
						+ "   1. Added 1-up and 1-down permissions" + "\r\n "
						+ " \r\n "
						+ " - ver 1.57 ---------" + "\r\n "
						+ "   1. Changed the Notifications Routine - per doc workflow" + "\r\n "
						+ " \r\n "
						+ " - ver 1.58 ---------" + "\r\n "
						+ "   1. Added the ability to create doc fromparsing PDFs" + "\r\n "
						+ "   2. Fixed the image creation to 150DPI as JPG keeping the cap on the image size." + "\r\n "
						+ " \r\n "
						+ " - ver 1.59 ---------" + "\r\n "
						+ "   1. Added the ability to create a doc with recipients" + "\r\n "
						+ "   2. Added the ability to create a doc with Linked and Backing Docs" + "\r\n "
						+ " \r\n "
						+ " - ver 1.60 ---------" + "\r\n "
						+ "   1. Added the ability to create doc with tags (PDF)" + "\r\n "
						+ " \r\n "
						+ " - ver 1.61 ---------" + "\r\n "
						+ "   1. Added the ability to update the use data (nickname and lineId)" + "\r\n "
						+ "   2. Added the ability to filter deleted doc types" + "\r\n "
						+ " \r\n "
						+ " - ver 1.62 ---------" + "\r\n "
						+ "   1. Improved ability to pull doc records" + "\r\n "
						+ "   2. Fixed all document db conversion functions" + "\r\n "
						+ " \r\n "
						+ " - ver 1.63 ---------" + "\r\n "
						+ "   1. Added Password Edit capability" + "\r\n "
						+ " \r\n "
						+ " - ver 1.64 ---------" + "\r\n "
						+ "   1. Fixed issues with CREATE being added at doc creation process instead of SUBMIT" + "\r\n "
						+ " \r\n "
						+ " - ver 1.65 ---------" + "\r\n "
						+ "   1. Added New Tag functionality." + "\r\n "
						+ "       - Custom Prefix LU Table" + "\r\n "
						+ "       - Added new values to the TagData table" + "\r\n "
						+ "       - Modified the TagData Model as well" + "\r\n "
						+ " \r\n "
						+ " - ver 1.66 ---------" + "\r\n "
						+ "   1. Fixed an issue with Tag creation compatibility" + "\r\n "
						+ " \r\n "
						+ " - ver 1.67 ---------" + "\r\n "
						+ "   1. Added preliminary Notification Admin" + "\r\n "
						+ " \r\n "
						+ " - ver 1.68 ---------" + "\r\n "
						+ "   1. Fixed Notification Issues" + "\r\n "
						+ " \r\n "
						+ " - ver 1.69 ---------" + "\r\n "
						+ "   1. Added SYSTEM notifications" + "\r\n "
						+ " \r\n "
						+ " - ver 1.70 ---------" + "\r\n "
						+ "   1. Added GROUP notifications" + "\r\n "
						+ " \r\n "
						+ " - ver 1.71 ---------" + "\r\n "
						+ "   1. Fixed Tags" + "\r\n "
						+ "   2. Fixed Notification clicks" + "\r\n "
						+ " \r\n "
						+ " - ver 1.72 ---------" + "\r\n "
						+ "   1. Fixed Tags when teh id is unknown" + "\r\n "
						+ " \r\n "
						+ " - ver 1.73 ---------" + "\r\n "
						+ "   1. Fixed Tags and Linked lists" + "\r\n "
						+ " \r\n "
						+ " - ver 1.74 ---------" + "\r\n "
						+ "   1. Additional fixes to linked lists and tags" + "\r\n "
						+ "   2. Fixed backup docs" + "\r\n "
						+ "       - Go directly into Accepted mode" + "\r\n "
						+ " \r\n "
						+ " - ver 1.75 ---------" + "\r\n "
						+ "   1. PDF Upload fixes" + "\r\n "
						+ " \r\n "
						+ " - ver 1.76 ---------" + "\r\n "
						+ "   1. PDF Upload fixes - Return the created Document POJO to the caller" + "\r\n "
						+ " \r\n "
						+ " - ver 1.77 ---------" + "\r\n "
						+ "   1. Added RECALL functionality" + "\r\n "
						+ " \r\n "
						+ " - ver 1.78 ---------" + "\r\n "
						+ "   1. Added 'owner' functionality to Tag creation" + "\r\n "
						+ "   2. Added searching for tags outside of the permissions scope" + "\r\n "
						+ " \r\n "
						+ " - ver 1.79 ---------" + "\r\n "
						+ "   1. Added the ability to import PDFs with 'DRAFT' or 'SUBMITTED'" + "\r\n "
						+ " \r\n "
						+ " - ver 1.80 ---------" + "\r\n "
						+ "   1. Fixed Tagging lookup issue" + "\r\n "
						+ " \r\n "
						+ " - ver 1.81 ---------" + "\r\n "
						+ "   1. Added GPS Capabilities to Doc creation" + "\r\n "
						+ " \r\n "
						+ " - ver 1.82 ---------" + "\r\n "
						+ "   1. Improved <Document> fetches based on Stress test results" + "\r\n "
						+ " \r\n "
						+ " - ver 1.83 ---------" + "\r\n "
						+ "   1. Added the ability for the organization flat fetch to provide allowed doc information" + "\r\n "
						+ " \r\n "
						+ " - ver 1.84 ---------" + "\r\n "
						+ "   1. Added Stages creation and Updating" + "\r\n "
						+ " \r\n "
						+ " - ver 1.85 ---------" + "\r\n "
						+ "   1. Modified User Super-Admin login" + "\r\n "
						+ "   2. Added ability to save doc types" + "\r\n "
						+ "   3. Fixed Organization Filtering Issues" + "\r\n "
						+ "   4. Fixed Organization TYpe Filtering Issues" + "\r\n "
						+ "   5. Added Ability to create empty allowed docs with permissions" + "\r\n "
						+ "   6. Added Resourcses which are served with User data upon AUTH" + "\r\n "
						+ "   7. Added cascading delete for doc and its pages" + "\r\n "
						+ "   8. Added batch doc page delete" + "\r\n "
						+ "   9. Added Create Stage and Create Doc type to Internationalization" + "\r\n "
						+ "   10. Added Supported Languages" + "\r\n "
						+ "   11. Updated Stage CRUD Logic" + "\r\n "
						+ "   12. Added ability to delete resources" + "\r\n "
						+ "   13. Added key to out of <fetchallgrouptypes> !! reversed with name:value" + "\r\n "
						+ "   14. Added new Error Codes for Doc Creation/Update" + "\r\n "
						+ " \r\n "
						+ " - ver 1.86 ---------" + "\r\n "
						+ "   1. Added JNDI Connection Pooling and Configuration for DB Connections" + "\r\n "
						+ "   2. Fixed issue with stages not being put into group_allowed_doctype_rel table" + "\r\n "
						+ "   3. Added proper Stage Deletion (still need allowed docs fix)" + "\r\n "
						+ "   4. Added <Allowed docs> deletion fix" + "\r\n "
						+ "   5. Added <Allowed docs> debug log entries" + "\r\n "
						+ "   6. Added Profile Docs as part of Trace recursion" + "\r\n "
						+ " \r\n "
						+ " - ver 1.87 ---------" + "\r\n "
						+ "   1. Speed Improvements : Attachment Docs API Change" + "\r\n "
						+ "   2. Speed Improvements : Linked Docs API Change" + "\r\n "
						+ " \r\n "
						+ " - ver 1.88 ---------" + "\r\n "
						+ "   1. Added /document/pageimage form-data based page addition" + "\r\n "
						+ "   2. Added /document/pageimage JSON page return value" + "\r\n "
						+ " \r\n "
						+ " - ver 1.89 ---------" + "\r\n "
						+ "   1. Fixed User COntact Information fetch data" + "\r\n "
						+ " \r\n "
						+ " - ver 1.90 ---------" + "\r\n "
						+ "   1. Fixed up being able to read UTF-8 Strings from applications" + "\r\n "
						+ " \r\n "
						+ " - ver 1.91 ---------" + "\r\n "
						+ "   1. Fixed issues with updating a document with all peripheral elements deleted" + "\r\n "
						+ "   2. Update Date changes now with Document Update" + "\r\n "
						+ " \r\n "
						+ " - ver 1.92 ---------" + "\r\n "
						+ "   1. Added new columns to the group/organization data" + "\r\n "
						+ "   2. Added Edit ability for group/organization data" + "\r\n "
						+ "   3. Removed restriction on Backup docs - All user's docs are used as backup " + "\r\n "
						+ " \r\n "
						+ " - ver 1.93 ---------" + "\r\n "
						+ "   1. Added CRUD for Dynamic Field Definition" + "\r\n "
						+ "   2. Fixed issue with SQL for Fetch" + "\r\n "
						+ "   3. Added Dynamic Definition to user startup data" + "\r\n "
						+ "   4. Added Dynamic Definition CRUD" + "\r\n "
						+ "   5. Added Dynamic Definition Namde field toData for faster render" + "\r\n "
						+ "   6. Fixed up the Doc Field Data fetch" + "\r\n "
						+ " \r\n "
						+ " - ver 1.94 ---------" + "\r\n "
						+ "   1. Added designated recipient permission logic" + "\r\n "
						+ "   2. Added PDF Data for Doc Info" + "\r\n "
						+ " \r\n "
						+ " - ver 1.95 ---------" + "\r\n "
						+ "   1. Added Trace GPS functionality (Model changes and new API addition)" + "\r\n "
						+ "   2. Added the ability to reorder existing doc pages" + "\r\n "
						+ "   3. Added proper page sorting for doc fetch" + "\r\n "
						+ " \r\n "
						+ " - ver 1.96 ---------" + "\r\n "
						+ "   1. Add the ability to import <new>multi-file PDF with Doc Info" + "\r\n "
						+ "   2. Add the ability to import <edit> multi-file PDF with Doc Info" + "\r\n "
						+ "   3. Added the collation and page deletion algorithm" + "\r\n "
						+ "   4. Fixed the collation for new doc form upload" + "\r\n "
						+ "   5. Fixed the collation for multi-doc upload" + "\r\n "
						+ "   6. Mobile create/update fix for pages" + "\r\n "
						+ " \r\n "
						+ " - ver 1.97 ---------" + "\r\n "
						+ "   1. Complete split between update and create document for android and iOS platforms" + "\r\n "
						+ "   2. Added form-based page update" + "\r\n "
						+ " \r\n "
						+ " - ver 1.98 ---------" + "\r\n "
						+ "   1. Fix to hashing function for more secure 1-way hash" + "\r\n "
						+ "   2. Added the ability to provide user id for doc linking" + "\r\n "
						+ "   3. Added user profile update changes - spilt out user operations" + "\r\n "
						+ "   4. Added email validation for format" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99 ---------" + "\r\n "
						+ "   1. Removed collation from document create for pages" + "\r\n "
						+ "   2. Added UNIQUE constraint on docuemnts" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99.1 ---------" + "\r\n "
						+ "   1. Added permission for a user to get access to backup docs pages if they are a recipient" + "\r\n "
						+ "   2. Added email address to roaganization (Group)" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99.2 ---------" + "\r\n "
						+ "   1. Additional Functions 1 <screening>" + "\r\n "
						+ "   2. Added Document Type Edit" + "\r\n "
						+ "   3. Added LEFT JOIN to Groups" + "\r\n "
						+ "   4. Additional Functions 2 <screening>" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99.4 ---------" + "\r\n "
						+ "   1. Additional Recipient adding of linked and backup docs" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99.5 ---------" + "\r\n "
						+ "   1. Added permission restriction to DRAFT documents that a user is a recipient on" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99.6 ---------" + "\r\n "
						+ "   1. Added login username to be case insensitive for resource fetches" + "\r\n "
						+ "   2. Added the ability to lock a document if it is ACEPTED and part of a trace" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99.7 ---------" + "\r\n "
						+ "   1. Add"
						+ "ed more logs for iOS Data creation for spares docuemnts" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99.8 ---------" + "\r\n "
						+ "   1. Fixed issues with reading OCR data" + "\r\n "
						+ " \r\n "
						+ " - ver 1.99.9 ---------" + "\r\n "
						+ "   1. Fixed issues with new RESTResponse Data" + "\r\n "
						+ " \r\n "
						+ " - ver 1.100 ---------" + "\r\n "
						+ "   1. Fixed issues with email template sending configurations" + "\r\n "
						+ "   2. Fixed issues with activation email getting teh right address" + "\r\n "
						+ "   3. Fixed issues with contact data  with activated and verified flags" + "\r\n "
						+ "   4. Added proper email verification for format" + "\r\n "
						+ "   5. added authentication to account for activation/verification flag" + "\r\n "
						+ "   6. added verified as true when users are batch created" + "\r\n "
						+ " \r\n "
						+ " - ver 1.110 ---------" + "\r\n "
						+ "   1. Added registration capabilities" + "\r\n "
						+ "   2. Fixed up locked document for ACCEPTED docs in a trace" + "\r\n "
						+ "   3. Added activation trigger for verification user update" + "\r\n "
						+ "   4. Fixed Linked Docs returning a null for <fetchalldocstolink>" + "\r\n "
						+ " \r\n "
						+ " - ver 1.111 ---------" + "\r\n "
						+ "   1. Added Org Batch Upload" + "\r\n "
						+ "   2. Added New ORG CSV Template" + "\r\n "
						+ "   3. Added Org Registration API/Fixes" + "\r\n "
						+ " - ver 1.112 ---------" + "\r\n "
						+ "   1. Additional REgistration Fixes" + "\r\n "
						+ " \r\n "
						+ " - ver 1.12 ---------" + "\r\n "
						+ "   1. Added new PDF Export for Trace (font and extra doc info field row)" + "\r\n "
						+ " \r\n "
						+ " - ver 1.13 ---------" + "\r\n "
						+ "   1. Added GPS Location for Document into the main Doc Creation routine" + "\r\n "
						+ "   2. Fixed up comma issues with Doc Info Fields" + "\r\n "
						+ "   3. Fixed up recipients returning null result" + "\r\n "
						+ "   4. Fixed up Doc Info fields delimiter for parsing issues." + "\r\n "
						+ " \r\n "
						+ " - ver 1.14 ---------" + "\r\n "
						+ "   1. Fixed up the trace report data format" + "\r\n "
						+ " \r\n "
						+ " - ver 1.15 ---------" + "\r\n "
						+ "   1. Fixed up the trace PDF report Unicide encoding issue" + "\r\n "
						+ "   2. Fixed the image scaling issue (1st attempt)" + "\r\n "
						+ " \r\n "
						+ " - ver 1.16 ---------" + "\r\n "
						+ "   1. Added stage associations in group_data_type" + "\r\n "
						+ "   2. Added new API for group data extraction and recipients" + "\r\n "
						+ "   3. Added new restiction for recipients to not see other orgs in their stage" + "\r\n "
						+ "   4. Added pdf export trace generation to be dynamic (for doc info) and requires no images" + "\r\n "
						+ "   5. Fixed up a Math.cail issue with dunamic export" + "\r\n "
						+ " \r\n "
						+ " - ver 1.117 ---------" + "\r\n "
						+ "   1. Fixed up an export trace issue with duplicate elements" + "\r\n "
						+ "   2. Added recipients permissions configuratation (Super Admin by defult)" + "\r\n "
						+ " \r\n "
						+ " - ver 1.118 ---------" + "\r\n "
						+ "   1. Fixed duplicate documents issue in the export data for traces." + "\r\n "
						+ "   2. Fixed MATRIX_ADMIN role with reference to DRAFT and REJECTED documents." + "\r\n "
						+ " \r\n "
						+ " - ver 1.119 ---------" + "\r\n "
						+ "   1. Added configuration for self-referential stage recipients." + "\r\n "
						+ " \r\n "
						+ " - ver 1.120 ---------" + "\r\n "
						+ "   1. Added Remember Me token authentication." + "\r\n "
						+ " \r\n "
						+ " - ver 1.121 ---------" + "\r\n "
						+ "   1. Added value for trace columns internationalization." + "\r\n "
						+ " \r\n "
						+ " - ver 1.122 ---------" + "\r\n "
						+ "   1. Fixed up fetchall_v2 for document fetching" + "\r\n "
						+ "       - optimized fetching all docs for the user requestor" + "\r\n "
						+ "   2. Fixed document creation bug." + "\r\n "
						+ "       - Added a fetchalldocstolink_v2" + "\r\n "
						+ "       - Added a fetchAllDocumentsLinkCollection_v2" + "\r\n "
						+ "       - Added a fetchattachdoccollection_v2" + "\r\n "
						+ "   3. Added MATRIX and SUPER roles." + "\r\n "
						+ "       - Separated MATRIX Role out for now " + "\r\n "
						+ " \r\n "
						+ " - ver 1.123 ---------" + "\r\n "
						+ "   1. Added new trace export data fetch for MATRIX_ADMIN" + "\r\n"
						+ "   2. Added update to fetch all docs for MATRIX_ADMIN" + "\r\n"
						+ " \r\n "
						+ " - ver 1.1243 ---------" + "\r\n "
						+ "   1. Added new server update date field in DB" + "\r\n"
						+ "   2. Coded the update to a new Docuemnt data model" + "\r\n"
						+ "   3. Added spacial cases for DRAFT->SUBMIT, and REJECT statuses for docs" + "\r\n";
		

		// return HTTP response 200 in case of success
		return Response.status(Status.OK).entity(result).build();
	}
}
