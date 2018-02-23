package MFClient.Tests.Workflows;

import java.util.HashMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import MFClient.Wrappers.ExternalRepositoryHelper;
import MFClient.Wrappers.Utility;

public class WorkflowStateActionsExternalRepository extends WorkflowStateActions{

	@BeforeClass (alwaysRun=true)
	public void setupExternalRepository() throws Exception{
		
		ExternalRepositoryHelper.installExternalRepository(className, xlTestDataWorkBook);
		
	}
	
	@AfterClass (alwaysRun = true)
	public void cleanExternalRepository() throws Exception{
		try{
			Utility.clearExternalRepository(className);//Clears the external repository created for this class
		}
		catch(Exception e){
			throw e;
		}
		
	}
	
	/* 
	 * Overriding those tests from superclass that do not yet have test data.
	 * Overriding is done without @Test annotation so TestNG will not execute them.
	 */

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_13(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_49(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_51(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_52(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_53(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_55(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_56(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_57(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_58(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_59(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_60(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_61(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_62(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_77(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_78(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_79(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

	//Test data not yet defined for this test case
	@Override
	public void SprintTest39_1_80(HashMap<String,String> dataValues, String driverType) throws Exception {
		return;
	}

}
