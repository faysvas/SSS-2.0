package eu.stork.ss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.Action;
import eu.stork.ss.Monitoring;
import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSUtil;
import eu.stork.peps.auth.commons.STORKAuthnResponse;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import eu.stork.peps.exceptions.STORKSAMLEngineException;

public class ServiceRedirect extends AbstractAction {
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = -5921651086873165403L;

	//The logger
	static final Logger logger = LoggerFactory.getLogger(ServiceRedirect.class.getName());

	//The PAL
	private IPersonalAttributeList pal;

	/**
	 * SAML token.
	 */
	private String SAMLResponse;

	/**
	 * Validate SAML token and extract the PersonalAttributeList.
	 */
	
	Monitoring monitor = new Monitoring(); 


	public String execute() {
           
     
             
             
		ApplicationSpecificServiceException exception = null;

		try {
			byte[] decSamlToken = PEPSUtil.decodeSAMLToken(SAMLResponse);
 monitor.monitoringLog( " Response Token: "+decSamlToken.toString());
			//Get SAMLEngine instance
			STORKSAMLEngine engine = STORKSAMLEngine.getInstance(Constants.SP_CONF);

			try {
				//validate SAML Token
				STORKAuthnResponse authnResponse = engine.validateSTORKAuthnResponseWithQuery(decSamlToken, (String)getServletRequest().getRemoteHost());				

				if( authnResponse.isFail() ){
                                    monitor.monitoringLog( "<span class='error'>Step 4: Error! Saml Response had failed!</span>");
					exception = new ApplicationSpecificServiceException("Saml Response had failed!", authnResponse.getMessage());
				}
				else {
					if ( validateResponse(authnResponse) )
					{
						pal = authnResponse.getTotalPersonalAttributeList();           
						if (pal.isEmpty())
							pal = authnResponse.getPersonalAttributeList();
					}
                                        else{   monitor.monitoringLog( "<span class='error'>Step 4: Error! Saml Response validation failed!. Either the Issuer or the SAML response ID are invalid!</span>");
						exception = new ApplicationSpecificServiceException("Saml Response validation failed!", "Either the Issuer or the SAML response ID are invalid!");
                                        }
                                        }
			}catch(STORKSAMLEngineException e){	
                            monitor.monitoringLog( "<span class='error'>Step 4: Error! "+e.toString()+"</span>");
				exception = new ApplicationSpecificServiceException("Could not validate token for Saml Response", e.getErrorMessage());
			}
		} catch(Exception e) {
                    monitor.monitoringLog( "<span class='error'>Step 4: Error! "+e.toString()+"</span>");
			exception = new ApplicationSpecificServiceException("Saml Response is invalid!", "Failed to parse the SAML response from PEPS.");
		}

		//Check if we had a failure and throw an exception
		if ( exception!=null ) {
                    monitor.monitoringLog( "<span class='error'>Step 4: Error! "+exception.getMessage()+"</span>");
			logger.error(exception.getMessage());
			throw exception;
		}
		monitor.monitoringLog( "<span class='success'>Step 4: Success!</span>");
		return Action.SUCCESS;
	}

	/**
	 * Validates a given {@link STORKAuthnResponse}.
	 * 
	 * @param authnResponse The {@link STORKAuthnResponse} to validate.
	 * 
	 * @return true if all tests are OK
	 */
	private boolean validateResponse(final STORKAuthnResponse attrResponse) {
          
		boolean outcome = false;

		if (getSession() != null) {
			final String sessionIdRequest = attrResponse.getInResponseTo();
			final String sessionIdActual = (String) getSession().getAttribute(Constants.SAML_IN_RESPONSE_TO_SP);

			final String audienceRestriction = attrResponse.getAudienceRestriction();
			final String issuer = (String) getSession().getAttribute(Constants.ISSUER_SP);

			if ( sessionIdActual == null || issuer == null ) {
				getSession().invalidate();
			}
			else {
				if (sessionIdRequest != null
						&& sessionIdActual.equals(sessionIdRequest)
						&& audienceRestriction != null && issuer.equals(audienceRestriction)) {
					outcome = true;
				}
			}
		}

		return outcome;
	}

	/**
	 * Setter for SAMLResponse.
	 * 
	 * @param SAMLResponse the SAMLResponse to set.
	 */
	public void setSAMLResponse(final String SAMLResponse) {
		this.SAMLResponse = SAMLResponse;
	}
	
	/**
	 * Getter for SAMLResponse.
	 * 
	 * @return the SAMLResponse value.
	 */
	public String getSAMLResponse() {
		return SAMLResponse;
	}

	/**
	 * Getter for the PAL.
	 * 
	 * @return The PAL.
	 */
	public IPersonalAttributeList getPersonalAttributeList() {
		return pal;
	}

	/**
	 * Setter for the PAL.
	 * 
	 * @param pal The PAL.
	 */
	public void setPersonalAttributeList(IPersonalAttributeList pal) {
		this.pal = pal;
	}
}