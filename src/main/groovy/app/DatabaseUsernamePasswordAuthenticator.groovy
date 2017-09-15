package app

import com.google.inject.Inject
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.CommonHelper

import org.pac4j.http.credentials.UsernamePasswordCredentials
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator
import org.pac4j.http.profile.HttpProfile

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
class DatabaseUsernamePasswordAuthenticator implements UsernamePasswordAuthenticator {
  private static final int ITERATIONS = 1000
  private static final int KEY_LENGTH = 192 

  private final Sql sql
  
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUsernamePasswordAuthenticator.class)

  @Inject
  public DatabaseUsernamePasswordAuthenticator(Sql sql) { // <1>
    this.sql = sql
  }

  @Override
  void validate(UsernamePasswordCredentials credentials) { 
    
	  LOGGER.debug("Retrieving credentials ${credentials.username} -- ${credentials.password}")
 	  
	  def userRow = sql.firstRow(  // <2>
		  "SELECT * FROM MG_USERS_INFO WHERE USER_NAME = ${credentials.username}"
		  )
	  
		  if (!userRow) { // <3>
			throwsException("Invalid username or password")
		  }
	  
		  def password = userRow["PASSWORD"] // <4>
		  
		  def passwordhash = RSAUtility.encrypt(credentials.password)
	  
		  
		  if (!password || password != passwordhash) {
			throwsException("Invalid username or password.")
		  }
	  
		  credentials.userProfile = new HttpProfile(id: credentials.username)
	
   
  }

  protected void throwsException(final String message) {
    throw new CredentialsException(message);
  }


}
