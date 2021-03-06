package eu.stork.ss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;
import eu.stork.ss.Monitoring;
import eu.stork.peps.auth.commons.PEPSUtil;
import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.STORKAuthnRequest;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import eu.stork.peps.exceptions.STORKSAMLEngineException;

public class ValidateSelection extends AbstractAction {
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = -7878347835293457787L;

	//The logger
	static final Logger logger = LoggerFactory.getLogger(ValidateSelection.class.getName());

	//Configuration properties
	private Properties configs; 

	//List of known countries
	private ArrayList<Country> countries;

	//Indicates that an error was found (since the validate returns false)
	private int errorFound;

	//The URL to send the SAML request
	private String spepsUrl;

	//The SAML request
	private String samlToken;

        Monitoring monitor = new Monitoring(); 
	/**
	 * Check that the session variables are OK and display the list of known countries
	 * for the user to select. The selected country must be the users home CPEPS.
	 */
	public String execute() {
		HttpSession session;
		Country selectedCountry = null;
		String selectedId = this.getServletRequest().getParameter(Constants.SP_PARAM_COUNTRYID);
		session = this.getSession();

		//Check that the session contains valid information
		synchronized(session) {
			if ( session==null || session.getAttribute(Constants.SP_TOKEN)==null
					|| session.getAttribute(Constants.SP_PAL)==null ) {
				String message = "Session is empty or contains invalid data!";
                          monitor.monitoringLog( "<span class='error'>Step 3: Error! "+message+"+</span>");
				logger.error(message);
				throw new ApplicationSpecificServiceException("Session error", message);
			}
		}

		//Load the configuration (list of countries)
		try {
			configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
		} catch (IOException e) {
                    monitor.monitoringLog( "<span class='error'>Step 3: Error! "+e.toString()+"</span>");
			logger.error(e.getMessage());
			throw new ApplicationSpecificServiceException("Could not load configuration file", e.getMessage());
		}

		countries = new ArrayList<Country> ();
		int numCountries = Integer.parseInt(configs.getProperty(Constants.COUNTRY_NUMBER));
		for(int i=1; i<=numCountries; i++){
			Country country = new Country(i,configs.getProperty("country" + Integer.toString(i) + ".name"), configs.getProperty("country" + Integer.toString(i) + ".url"), configs.getProperty("country" + Integer.toString(i) + ".countrySelector"));
			countries.add(country);

			if ( selectedId!=null && selectedId.equals(country.getName()) )
				selectedCountry = country;
		}

		//Validate user selection and prepare the SAML document
		if ( selectedCountry!=null ) {//edw einai h xwra
                    monitor.monitoringLog( "Selected Country: "+ selectedCountry.getName());
			byte[] token = null;		
			STORKAuthnRequest authnRequest = new STORKAuthnRequest();

			logger.debug("Selection OK, starting SAML generation. Country: Id:[" + selectedCountry.getId()
					+ "] Name:[" + selectedCountry.getName() + "] URL:[" + selectedCountry.getUrl() + "]");

			this.spepsUrl = configs.getProperty(Constants.SPEPS_URL);

			authnRequest.setDestination(this.spepsUrl);
			authnRequest.setSpCountry(configs.getProperty(Constants.SP_COUNTRY));

			//V-IDP parameters
			authnRequest.setCitizenCountryCode(selectedCountry.getName());
                           //edw einai to onoma tou sp
			authnRequest.setProviderName(configs.getProperty(Constants.PROVIDER_NAME));	
			authnRequest.setQaa(Integer.parseInt(configs.getProperty(Constants.SP_QAALEVEL)));
			authnRequest.setPersonalAttributeList((IPersonalAttributeList)this.getSession().getAttribute(Constants.SP_PAL));
			authnRequest.setAssertionConsumerServiceURL(configs.getProperty(Constants.SP_RETURN));

			//new parameters
			authnRequest.setSpSector(configs.getProperty(Constants.SP_SECTOR));
			authnRequest.setSpApplication(configs.getProperty(Constants.SP_APLICATION));

			//V-IDP parameters
			authnRequest.setSPID(configs.getProperty(Constants.PROVIDER_NAME));

			try{
				STORKSAMLEngine engine = STORKSAMLEngine.getInstance(Constants.SP_CONF);
				authnRequest = engine.generateSTORKAuthnRequest(authnRequest);

				//Store in session in order to validate them in the response
				session.setAttribute(Constants.SAML_IN_RESPONSE_TO_SP, authnRequest.getSamlId());
				session.setAttribute(Constants.ISSUER_SP, authnRequest.getIssuer());
				//--
			}catch(STORKSAMLEngineException e){
                            monitor.monitoringLog( "<span class='error'>Step 3: Error! "+e.toString()+"</span>");
				logger.error(e.getMessage());
				throw new ApplicationSpecificServiceException("Could not generate token for Saml Request", e.getErrorMessage());
			}
					
			token = authnRequest.getTokenSaml();			
			this.samlToken = PEPSUtil.encodeSAMLToken(token);
			monitor.monitoringLog( "<span class='success'>Step 3: Success!</span>");
			return Action.SUCCESS;
		}
		else {
                    
			logger.debug("No country selected! Present error...");
			setErrorFound(1);
			return Action.INPUT;
		}
	}

	/**
	 * Get the list of known countries participating in STORK
	 * 
	 * @return The list of known CPEPS
	 */
	public ArrayList<Country> getCountries() {
		return countries;
	}

	/**
	 * Set the list of known countries participating in STORK
	 * 
	 * @param countries The countries list
	 */
	public void setCountries(ArrayList<Country> countries) {
		this.countries = countries;
	} 

	/**
	 * Returns the error number in order to display the appropriate message
	 * 
	 * @return The error number
	 */
	public int getErrorFound() {
		return errorFound;
	}

	/**
	 * Set the error number that was found
	 * 
	 * @param errorFound The error number
	 */
	public void setErrorFound(int errorFound) {
		this.errorFound = errorFound;
	}

	/**
	 * Setter for samlToken.
	 * 
	 * @param samlToken The samlToken to set.
	 */
	public void setSamlToken(final String samlToken) {
		this.samlToken = samlToken;
	}

	/**
	 * Getter for samlToken.
	 * 
	 * @return The samlToken value.
	 */
	public String getSamlToken() {
		return samlToken;
	}

	/**
	 * Setter for spepsUrl.
	 * 
	 * @param spepsUrl The spepsUrl to set.
	 */
	public void setSpepsUrl(final String spepsUrl) {
		this.spepsUrl = spepsUrl;
	}

	/**
	 * Getter for spepsUrl.
	 * 
	 * @return The spepsUrl value.
	 */
	public String getSpepsUrl() {
		return spepsUrl;
	}
}