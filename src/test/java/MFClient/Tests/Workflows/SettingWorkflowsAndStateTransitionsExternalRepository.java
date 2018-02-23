package MFClient.Tests.Workflows;

import java.util.HashMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import MFClient.Wrappers.ExternalRepositoryHelper;
import MFClient.Wrappers.Utility;

public class SettingWorkflowsAndStateTransitionsExternalRepository extends SettingWorkflowsAndStateTransitions{

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
	 * Overriding those tests from superclass that expect that the object already has a workflow. Currently
	 * there isn't an implementation to set workflow to promoted object as a setup operation.
	 * Overriding is done without @Test annotation so TestNG will not execute them.
	 */

	@Override 
	public void SprintTest39_1_1A(HashMap<String,String> dataValues, String driverType){
		return;
	}

	@Override 
	public void SprintTest39_1_1B(HashMap<String,String> dataValues, String driverType){
		return;
	}

	public void SprintTest39_1_12(HashMap<String,String> dataValues, String driverType){
		return;
	}
}
