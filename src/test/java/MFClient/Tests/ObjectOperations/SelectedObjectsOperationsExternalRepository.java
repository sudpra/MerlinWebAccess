package MFClient.Tests.ObjectOperations;

import java.util.HashMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import MFClient.Wrappers.ExternalRepositoryHelper;
import MFClient.Wrappers.Utility;

public class SelectedObjectsOperationsExternalRepository extends SelectedObjectsOperations{

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
	 * Overriding those tests from superclass that are not relevant for external repositories at the moment.
	 * Overriding is done without @Test annotation so TestNG will not execute them.
	 */

	//Not clear what is enabled/disalbed in context/operations menus when both unmanaged & promoted objects are multiselected
	@Override
	public void SprintTest105_6_5(HashMap<String,String> dataValues, String driverType){
		return;
	}

	//Not clear what is enabled/disalbed in context/operations menus when both unmanaged & promoted objects are multiselected
	@Override
	public void SprintTest105_6_6(HashMap<String,String> dataValues, String driverType){
		return;
	}

	//Not clear what is enabled/disalbed in context/operations menus when both unmanaged & promoted objects are multiselected
	@Override
	public void SprintTest105_6_7(HashMap<String,String> dataValues, String driverType){
		return;
	}

	//Not clear what is enabled/disalbed in context/operations menus when both unmanaged & promoted objects are multiselected
	@Override
	public void SprintTest105_6_8(HashMap<String,String> dataValues, String driverType){
		return;
	}

	//SFD/MFD conversions are not supported for unmanaged & promoted objects
	@Override
	public void SprintTest105_6_17(HashMap<String,String> dataValues, String driverType){
		return;
	}

	//SFD/MFD conversions are not supported for unmanaged & promoted objects
	@Override
	public void SprintTest105_6_18(HashMap<String,String> dataValues, String driverType){
		return;
	}

	//SFD/MFD conversions are not supported for unmanaged & promoted objects
	@Override
	public void SprintTest105_6_19(HashMap<String,String> dataValues, String driverType){
		return;
	}

	//SFD/MFD conversions are not supported for unmanaged & promoted objects
	@Override
	public void SprintTest105_6_20(HashMap<String,String> dataValues, String driverType){
		return;
	}

}
