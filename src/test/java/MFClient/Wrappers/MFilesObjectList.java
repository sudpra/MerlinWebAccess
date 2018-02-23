package MFClient.Wrappers;

public enum MFilesObjectList {
	
	DOCUMENT("0"), ASSIGNMENT("10"), CONTACT_PERSON("149"),CUSTOMER("136"),REPORT("15"),PROJECT("101"),EMPLOYEE("156"),DOCUMENT_COLLECTION("9");
	
	private String objectName;

	MFilesObjectList(String obj) {
		this.objectName = obj;
	}

	public String getValue() {
		return objectName;
	}
}
