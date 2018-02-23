package MFClient.Tests;


import java.util.HashMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import MFClient.Wrappers.ExternalRepositoryHelper;
import MFClient.Wrappers.Utility;

public class ObjectActionsExternalRepository extends ObjectActions{


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
	 * Overriding those tests from superclass that have no relevance with external repositories at the moment.
	 * Overriding is done without @Test annotation so TestNG will not execute them.
	 */

	@Override
	public void SprintTest43_3_4_4A(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest43_3_4_4B(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest43_3_4_4C(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest43_3_4_5A(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest43_3_4_5B(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest43_3_4_5C(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest32_5_2_4A(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest32_5_2_4B(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest32_5_2_5A(HashMap<String,String> dataValues, String driverType){

		return;
	}

	@Override
	public void SprintTest32_5_2_5B(HashMap<String,String> dataValues, String driverType){

		return;
	}

}
