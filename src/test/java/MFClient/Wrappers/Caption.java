package MFClient.Wrappers;


public class Caption {

	public enum ObjecTypes { //Object types

		Assignment					("Assignment"),
		ContactPerson			("Contact person"),
		Customer						("Customer"),
		Document						("Document"),
		DocumentCollection	("Document collection"),
		Employee						("Employee"),
		Project 						("Project"),
		Report 						("Report");


		public String Value;

		ObjecTypes(String caption) {
			this.Value = caption;
		}

	}//end enum objecttypes

	public enum Common { //Object types

		Class			("Class");

		public String Value;

		Common(String caption) {
			this.Value = caption;
		}

	}//end enum Common

	public enum MenuItems { //Menu items and Task Pane items

		Show						("Show"),
		StateTransition  			("State Transition"),
		Home						("Home"),
		CheckOut					("Check Out"),
		CheckIn					("Check In"),
		CheckInWithComments		("Check In with Comments..."),
		UndoCheckOut				("Undo Checkout"),
		Properties					("Properties"),
		History					("History"),
		Relationships				("Relationships"),
		Comments					("Comments"),
		Workflow					("Workflow"),
		RecentlyAccessedByMe		("Recently Accessed by Me"),
		GetMFilesWebURL			("Get M-Files Web URL"),
		SharePublicLink			("Share Public Link"),
		GetHyperlink	            ("Get Hyperlink"),
		AddToFavorites				("Add to Favorites"),
		Favorites					("Favorites"),
		RemoveFromFavorites		("Remove from Favorites"),
		Vaults						("Vaults"),	
		Rename						("Rename"),
		Delete						("Delete"),
		ConvertToSFD_O				("Convert to Single-file Document"),
		ConvertToSFD_C				("Convert to Single-file Docu..."),
		ConvertToMFD_O				("Convert to Multi-file Document"),
		ConvertToMFD_C				("Convert to Multi-file Docum..."),
		ReplaceWithFile			("Replace with File (Upload)"),
		ExportToFile				("Export to file"),
		SaveSearch					("Save Search"),
		DisplayMode				("Display Mode"),
		Details					("Details"),
		Thumbnails					("Thumbnails"),
		GrpObjByObjType			("Group Objects by Object Type"),
		GrpViewsAndFolders			("Group Views and Folders"),
		NavigationPane				("Navigation Pane"),
		ShowInRightPane			("Show metadata in Right pane"),
		ShowInBottomPane			("Show metadata in Bottom pane"),
		MakeCopy					("Make Copy"),
		OpenDownload				("Open (Download)"),
		DownloadFile				("Download File"),
		DownloadFiles				("Download Files"),
		ShowSubObjects				("Show Subobjects"),
		ShowMembers				("Show Members"),
		FileTransfers				("File Transfers"),
		LogOut						("Log Out"),
		GroupViews					("Group Views and Folders"),
		GroupObjects				("Group Objects by Object Type"),
		MultiFileDocument			("Multi-File Document"),
		CheckedOutToMe				("Checked Out to Me"),
		MarkComplete				("Mark Complete"),
		Approve					("Mark Approved"),
		Reject						("Mark Rejected"),
		MarkApproved				("Mark Approved"),
		MarkRejected				("Mark Rejected"),
		SharedFilesAllUsers		("Shared Files (All Users)"),
		Annotation					("Annotation");	

		public String Value;

		MenuItems(String caption) {
			this.Value = caption;
		}

	}//End menu bar

	public enum Column { //Column items

		SortAscending 				("Sort Ascending"),
		SortDescending 			("Sort Descending"),
		InsertColumn				("Insert Column"),
		RemoveThisColumn 			("Remove This Column"),
		SaveAsCommonDisplay 		("Save As Common Display Settings"),
		ResetDisplaySettings		("Reset Display Settings to Defaults"),
		ColumnName					("Name"),
		Coln_CheckedOutTo			("Checked Out To"),
		Coln_Status				("Status"),
		Coln_SingleFile			("Single file"),
		Coln_Version				("Version"),
		Coln_MarkedAsCompleteBy	("Marked as complete by"),
		Coln_MarkedAsRejectedeBy	("Marked as rejected by"),
		Coln_Comment				("Comment");


		public String Value;

		Column(String caption) {
			this.Value = caption;
		}

	}//End ColumnItems

	public enum Search { //Search items

		SearchAllObjects				("Search all objects"),
		SearchOnlyDocuments			("Search only: Documents"),
		SearchOnlyAssignments			("Search only: Assignments"),
		SearchOnlyProjects				("Search only: Projects"),
		SearchOnlyReports				("Search only: Reports"),
		SearchAllWords 				("All Words"),
		SearchAnyWord 					("Any Word"),
		SearchOnlyCustomers			("Search only: Customers"),
		SearchonlyAnnotations			("Search only: Annotations"),
		SearchBoolean					("Boolean");

		public String Value;

		Search(String caption) {
			this.Value = caption;
		}

	}//End Search items

	public enum Classes { //Class Name

		AssignmentBasicClass					("Assignment"),
		AssignmentAnyoneCanApprove				("Assignment that any one can approve"),
		AssignmentAllMustApprove				("Assignment that all must approve"),
		Customer								("Customer");


		public String Value;

		Classes(String caption) {
			this.Value = caption;

		}

	}//End Classname

	public enum GetMFilesWebURL { //Get GetMFilesWebURL

		DefaultLayout				("Default"),
		SimpleListing				("Simple listing"),
		SearchArea					("Search Area"),
		TaskArea					("Task area"),
		PropertiesPane				("Properties Pane"),
		TopMenu						("Top Menu"),
		Breadcrumb					("Breadcrumb"),
		JavaApplet					("Java Applet"),
		Metadatacard				("Metadata card"),
		DownloadSelectedFile		("Download the selected file"),
		ShowSelectedObject			("Show the selected object in M-Files Web"),
		ShowSelectedView			("Show the current view");

		public String Value;

		GetMFilesWebURL(String caption) {
			this.Value = caption;

		}
	}//End GetMFilesWebURL

	public enum Template { //Template dialog

		Template_All			("All"),
		Template_Blank			("Blank"),
		Template_Recent			("Recently Used");

		public String Value;

		Template(String caption) {
			this.Value = caption;
		}

	}// End Template dialog

	public enum Taskpanel { //TaskPanel Items

		AssignedToMe			("Assigned to Me"),
		NewAnnotations			("New Annotations"),
		EditAnnotations			("Edit Annotations"),
		SaveAnnotations			("Save Annotations"),	
		HideAnnotations			("Hide Annotations"),
		ShowAnnotations			("Show Annotations"),			
		Vaults					("Vaults");
		public String Value;

		Taskpanel(String caption) {
			this.Value = caption;
		}

	}// End TaskPanel Items

	public enum PreviewPane { //Preview Pane

		MetadataTab					("Metadata"),
		PreviewTab					("Preview");

		public String Value;

		PreviewPane(String caption) {
			this.Value = caption;
		}

	}// end Preview Pane

	public enum ConfigSettings { //Configuration Settings

		GeneralSettings								("General settings"),
		General										("General"),
		VaultSpecificSettings						("Vault-specific settings"),
		Config_Controls								("Controls"),
		Config_TaskArea								("Task area"),
		Config_Default								("Default layout"),
		Config_DefaultAndNavigation					("Default layout with navigation pane"),
		Config_NoTaskArea							("No task area"),
		Config_TaskAreaWithShowGoTo					("Task area with \"Go To\" shortcuts only"),
		Config_ListingPropertiesPaneOnly			("Listing area and right pane only"),
		Config_ListingPaneOnly						("Listing area only"),
		Config_Show									("Show"),
		Config_Hide									("Hide"),
		Config_Enable								("Enable"),
		Config_Disable								("Disable"),
		Config_SearchInRightPane					("Search in right pane");

		public String Value;

		ConfigSettings(String caption) {
			this.Value = caption;
		}
	}//End ConfigSettings

	public enum MFilesDialog { //Preview Pane

		ConfirmAutoFill		("Confirm Autofill"),
		ElectronicSignature	("Electronic Signature"),
		ExpiredLink				("The link is either expired or invalid.");

		public String Value;

		MFilesDialog(String caption) {
			this.Value = caption;
		}

	}// end Preview Pane

}//Caption





