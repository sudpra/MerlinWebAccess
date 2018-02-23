package MFClient.Tests.ExternalConnectors;


import org.testng.annotations.BeforeClass;
import MFClient.Wrappers.ExternalRepositoryHelper;

/* UserSpecificAuthExternalRepositorySmoke test class extends CommonAuthExternalRepositorySmoke test class and will execute all its test cases
 * The only difference is the BeforeClass method which in this sub-class will log in using UserSpecific authentication.
 */
public class UserSpecificAuthExternalRepositorySmoke extends CommonAuthExternalRepositorySmoke {

	@BeforeClass (alwaysRun=true)
	public void UserSpecificLogin() throws Exception{

		ExternalRepositoryHelper.userSpecificLogin();

	}

}
